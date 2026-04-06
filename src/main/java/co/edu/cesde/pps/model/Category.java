package co.edu.cesde.pps.model;

import java.util.Objects;

/**
 * Entidad Category - Organiza el catálogo en categorías jerárquicas.
 *
 * Soporta estructura tipo árbol mediante auto-referencia (parent_id).
 * Ejemplo: "Computadores" → "Portátiles" → "Gaming"
 *
 * Campos:
 * - categoryId: Identificador único de la categoría (PK)
 * - parentId: Categoría padre (FK a Category) - NULLABLE para categorías raíz
 * - name: Nombre de la categoría
 * - slug: URL-friendly identifier (UNIQUE) - para URLs amigables
 *
 * Relaciones (futuro - etapa02):
 * - N:1 con Category (auto-referencia para jerarquía)
 * - 1:N con Category (subcategorías)
 * - 1:N con Product (productos de esta categoría)
 */
public class Category {

    private Long categoryId;
    private Long parentId; // Nullable - NULL para categorías raíz
    private String name;
    private String slug;

    // Constructor vacío (requerido para JPA futuro)
    public Category() {
    }

    // Constructor para categoría raíz (sin parent)
    public Category(String name, String slug) {
        this.parentId = null; // Categoría raíz
        this.name = name;
        this.slug = slug;
    }

    // Constructor para subcategoría (con parent)
    public Category(Long parentId, String name, String slug) {
        this.parentId = parentId;
        this.name = name;
        this.slug = slug;
    }

    // Getters y Setters

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    // Método helper para verificar si es categoría raíz
    public boolean isRootCategory() {
        return parentId == null;
    }

    // equals y hashCode basados en ID

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(categoryId, category.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryId);
    }

    // toString sin navegación a objetos relacionados (solo IDs)

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", slug='" + slug + '\'' +
                ", isRoot=" + isRootCategory() +
                '}';
    }
}
