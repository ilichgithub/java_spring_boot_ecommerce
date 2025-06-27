package com.ilich.sb.e_commerce.controller;

import java.util.List;

import com.ilich.sb.e_commerce.model.Category;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
@Tag(name = "Categories", description = "Operaciones relacionadas con la gestión de Categorias") // Agrega una etiqueta para agrupar endpoints
public class CategoryRestController {
    
    private final ICategoryService iCategoryServ;
    private final ICategoryMapper categoryMapper;
    @Autowired
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
    @Operation(summary = "Obtener todos las categorias", description = "Lista todas las categorias disponibles en el catálogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias obtenida exitosamente"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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
    @Operation(summary = "Obtener categoria por ID", description = "Obtiene los detalles de una categoria específico por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "categoria encontrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "categoria no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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
    @Operation(summary = "Crear una nueva categoria", description = "Permite a un administrador crear una nueva categoria en el catálogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "categoria creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (solo ADMIN)"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Actualizar una categoria existente", description = "Permite a un administrador actualizar los detalles de una categoria por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "categoria actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (solo ADMIN)"),
            @ApiResponse(responseCode = "404", description = "categoria no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(path="{id}", consumes = { MediaType.APPLICATION_JSON_VALUE } )
    @PreAuthorize("hasRole('ADMIN')") 
    public ResponseEntity<CategoryDTO> putUpdateCategory(@PathVariable Long id, @RequestBody CategoryDTO categoryDto) {
        Category category = iCategoryServ.update(id, categoryMapper.toEntity(categoryDto));
        return new ResponseEntity<CategoryDTO>(
            categoryMapper.toDto(category),
                category == null ? HttpStatus.NOT_FOUND : HttpStatus.OK
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
    @Operation(summary = "Eliminar una categoria", description = "Permite a un administrador eliminar una categoria por su ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "categoria eliminada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (solo ADMIN)"),
            @ApiResponse(responseCode = "404", description = "categoria no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @SecurityRequirement(name = "bearerAuth")
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
