package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.OrderDTO;
import co.edu.cesde.pps.enums.CartStatus;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.InsufficientStockException;
import co.edu.cesde.pps.exception.InvalidCartStateException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.mapper.OrderMapper;
import co.edu.cesde.pps.model.*;
import co.edu.cesde.pps.repository.OrderRepository;
import co.edu.cesde.pps.repository.OrderStatusRepository;
import co.edu.cesde.pps.util.CalculationUtils;
import co.edu.cesde.pps.config.AppConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderMapper orderMapper;
    private final UserService userService;
    private final CartService cartService;
    private final AddressService addressService;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final Random random;

    public OrderService(UserService userService, CartService cartService,
                       AddressService addressService, ProductService productService,
                       OrderRepository orderRepository, OrderStatusRepository orderStatusRepository) {
        this.orderMapper = new OrderMapper();
        this.userService = userService;
        this.cartService = cartService;
        this.addressService = addressService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.random = new Random();
    }

    @Transactional
    public OrderDTO checkout(Long userId, Long cartId, Long shippingAddressId,
                            Long billingAddressId) {
        User user = userService.findUserEntityOrThrow(userId);

        Cart cart = cartService.findCartEntityOrThrow(cartId);

        if (cart.getStatus() != CartStatus.OPEN) {
            throw new InvalidCartStateException(cartId, cart.getStatus(),
                CartStatus.OPEN, "checkout");
        }

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ValidationException("Cannot checkout empty cart");
        }

        if (cart.getUser() == null || !cart.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Cart does not belong to user");
        }

        Address shippingAddress = addressService.findAddressEntityOrThrow(shippingAddressId);
        Address billingAddress = addressService.findAddressEntityOrThrow(billingAddressId);

        if (!shippingAddress.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Shipping address does not belong to user");
        }
        if (!billingAddress.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Billing address does not belong to user");
        }

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();

            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new ValidationException("Product '" + product.getName() +
                    "' is no longer available");
            }

            if (!CalculationUtils.hasEnoughStock(product.getStockQty(), item.getQuantity())) {
                throw new InsufficientStockException(product.getProductId(),
                    product.getSku(), item.getQuantity(), product.getStockQty());
            }
        }

        String orderNumber = generateOrderNumber();

        OrderStatus pendingStatus = orderStatusRepository.findByNameIgnoreCase("PENDING")
                .orElseThrow(() -> new EntityNotFoundException("OrderStatus", "PENDING"));

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .orderStatus(pendingStatus)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .subtotal(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        for (CartItem cartItem : cart.getItems()) {
            BigDecimal lineTotal = CalculationUtils.calculateOrderItemLineTotal(
                    cartItem.getUnitPrice(), cartItem.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();

            order.getItems().add(orderItem);
            orderItem.setOrder(order);
        }

        List<BigDecimal> lineTotals = order.getItems().stream()
                .map(OrderItem::getLineTotal)
                .toList();

        BigDecimal subtotal = CalculationUtils.calculateOrderSubtotal(lineTotals);
        BigDecimal taxRate = BigDecimal.valueOf(AppConfig.getDefaultTaxRate());
        BigDecimal tax = CalculationUtils.calculateTax(subtotal, taxRate);
        BigDecimal shippingCost = calculateShippingCost(subtotal);
        BigDecimal total = CalculationUtils.calculateOrderTotal(subtotal, tax, shippingCost);

        order.setSubtotal(subtotal);
        order.setTax(tax);
        order.setShippingCost(shippingCost);
        order.setTotal(total);

        for (CartItem item : cart.getItems()) {
            productService.decreaseStock(item.getProduct().getProductId(),
                item.getQuantity());
        }

        cart.setStatus(CartStatus.CONVERTED);
        cart.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);

        return orderMapper.toDTO(order);
    }

    public OrderDTO findById(Long orderId) {
        Order order = findOrderEntityOrThrow(orderId);
        return orderMapper.toDTO(order);
    }

    public OrderDTO findByIdForUser(Long userId, Long orderId) {
        Order order = findOrderEntityOrThrow(orderId);
        if (order.getUser() == null || !order.getUser().getUserId().equals(userId)) {
            throw new ValidationException("Order does not belong to user");
        }
        return orderMapper.toDTO(order);
    }

    public OrderDTO findByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumberIgnoreCase(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("Order with number: " + orderNumber));

        return orderMapper.toDTO(order);
    }

    public List<OrderDTO> findOrdersByUser(Long userId) {
        userService.findUserEntityOrThrow(userId);
        return orderMapper.toDTOList(orderRepository.findByUser_UserId(userId));
    }

    public List<OrderDTO> findOrdersByStatus(Long statusId) {
        return orderMapper.toDTOList(orderRepository.findByOrderStatus_OrderStatusId(statusId));
    }

    public List<OrderDTO> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderMapper.toDTOList(orderRepository.findByCreatedAtBetween(startDate, endDate));
    }

    public String generateOrderNumber() {
        String prefix = AppConfig.getOrderNumberPrefix();
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String orderNumber;
        do {
            String randomPart = String.format("%06d", random.nextInt(1000000));
            orderNumber = prefix + date + "-" + randomPart;
        } while (orderRepository.existsByOrderNumberIgnoreCase(orderNumber));

        return orderNumber;
    }

    private BigDecimal calculateShippingCost(BigDecimal subtotal) {
        return CalculationUtils.calculateShippingCost(subtotal, 1);
    }

    public Order findOrderEntityOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId));
    }
}
