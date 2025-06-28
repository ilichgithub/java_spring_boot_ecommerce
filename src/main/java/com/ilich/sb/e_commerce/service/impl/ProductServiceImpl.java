package com.ilich.sb.e_commerce.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.ilich.sb.e_commerce.model.Category;
import com.ilich.sb.e_commerce.repository.ICategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.repository.IProductRepository;
import com.ilich.sb.e_commerce.service.IProductService;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private IProductRepository iProductRepository;

    @Autowired
    private ICategoryRepository iCategoryRepository;

    @Override
    public List<Product> getAll() {
        return iProductRepository.findAll();
    }

    @Override
    public Optional<Product> getById(long id) {
        return iProductRepository.findById(id);
    }

    @Override
    public Product save(Product product) {
        // Lógica de negocio: asegura que la categoría existe si se proporciona
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = iCategoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + product.getCategory().getId()));
            product.setCategory(category);
        } else if (product.getCategory() != null && product.getCategory().getName() != null) {
            // Opcional: buscar categoría por nombre si no se da ID
            Category category = iCategoryRepository.findByName(product.getCategory().getName())
                    .orElseThrow(() -> new RuntimeException("Category not found with name: " + product.getCategory().getName()));
            product.setCategory(category);
        } else {
            // Si no se proporciona categoría, el producto se crea sin ella
            product.setCategory(null);
        }

        // Lógica de negocio: Validar precio y stock
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price cannot be negative.");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative.");
        }

        return iProductRepository.save(product);
    }

    @Override
    public Product update(long id, Product productDetails) {
        Product product = iProductRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());

        // Lógica de negocio para actualizar categoría
        if (productDetails.getCategory() != null && productDetails.getCategory().getId() != null) {
            Category category = iCategoryRepository.findById(productDetails.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + productDetails.getCategory().getId()));
            product.setCategory(category);
        } else if (productDetails.getCategory() == null) {
            product.setCategory(null); // Permite desasociar categoría
        }
        // No manejamos el caso de actualizar categoría por nombre en el update, solo por ID o a null.

        // Validaciones similares a create
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price cannot be negative.");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Product stock cannot be negative.");
        }

        return iProductRepository.save(product);
    }

    @Override
    public boolean delete(Long id) {
        // Es muy probable que aquí estés usando existsById() antes de findById()
        if (!iProductRepository.existsById(id)) { // <-- ¡Esta es la llamada que se está haciendo!
            throw new RuntimeException("Product not found with id: " + id);
        }
        // Si el producto existe, entonces podrías hacer un findById para obtenerlo
        // o simplemente llamar a deleteById si tu repositorio lo soporta
        iProductRepository.deleteById(id); // O productRepository.delete(productRepository.findById(id).get());
        return true;
    }

}
