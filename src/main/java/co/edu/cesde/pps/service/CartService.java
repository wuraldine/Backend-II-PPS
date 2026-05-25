package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.CartDTO;
import co.edu.cesde.pps.enums.CartStatus;
import co.edu.cesde.pps.exception.CartMergeException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.InsufficientStockException;
import co.edu.cesde.pps.exception.InvalidCartStateException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.mapper.CartMapper;
import co.edu.cesde.pps.model.Cart;
import co.edu.cesde.pps.model.CartItem;
import co.edu.cesde.pps.model.Product;
import co.edu.cesde.pps.model.User;
import co.edu.cesde.pps.model.UserSession;
import co.edu.cesde.pps.repository.CartRepository;
import co.edu.cesde.pps.repository.UserSessionRepository;
import co.edu.cesde.pps.util.CalculationUtils;
import co.edu.cesde.pps.util.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@Transactional(readOnly = true)
public class CartService {

    private final CartMapper cartMapper;
    private final UserService userService;
    private final ProductService productService;
    private final CartRepository cartRepository;
    private final UserSessionRepository userSessionRepository;

    public CartService(UserService userService, ProductService productService,
                       CartRepository cartRepository, UserSessionRepository userSessionRepository) {
        this.cartMapper = new CartMapper();
        this.userService = userService;
        this.productService = productService;
        this.cartRepository = cartRepository;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    public CartDTO createCartForGuest(Long sessionId) {
        Cart cart = Cart.builder()
                .user(null)
                .session(resolveSession(sessionId))
                .status(CartStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO createCartForUser(Long userId) {
        User user = userService.findUserEntityOrThrow(userId);
        Cart cart = Cart.builder()
                .user(user)
                .status(CartStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    public CartDTO findById(Long cartId) {
        return cartMapper.toDTO(findCartEntityOrThrow(cartId));
    }

    public CartDTO findOpenCartByUser(Long userId) {
        Cart cart = cartRepository.findByUser_UserIdAndStatus(userId, CartStatus.OPEN).orElse(null);
        return cart != null ? cartMapper.toDTO(cart) : null;
    }

    public CartDTO findOpenCartBySession(Long sessionId) {
        if (sessionId == null) return null;
        Cart cart = cartRepository.findBySession_SessionIdAndStatus(sessionId, CartStatus.OPEN).orElse(null);
        return cart != null ? cartMapper.toDTO(cart) : null;
    }

    @Transactional
    public CartDTO findOrCreateOpenCartForUser(Long userId) {
        return cartMapper.toDTO(findOrCreateOpenCartEntityForUser(userId));
    }

    @Transactional
    public CartDTO findOrCreateOpenCartForGuestSession(Long sessionId) {
        ValidationUtils.validateNotNull(sessionId, "sessionId");
        Cart cart = cartRepository.findBySession_SessionIdAndStatus(sessionId, CartStatus.OPEN)
                .orElseGet(() -> {
                    UserSession session = resolveSession(sessionId);
                    return cartRepository.save(Cart.builder()
                            .user(null)
                            .session(session)
                            .status(CartStatus.OPEN)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build());
                });
        return cartMapper.toDTO(cart);
    }

    @Transactional
    public CartDTO addItem(Long cartId, Long productId, Integer quantity) {
        ValidationUtils.validatePositive(quantity, "quantity");
        Cart cart = findCartEntityOrThrow(cartId);
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(), CartStatus.OPEN, "add item");
        }

        Product product = productService.findProductEntityOrThrow(productId);
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ValidationException("Product '" + product.getName() + "' is not active");
        }
        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), quantity)) {
            throw new InsufficientStockException(productId, product.getSku(), quantity, product.getStockQty());
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findFirst().orElse(null);

        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (!CalculationUtils.hasEnoughStock(product.getStockQty(), newQuantity)) {
                throw new InsufficientStockException(productId, product.getSku(), newQuantity, product.getStockQty());
            }
            existingItem.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart).product(product).quantity(quantity)
                    .unitPrice(product.getPrice()).addedAt(LocalDateTime.now()).build();
            cart.getItems().add(newItem);
            newItem.setCart(cart);
        }

        touchCart(cart);
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO updateItemQuantity(Long cartId, Long productId, Integer newQuantity) {
        ValidationUtils.validatePositive(newQuantity, "quantity");
        Cart cart = findCartEntityOrThrow(cartId);
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(), CartStatus.OPEN, "update item");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getProductId().equals(productId))
                .findFirst().orElseThrow(() -> new ValidationException("Product not found in cart"));

        Product product = item.getProduct();
        if (!CalculationUtils.hasEnoughStock(product.getStockQty(), newQuantity)) {
            throw new InsufficientStockException(productId, product.getSku(), newQuantity, product.getStockQty());
        }

        item.setQuantity(newQuantity);
        touchCart(cart);
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO removeItem(Long cartId, Long productId) {
        Cart cart = findCartEntityOrThrow(cartId);
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(), CartStatus.OPEN, "remove item");
        }

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getProductId().equals(productId))
                .findFirst().orElseThrow(() -> new ValidationException("Product not found in cart"));

        cart.getItems().remove(item);
        item.setCart(null);
        touchCart(cart);
        return cartMapper.toDTO(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(Long cartId) {
        Cart cart = findCartEntityOrThrow(cartId);
        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(), CartStatus.OPEN, "clear");
        }
        cart.getItems().clear();
        touchCart(cart);
        cartRepository.save(cart);
    }

    public BigDecimal calculateCartTotal(Long cartId) {
        return findCartEntityOrThrow(cartId).calculateTotal();
    }

    @Transactional
    public CartDTO mergeGuestCartToUserCart(Long guestCartId, Long userId) {
        Cart guestCart = findCartEntityOrThrow(guestCartId);
        Cart userCart = findOrCreateOpenCartEntityForUser(userId);

        if (guestCart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(guestCartId, guestCart.getStatus(), CartStatus.OPEN, "merge");
        }
        if (userCart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(userCart.getCartId(), userCart.getStatus(), CartStatus.OPEN, "merge");
        }
        if (guestCart.getUser() != null) {
            throw new CartMergeException(guestCartId, userCart.getCartId(), "Guest cart already has a user assigned");
        }

        for (CartItem guestItem : new ArrayList<>(guestCart.getItems())) {
            Product product = guestItem.getProduct();
            Integer guestQuantity = guestItem.getQuantity();

            CartItem userItem = userCart.getItems().stream()
                    .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
                    .findFirst().orElse(null);

            if (userItem != null) {
                int totalQuantity = userItem.getQuantity() + guestQuantity;
                if (!CalculationUtils.hasEnoughStock(product.getStockQty(), totalQuantity)) {
                    throw new InsufficientStockException(product.getProductId(), product.getSku(),
                            totalQuantity, product.getStockQty());
                }
                userItem.setQuantity(totalQuantity);
                if (guestItem.getAddedAt().isAfter(userItem.getAddedAt())) {
                    userItem.setUnitPrice(guestItem.getUnitPrice());
                }
            } else {
                if (!CalculationUtils.hasEnoughStock(product.getStockQty(), guestQuantity)) {
                    throw new InsufficientStockException(product.getProductId(), product.getSku(),
                            guestQuantity, product.getStockQty());
                }
                CartItem newItem = CartItem.builder()
                        .cart(userCart).product(product).quantity(guestQuantity)
                        .unitPrice(guestItem.getUnitPrice()).addedAt(guestItem.getAddedAt()).build();
                userCart.getItems().add(newItem);
            }
        }

        guestCart.setStatus(CartStatus.ABANDONED);
        touchCart(guestCart);
        touchCart(userCart);
        cartRepository.save(guestCart);
        return cartMapper.toDTO(cartRepository.save(userCart));
    }

    public boolean isCartOpen(Long cartId) {
        return findCartEntityOrThrow(cartId).isOpen();
    }

    @Transactional
    public void touchCartById(Long cartId) {
        Cart cart = findCartEntityOrThrow(cartId);
        touchCart(cart);
        cartRepository.save(cart);
    }

    public Cart findCartEntityOrThrow(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart", cartId));
    }

    private Cart findOrCreateOpenCartEntityForUser(Long userId) {
        User user = userService.findUserEntityOrThrow(userId);
        return cartRepository.findByUser_UserIdAndStatus(userId, CartStatus.OPEN)
                .orElseGet(() -> cartRepository.save(Cart.builder()
                        .user(user).status(CartStatus.OPEN)
                        .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()));
    }

    private void touchCart(Cart cart) {
        cart.setUpdatedAt(LocalDateTime.now());
    }

    private UserSession resolveSession(Long sessionId) {
        if (sessionId == null) return null;
        return userSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("UserSession", sessionId));
    }
}
