package com.ilich.sb.e_commerce.service;

import com.ilich.sb.e_commerce.model.Category;
import com.ilich.sb.e_commerce.repository.ICategoryRepository;
import com.ilich.sb.e_commerce.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks; // Para inyectar mocks en la clase a probar
import org.mockito.Mock;     // Para crear objetos mock
import org.mockito.junit.jupiter.MockitoExtension; // Extensión para usar Mockito

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*; // Para aserciones
import static org.mockito.Mockito.*;             // Para métodos de Mockito (when, verify)

@ExtendWith(MockitoExtension.class) // Habilita Mockito para JUnit 5
public class CategoryServiceTest {

    @Mock // Crea un mock del CategoryRepository
    private ICategoryRepository categoryRepository;

    @InjectMocks // Inyecta los mocks (categoryRepository) en esta instancia de CategoryService
    private CategoryServiceImpl categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach // Se ejecuta antes de cada método de prueba
    void setUp() {
        category1 = new Category();
        category1.setId(1L);
        category1.setName("Electronics");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Books");
    }

    @Test
    void testFindAllCategories() {
        // Define el comportamiento del mock: cuando se llama a findAll(), devuelve esta lista
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        List<Category> categories = categoryService.getAll();

        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertEquals("Electronics", categories.get(0).getName());
        assertEquals("Books", categories.get(1).getName());

        // Verifica que el método findAll() del repositorio fue llamado exactamente una vez
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testFindCategoryByIdFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category1));

        Optional<Category> foundCategory = categoryService.getById(1L);

        assertTrue(foundCategory.isPresent());
        assertEquals("Electronics", foundCategory.get().getName());
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void testFindCategoryByIdNotFound() {
        when(categoryRepository.findById(3L)).thenReturn(Optional.empty());

        Optional<Category> foundCategory = categoryService.getById(3L);

        assertFalse(foundCategory.isPresent());
        verify(categoryRepository, times(1)).findById(3L);
    }

    @Test
    void testSaveCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category1);

        Category savedCategory = categoryService.save(new Category("Electronics"));

        assertNotNull(savedCategory);
        assertEquals("Electronics", savedCategory.getName());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testDeleteCategoryById() {
        // No necesitamos que existsById() devuelva true para esta prueba,
        // ya que el servicio solo llama a deleteById si existe.
        // Pero si quisiéramos probar la rama de "no encontrado", simularíamos existsById(false)
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L); // Define que deleteById() no haga nada

        categoryService.delete(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void testExistsById() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        assertTrue(categoryService.existsById(1L));
        verify(categoryRepository, times(1)).existsById(1L);

        when(categoryRepository.existsById(99L)).thenReturn(false);
        assertFalse(categoryService.existsById(99L));
        verify(categoryRepository, times(1)).existsById(99L);
    }
}