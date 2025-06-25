package com.ilich.sb.e_commerce.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilich.sb.e_commerce.mapper.IProductMapper;
import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.payload.ProductDTO;
import com.ilich.sb.e_commerce.service.IProductService;


@RestController
@RequestMapping("/api/product")
@Tag(name = "Products", description = "Operaciones relacionadas con la gestión de productos") // Agrega una etiqueta para agrupar endpoints
public class ProductRestController {

    private final IProductService iProductService;
    private final IProductMapper productMapper;

    ProductRestController(IProductService iProductService, IProductMapper productMapper){
        this.iProductService = iProductService;
        this.productMapper = productMapper;
    }

    /**
     * Obtener todos los productos.
     *
     * URL de ejemplo: GET http://localhost:8080/api/product/getAll
     *
     * @return ResponseEntity con la lista de los productos.
     */
    @Operation(summary = "Obtener todos los productos", description = "Lista todos los productos disponibles en el catálogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/getAll")
    public ResponseEntity<List<ProductDTO>> getAll() {
        return new ResponseEntity<List<ProductDTO>>(
                productMapper.toDtoList(iProductService.getAll()), 
                HttpStatus.OK
            );
    }
    /**
     * Buscar un producto por su ID.
     *
     * URL de ejemplo: GET http://localhost:8080/api/product/getById/{id}
     *
     * @param id El ID del producto a buscar.
     * @return ResponseEntity con el producto de la operación.
     */
    @Operation(summary = "Obtener producto por ID", description = "Obtiene los detalles de un producto específico por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/getById/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return iProductService.getById(id).map(
            obj -> new ResponseEntity<ProductDTO>(productMapper.toDto(obj), HttpStatus.OK))
            .orElseGet(()->new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    /**
     * Crea un producto.
     *
     * URL de ejemplo: POST http://localhost:8080/api/product/{id}
     * 
     * Json para la creacion es ProductDTO
     *
     * @return ResponseEntity con el Product de la operación.
     */

    @Operation(summary = "Crear un nuevo producto", description = "Permite a un administrador crear un nuevo producto en el catálogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (solo ADMIN)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<ProductDTO> postNewCategory(@RequestBody ProductDTO productDto) {
        return new ResponseEntity<ProductDTO>(
            productMapper.toDto(iProductService.save(productMapper.toEntity(productDto))),
            HttpStatus.CREATED
        );
        
    }
    /**
     * Actualiza un Producto por su ID.
     *
     * URL de ejemplo: PUT http://localhost:8080/api/product/{id}
     * 
     * Json para la actualizacion es ProductDTO
     *
     * @param id El ID del recurso a actualizar.
     * @return ResponseEntity con el Producto de la operación.
     */

    @Operation(summary = "Actualizar un producto existente", description = "Permite a un administrador actualizar los detalles de un producto por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (solo ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("{id}")
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<ProductDTO> putUpdateCategory(@PathVariable Long id, @RequestBody ProductDTO productDto) {
        Product product = new Product(productDto);
        return new ResponseEntity<ProductDTO>(
            productMapper.toDto(iProductService.update(id, product)),
            HttpStatus.OK
        );
    }
    /**
     * Elimina una categoria por su ID.
     *
     * URL de ejemplo: DELETE http://localhost:8080/api/product/{id}
     *
     * @param id El ID del recurso a eliminar.
     * @return ResponseEntity con el estado de la operación.
     */

    @Operation(summary = "Eliminar un producto", description = "Permite a un administrador eliminar un producto por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (solo ADMIN)"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}") 
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (iProductService.delete(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    

}
