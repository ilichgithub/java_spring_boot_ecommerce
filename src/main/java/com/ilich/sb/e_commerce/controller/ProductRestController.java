package com.ilich.sb.e_commerce.controller;

import java.util.List;

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

import com.ilich.sb.e_commerce.dto.ProductDTO;
import com.ilich.sb.e_commerce.mapper.IProductMapper;
import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.service.IProductService;


@RestController
@RequestMapping("/api/product")
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
