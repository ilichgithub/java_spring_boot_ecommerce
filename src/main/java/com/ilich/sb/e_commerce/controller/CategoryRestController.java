package com.ilich.sb.e_commerce.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilich.sb.e_commerce.dto.CategoryDTO;
import com.ilich.sb.e_commerce.model.Category;
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

    @Autowired
    private ICategoryService iCategoryServ;

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
            iCategoryServ.getAll().stream()
                .map(c -> new CategoryDTO(c)).collect(Collectors.toList()), 
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
            .map(obj -> new ResponseEntity<CategoryDTO>(new CategoryDTO(obj), HttpStatus.OK))
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
    public ResponseEntity<CategoryDTO> postNewCategory(@RequestBody CategoryDTO categoryDto) {
        return new ResponseEntity<CategoryDTO>(
            new CategoryDTO(iCategoryServ.save(new Category(categoryDto))),
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
    public ResponseEntity<CategoryDTO> putUpdateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDto) {
        return new ResponseEntity<CategoryDTO>(
            new CategoryDTO(iCategoryServ.update(
                id,new Category(categoryDto))),
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
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (iCategoryServ.delete(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    

}
