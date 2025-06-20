package com.ilich.sb.e_commerce.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.ilich.sb.e_commerce.model.Category;

public interface ICategoryRepository extends JpaRepository<Category,Long> {

}
