package com.ilich.sb.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ilich.sb.e_commerce.model.Product;

public interface IProductRepository extends JpaRepository<Product, Long> {

    

}
