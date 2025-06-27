package com.ilich.sb.e_commerce.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ilich.sb.e_commerce.model.Category;
import com.ilich.sb.e_commerce.repository.ICategoryRepository;
import com.ilich.sb.e_commerce.service.ICategoryService;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private final ICategoryRepository iCategoryRepository;

    @Autowired
    public CategoryServiceImpl(ICategoryRepository iCategoryRepository) {
        this.iCategoryRepository = iCategoryRepository;
    }

    @Override
    public List<Category> getAll() {
        return iCategoryRepository.findAll();
    }

    @Override
    public Optional<Category> getById(long id) {
        return iCategoryRepository.findById(id);
    }

    @Override
    public Category save(Category category) {
        return iCategoryRepository.save(category);
    }

    @Override
    public Category update(long id, Category category) {
        Optional<Category> cat = iCategoryRepository.findById(id);
            //.orElseThrow(()-> new RuntimeException("No existe id: "+id));
        return cat.isPresent() ? iCategoryRepository.save(category) : null;
    }


    @Override
    public boolean delete(long id) {
        if (iCategoryRepository.existsById(id)) {
            iCategoryRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(Long id) {
        return iCategoryRepository.existsById(id);
    }

}
