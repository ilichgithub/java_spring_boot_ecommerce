package com.ilich.sb.e_commerce.service;

import java.util.List;
import java.util.Optional;

import com.ilich.sb.e_commerce.model.Category;

public interface ICategoryService {

    public List<Category> getAll();

    public Optional<Category> getById(long id);

    public Category save(Category category);

    public void delete(long id);

}
