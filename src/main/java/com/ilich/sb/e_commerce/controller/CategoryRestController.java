package com.ilich.sb.e_commerce.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilich.sb.e_commerce.mapper.ICategoryMapper;
import com.ilich.sb.e_commerce.payload.CategoryDTO;
import com.ilich.sb.e_commerce.service.ICategoryService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/category")
public class CategoryRestController {
    
    private final ICategoryService iCategoryServ;
    private final ICategoryMapper categoryMapper;

    CategoryRestController(ICategoryService iCategoryServ, ICategoryMapper categoryMapper){
        this.iCategoryServ = iCategoryServ;
        this.categoryMapper = categoryMapper;
    }


    /**
     * Obtener todas las categorias.
     *
     * URL de ejemplo: GET http://localhost:8080/api/category/getAll
     *
     * @return ResponseEntity con la lista de categorias.
     */
    @GetMapping(path = "/getAll", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return new ResponseEntity<List<CategoryDTO>>(
                categoryMapper.toDtoList(iCategoryServ.getAll()), 
                HttpStatus.OK
            );
    }
    /**
     * Buscar una categoria por su ID.
     *
     * URL de ejemplo: GET http://localhost:8080/api/category/getById/{id}
     *
     * @param id El ID del recurso a buscar.
     * @return ResponseEntity con la categoria de la operación.
     */
    @GetMapping(path="/getById/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<CategoryDTO> getById(@PathVariable Long id) {
        return iCategoryServ.getById(id)
            .map(obj -> new ResponseEntity<CategoryDTO>(categoryMapper.toDto(obj), HttpStatus.OK))
            .orElseGet(()->new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    /**
     * Crea una categoria.
     *
     * URL de ejemplo: POST http://localhost:8080/api/category/{id}
     * 
     * Json para la creacion es CategoryDTO
     *
     * @return ResponseEntity con la categoria de la operación.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<CategoryDTO> postNewCategory(@RequestBody CategoryDTO categoryDto) {
        return new ResponseEntity<CategoryDTO>(
            categoryMapper.toDto(iCategoryServ.save(categoryMapper.toEntity(categoryDto))),
            HttpStatus.CREATED
        );
    }
    /**
     * Actualiza una categoria por su ID.
     *
     * URL de ejemplo: PUT http://localhost:8080/api/category/{id}
     * 
     * Json para la actualizacion es CategoryDTO
     *
     * @param id El ID del recurso a actualizar.
     * @return ResponseEntity con la categoria de la operación.
     */
    @PutMapping(path="{id}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<CategoryDTO> putUpdateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDto) {
        return new ResponseEntity<CategoryDTO>(
            categoryMapper.toDto(iCategoryServ.update(
                id,categoryMapper.toEntity(categoryDto))),
                HttpStatus.OK
            );
    }
    /**
     * Elimina una categoria por su ID.
     *
     * URL de ejemplo: DELETE http://localhost:8080/api/category/{id}
     *
     * @param id El ID del recurso a eliminar.
     * @return ResponseEntity con el estado de la operación.
     */
    @DeleteMapping("/{id}") 
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (iCategoryServ.delete(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    

}
