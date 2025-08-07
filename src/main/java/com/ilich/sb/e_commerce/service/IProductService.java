package com.ilich.sb.e_commerce.service;

import java.util.List;
import java.util.Optional;

import com.ilich.sb.e_commerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IProductService {

    public List<Product> getAll();

    public Optional<Product> getById(long id);

    public Product save(Product category);

    public Product update(long id, Product product);

    boolean delete(Long id);

    Page<Product> getAllProductsWithFilterPageable(String search, Double minPrice, Double maxPrice, Long categoryId, Pageable pageable);

}
