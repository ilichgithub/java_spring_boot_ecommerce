package com.ilich.sb.e_commerce.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.repository.IProductRepository;
import com.ilich.sb.e_commerce.service.IProductService;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private IProductRepository iProductRepository;

    @Override
    public List<Product> getAll() {
        return iProductRepository.findAll();
    }

    @Override
    public Optional<Product> getById(long id) {
        return iProductRepository.findById(id);
    }

    @Override
    public Product save(Product category) {
        return iProductRepository.save(category);
    }

    @Override
    public void delete(long id) {
        iProductRepository.deleteById(id);
    }

}
