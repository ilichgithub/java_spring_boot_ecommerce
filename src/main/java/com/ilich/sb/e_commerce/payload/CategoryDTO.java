package com.ilich.sb.e_commerce.payload;

import com.ilich.sb.e_commerce.model.Category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    
    private Long id;
    private String name;
    private String description;

    public CategoryDTO(Category cat) {
        this.id = cat.getId();
        this.name = cat.getName();
        this.description = cat.getDescription();
    }

    


}
