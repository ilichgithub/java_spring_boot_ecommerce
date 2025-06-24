package com.ilich.sb.e_commerce.payload;

import java.math.BigDecimal;

import com.ilich.sb.e_commerce.model.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price; 
    private Integer stockQuantity;
    private String imageUrl;
    private CategoryDTO categoryDTO;


    public ProductDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.imageUrl = product.getImageUrl();
        this.categoryDTO = new CategoryDTO((product.getCategory()));
    }

    

}
