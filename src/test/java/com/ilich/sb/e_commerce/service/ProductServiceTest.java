package com.ilich.sb.e_commerce.service;

import com.ilich.sb.e_commerce.model.Category;
import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.repository.ICategoryRepository;
import com.ilich.sb.e_commerce.repository.IProductRepository;
import com.ilich.sb.e_commerce.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*; // Importa los métodos estáticos de Mockito

@ExtendWith(MockitoExtension.class) // Habilita la integración de Mockito con JUnit 5
public class ProductServiceTest {

    @Mock // Crea un mock de ProductRepository
    private IProductRepository productRepository;

    @Mock // Crea un mock de CategoryRepository
    private ICategoryRepository categoryRepository;

    @InjectMocks // Inyecta los mocks en una instancia real de ProductService
    private ProductServiceImpl productService;

    // Categorías de prueba
    private Category electronicsCategory;
    private Category booksCategory;

    // Productos de prueba
    private Product laptop;
    private Product smartphone;
    private Product novel;

    @BeforeEach // Se ejecuta antes de cada método de prueba
    void setUp() {
        // Inicializar categorías
        electronicsCategory = new Category(1L, "Electronics");
        booksCategory = new Category(2L, "Books");

        // Inicializar productos
        laptop = new Product("Laptop Pro", "Powerful laptop", new BigDecimal("1200.00"), 10, electronicsCategory);
        laptop.setId(101L);
        smartphone = new Product("Smartphone X", "Latest smartphone", new BigDecimal("800.00"), 25, electronicsCategory);
        smartphone.setId(102L);
        novel = new Product("Great Novel", "Classic literature", new BigDecimal("25.00"), 50, booksCategory);
        novel.setId(103L);
    }

    @Test
    void testGetAllProducts() {
        // Simular el comportamiento del mock: cuando se llame a findAll(), devuelve esta lista
        when(productRepository.findAll()).thenReturn(Arrays.asList(laptop, smartphone, novel));

        List<Product> products = productService.getAll();

        assertNotNull(products);
        assertEquals(3, products.size());
        assertTrue(products.contains(laptop));
        assertTrue(products.contains(smartphone));
        assertTrue(products.contains(novel));

        // Verificar que el método findAll() del repositorio fue llamado exactamente una vez
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById_Found() {
        when(productRepository.findById(101L)).thenReturn(Optional.of(laptop));

        Optional<Product> foundProduct = productService.getById(101L);

        assertTrue(foundProduct.isPresent());
        assertEquals(laptop.getName(), foundProduct.get().getName());
        verify(productRepository, times(1)).findById(101L);
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> foundProduct = productService.getById(999L);

        assertFalse(foundProduct.isPresent());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void testCreateProduct_SuccessWithExistingCategory() {
        // Creamos una categoría "dummy" solo con el ID, para que el servicio la busque
        Category categoryWithIdOnly = new Category();
        categoryWithIdOnly.setId(electronicsCategory.getId()); // ID 1L

        // Creamos el producto usando esta categoría con solo el ID
        Product newProduct = new Product("Tablet", "Portable device", new BigDecimal("400.00"), 15, categoryWithIdOnly);
        newProduct.setId(104L); // ID para el producto, no la categoría

        // Simular que el repositorio de categorías encuentra la categoría COMPLETA cuando se le pide por ID
        // Es decir, cuando categoryRepository.findById(1L) sea llamado, Mockito devolverá electronicsCategory (con nombre, etc.)
        when(categoryRepository.findById(electronicsCategory.getId())).thenReturn(Optional.of(electronicsCategory));

        // Simular el guardado del producto (el servicio lo guardará con la categoría COMPLETA)
        when(productRepository.save(any(Product.class))).thenReturn(newProduct); // newProduct ahora tendrá la categoría completa

        Product createdProduct = productService.save(newProduct);

        assertNotNull(createdProduct);
        assertEquals("Tablet", createdProduct.getName());
        // Asegúrate de que la categoría asociada al producto creado es la que se "encontró" (electronicsCategory)
        assertEquals(electronicsCategory, createdProduct.getCategory());

        // ¡Ahora esta verificación debería pasar!
        verify(categoryRepository, times(1)).findById(electronicsCategory.getId());
        verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    void testCreateProduct_SuccessWithoutCategory() {
        Product newProduct = new Product("Mouse", "Computer accessory", new BigDecimal("25.00"), 100, null);
        newProduct.setId(105L);

        when(productRepository.save(any(Product.class))).thenReturn(newProduct);

        Product createdProduct = productService.save(newProduct);

        assertNotNull(createdProduct);
        assertEquals("Mouse", createdProduct.getName());
        assertNull(createdProduct.getCategory()); // No se asignó categoría
        verify(productRepository, times(1)).save(newProduct);
        verify(categoryRepository, never()).findById(anyLong()); // Verificar que no se intentó buscar categoría
    }

    @Test
    void testCreateProduct_NegativePriceThrowsException() {
        Product invalidProduct = new Product("Bad Price", "Negative price product", new BigDecimal("-10.00"), 10, null);

        // Esperar que se lance una excepción
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productService.save(invalidProduct);
        });

        assertEquals("Product price cannot be negative.", thrown.getMessage());
        verify(productRepository, never()).save(any(Product.class)); // Verificar que no se guardó
    }

    @Test
    void testUpdateProduct_Success() {
        // Simular que el producto a actualizar existe
        when(productRepository.findById(laptop.getId())).thenReturn(Optional.of(laptop));
        // Simular que la categoría existe (si se actualiza)
        when(categoryRepository.findById(booksCategory.getId())).thenReturn(Optional.of(booksCategory));
        // Simular el guardado del producto actualizado
        when(productRepository.save(any(Product.class))).thenReturn(laptop); // laptop ahora tendrá los nuevos detalles

        Product updatedDetails = new Product("Updated Laptop", "New description", new BigDecimal("1300.00"), 8, booksCategory);
        updatedDetails.setId(laptop.getId());

        Product updatedProduct = productService.update(laptop.getId(), updatedDetails);

        assertNotNull(updatedProduct);
        assertEquals("Updated Laptop", updatedProduct.getName());
        assertEquals(new BigDecimal("1300.00"), updatedProduct.getPrice());
        assertEquals(8, updatedProduct.getStockQuantity());
        assertEquals(booksCategory, updatedProduct.getCategory());
        verify(productRepository, times(1)).findById(laptop.getId());
        verify(categoryRepository, times(1)).findById(booksCategory.getId());
        verify(productRepository, times(1)).save(laptop);
    }

    @Test
    void testUpdateProduct_ProductNotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Product productDetails = new Product("Non Existent", "", BigDecimal.ONE, 1, null);
        productDetails.setId(999L);

        // Esperar que se lance una excepción
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            productService.update(999L, productDetails);
        });

        assertEquals("Product not found with id: 999", thrown.getMessage());
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() {
        // Aquí es CRÍTICO que 'laptop' sea el mismo objeto (con el mismo ID) que el que se mockea
        // y el que se pasa al servicio.

        // 1. Simula que findById(101L) devuelve el 'laptop' creado en setUp
        //when(productRepository.findById(laptop.getId())).thenReturn(Optional.of(laptop));
        when(productRepository.existsById(laptop.getId())).thenReturn(true);

        // 2. Simula que productRepository.delete() recibe el objeto 'laptop'
        // Si tu deleteProduct() en el servicio usa 'productRepository.deleteById(id)'
        // en lugar de 'productRepository.delete(product)', el mock debería cambiar a:
        doNothing().when(productRepository).deleteById(laptop.getId());
        //doNothing().when(productRepository).delete(laptop); // Esto asume que tu servicio pasa el objeto completo a delete

        // 3. Llama al servicio con el ID del producto
        productService.delete(laptop.getId()); // Pasa el ID

        // 4. Verifica las interacciones
        //verify(productRepository, times(1)).findById(laptop.getId());
        verify(productRepository, times(1)).deleteById(laptop.getId());
    }
    @Test
    void testDeleteProduct_ProductNotFound() {
        // **CAMBIO AQUÍ:** Simula existsById(), no findById()
        // Cuando se llame a productRepository.existsById(999L), debe devolver 'false'
        when(productRepository.existsById(999L)).thenReturn(false);

        // Ejecuta el método del servicio y espera que lance una RuntimeException
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            productService.delete(999L);
        });

        // Verifica que el mensaje de la excepción sea el esperado
        assertEquals("Product not found with id: 999", thrown.getMessage());

        // **VERIFICACIONES CORREGIDAS:**
        // 1. Verifica que existsById() fue llamado exactamente una vez con el ID 999L
        verify(productRepository, times(1)).existsById(999L);
        // 2. Verifica que el método delete NUNCA fue llamado en el repositorio
        verify(productRepository, never()).delete(any(Product.class));
        // 3. Opcional: Si tu servicio no hace findById(), verifica que nunca se llamó.
        verify(productRepository, never()).findById(anyLong()); // Para ser explícitos
    }
    /*
    @Test
    void testGetProductsByCategory() {
        when(productRepository.findByCategoryName("Electronics")).thenReturn(Arrays.asList(laptop, smartphone));

        List<Product> products = productService.getProductsByCategory("Electronics");

        assertNotNull(products);
        assertEquals(2, products.size());
        assertTrue(products.contains(laptop));
        assertTrue(products.contains(smartphone));
        verify(productRepository, times(1)).findByCategoryName("Electronics");
    }
    @Test
    void testGetProductsByPriceRange_Success() {
        when(productRepository.findByPriceBetween(new BigDecimal("500.00"), new BigDecimal("1500.00")))
                .thenReturn(Arrays.asList(laptop, smartphone));

        List<Product> products = productService.getProductsByPriceRange(new BigDecimal("500.00"), new BigDecimal("1500.00"));

        assertNotNull(products);
        assertEquals(2, products.size());
        assertTrue(products.contains(laptop));
        assertTrue(products.contains(smartphone));
        verify(productRepository, times(1)).findByPriceBetween(new BigDecimal("500.00"), new BigDecimal("1500.00"));
    }

    @Test
    void testGetProductsByPriceRange_NegativeMinPriceThrowsException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductsByPriceRange(new BigDecimal("-10.00"), new BigDecimal("100.00"));
        });
        assertEquals("Price range values cannot be negative.", thrown.getMessage());
        verify(productRepository, never()).findByPriceBetween(any(), any());
    }

    @Test
    void testGetProductsByPriceRange_MinGreaterThanMaxThrowsException() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productService.getProductsByPriceRange(new BigDecimal("100.00"), new BigDecimal("10.00"));
        });
        assertEquals("Minimum price cannot be greater than maximum price.", thrown.getMessage());
        verify(productRepository, never()).findByPriceBetween(any(), any());
    }

    @Test
    void testDecrementStock_Success() {
        when(productRepository.findById(laptop.getId())).thenReturn(Optional.of(laptop));
        when(productRepository.save(any(Product.class))).thenReturn(laptop); // Simula el guardado

        int initialStock = laptop.getStock();
        int quantityToDecrement = 2;

        Product updatedProduct = productService.decrementStock(laptop.getId(), quantityToDecrement);

        assertNotNull(updatedProduct);
        assertEquals(initialStock - quantityToDecrement, updatedProduct.getStock());
        verify(productRepository, times(1)).findById(laptop.getId());
        verify(productRepository, times(1)).save(laptop);
    }

    @Test
    void testDecrementStock_InsufficientStockThrowsException() {
        when(productRepository.findById(laptop.getId())).thenReturn(Optional.of(laptop)); // laptop tiene 10 de stock

        int quantityToDecrement = 15; // Más de lo que hay en stock

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            productService.decrementStock(laptop.getId(), quantityToDecrement);
        });

        assertEquals("Not enough stock for product: Laptop Pro", thrown.getMessage());
        verify(productRepository, times(1)).findById(laptop.getId());
        verify(productRepository, never()).save(any(Product.class)); // No debería guardarse si falla
    }
    */
}