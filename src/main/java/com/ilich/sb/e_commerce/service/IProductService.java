package com.ilich.sb.e_commerce.service;

import java.util.List;
import java.util.Optional;

import com.ilich.sb.e_commerce.model.Product;

public interface IProductService {

    public List<Product> getAll();

    public Optional<Product> getById(long id);

    public Product save(Product category);

    public Product update(long id, Product product);

    public boolean delete(long id);

}
