package co.edu.cesde.pps.controller;

import co.edu.cesde.pps.dto.ProductDTO;
import co.edu.cesde.pps.exception.EntityNotFoundException;
import co.edu.cesde.pps.service.ProductService;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
@Tag(name = "Productos",
        description = "Endpoints públicos para consultar el catálogo de productos")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    @Operation(
            summary = "Listar productos activos",
            description = "Retorna todos los productos activos disponibles en el catálogo"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de productos obtenida correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error interno del servidor",
                    content = @Content
            )
    })
    @GetMapping
    public List<ProductDTO> getProducts() {
        return productService.findAllProducts();
    }
    @GetMapping("/{id}")
    public ProductDTO getProduct(@PathVariable Long id) {
        try {
            return productService.findById(id);
        } catch (EntityNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return null;
        }
    }
}
