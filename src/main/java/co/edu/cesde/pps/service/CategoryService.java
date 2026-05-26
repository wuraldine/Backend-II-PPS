package co.edu.cesde.pps.service;

import co.edu.cesde.pps.dto.CategoryDTO;
import co.edu.cesde.pps.exception.DuplicateEntityException;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.exception.ValidationException;
import co.edu.cesde.pps.mapper.CategoryMapper;
import co.edu.cesde.pps.model.Category;
import co.edu.cesde.pps.repository.CategoryRepository;
import co.edu.cesde.pps.util.StringUtils;
import co.edu.cesde.pps.util.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryMapper = new CategoryMapper();
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        ValidationUtils.validateNotBlank(categoryDTO.getName(), "name");

        String slug = categoryDTO.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = StringUtils.slugify(categoryDTO.getName());
        }

        if (existsBySlug(slug)) {
            throw new DuplicateEntityException("Category", "slug", slug);
        }

        Category category = categoryMapper.toEntity(categoryDTO);
        category.setSlug(slug);

        if (categoryDTO.getParentId() != null) {
            Category parent = findCategoryEntityOrThrow(categoryDTO.getParentId());
            category.setParent(parent);
        }

        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Category category = findCategoryEntityOrThrow(categoryId);
        ValidationUtils.validateNotBlank(categoryDTO.getName(), "name");

        String newSlug = categoryDTO.getSlug();
        if (newSlug == null || newSlug.isBlank()) {
            newSlug = StringUtils.slugify(categoryDTO.getName());
        }

        if (!category.getSlug().equals(newSlug) && existsBySlug(newSlug)) {
            throw new DuplicateEntityException("Category", "slug", newSlug);
        }

        category.setName(categoryDTO.getName());
        category.setSlug(newSlug);

        if (categoryDTO.getParentId() != null) {
            if (categoryDTO.getParentId().equals(categoryId)) {
                throw new ValidationException("Category cannot be its own parent");
            }
            Category newParent = findCategoryEntityOrThrow(categoryDTO.getParentId());
            if (wouldCreateCycle(category, newParent)) {
                throw new ValidationException("Cannot create cycle in category hierarchy");
            }
            category.setParent(newParent);
        } else {
            category.setParent(null);
        }

        return categoryMapper.toDTO(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = findCategoryEntityOrThrow(categoryId);

        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            throw new ValidationException("Cannot delete category with subcategories");
        }
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new ValidationException("Cannot delete category with products");
        }

        categoryRepository.delete(category);
    }

    public CategoryDTO findById(Long categoryId) {
        return categoryMapper.toDTO(findCategoryEntityOrThrow(categoryId));
    }

    public CategoryDTO findBySlug(String slug) {
        Category category = categoryRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException("Category with slug: " + slug));
        return categoryMapper.toDTO(category);
    }

    public List<CategoryDTO> findAllCategories() {
        return categoryMapper.toDTOList(categoryRepository.findAll());
    }

    public List<CategoryDTO> findRootCategories() {
        return categoryMapper.toDTOList(categoryRepository.findByParentIsNull());
    }

    public List<CategoryDTO> findSubcategories(Long parentId) {
        findCategoryEntityOrThrow(parentId);
        return categoryMapper.toDTOList(categoryRepository.findByParent_CategoryId(parentId));
    }

    @Transactional
    public CategoryDTO addSubcategory(Long parentId, CategoryDTO subcategoryDTO) {
        Category parent = findCategoryEntityOrThrow(parentId);
        ValidationUtils.validateNotBlank(subcategoryDTO.getName(), "name");

        String slug = subcategoryDTO.getSlug();
        if (slug == null || slug.isBlank()) {
            slug = StringUtils.slugify(subcategoryDTO.getName());
        }
        if (existsBySlug(slug)) {
            throw new DuplicateEntityException("Category", "slug", slug);
        }

        Category subcategory = categoryMapper.toEntity(subcategoryDTO);
        subcategory.setSlug(slug);
        subcategory.setParent(parent);

        return categoryMapper.toDTO(categoryRepository.save(subcategory));
    }

    @Transactional
    public void removeSubcategory(Long parentId, Long subcategoryId) {
        findCategoryEntityOrThrow(parentId);
        Category subcategory = findCategoryEntityOrThrow(subcategoryId);

        if (subcategory.getParent() == null ||
            !subcategory.getParent().getCategoryId().equals(parentId)) {
            throw new ValidationException("Category is not a subcategory of specified parent");
        }

        subcategory.setParent(null);
        categoryRepository.save(subcategory);
    }

    public CategoryDTO buildCategoryTree(Long categoryId) {
        Category category = findCategoryEntityOrThrow(categoryId);
        return categoryMapper.toDTOWithHierarchy(category);
    }

    public List<CategoryDTO> buildFullCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return categoryMapper.toDTOListWithHierarchy(rootCategories);
    }

    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlugIgnoreCase(slug);
    }

    public Category findCategoryEntityOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category", categoryId));
    }

    private boolean wouldCreateCycle(Category category, Category newParent) {
        Category current = newParent;
        while (current != null) {
            if (current.getCategoryId().equals(category.getCategoryId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
