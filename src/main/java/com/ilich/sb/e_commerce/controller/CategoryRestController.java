package com.ilich.sb.e_commerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilich.sb.e_commerce.model.Category;
import com.ilich.sb.e_commerce.service.ICategoryService;

import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/category")
public class CategoryRestController {

    @Autowired
    private ICategoryService iCategoryServ;

    @GetMapping("/getAll")
    public ResponseEntity<List<Category>> getAll() {
        return new ResponseEntity<List<Category>>(iCategoryServ.getAll(), HttpStatus.OK) ;
    }
    

}
