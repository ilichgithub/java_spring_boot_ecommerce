package com.ilich.sb.e_commerce.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.ilich.sb.e_commerce.model.Category;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICategoryRepository extends JpaRepository<Category,Long> {

    Optional<Category> findByName(String name);

}
