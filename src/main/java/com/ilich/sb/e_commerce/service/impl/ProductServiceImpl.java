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
    public Product update(long id, Product product) {
        Product pro = iProductRepository.findById(id).orElseThrow(()-> new RuntimeException("No existe id: "+id));
        return iProductRepository.save(product);
    }

    @Override
    public boolean delete(long id) {
        if (iProductRepository.existsById(id)) {
            iProductRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
