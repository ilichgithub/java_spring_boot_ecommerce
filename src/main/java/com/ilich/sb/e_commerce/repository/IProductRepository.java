package com.ilich.sb.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilich.sb.e_commerce.model.Product;

import java.math.BigDecimal;
import java.util.List;

public interface IProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryName(String categoryName);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

}
