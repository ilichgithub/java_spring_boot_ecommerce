package com.ilich.sb.e_commerce.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ilich.sb.e_commerce.model.Product;
import com.ilich.sb.e_commerce.service.IProductService;

@RestController
@RequestMapping("/api/product")
public class ProductRestController {

    @Autowired
    private IProductService iProductService;

    @GetMapping("/getAll")
    public ResponseEntity<List<Product>> getAll() {
        return new ResponseEntity<List<Product>>(iProductService.getAll(), HttpStatus.OK) ;
    }
    

}
