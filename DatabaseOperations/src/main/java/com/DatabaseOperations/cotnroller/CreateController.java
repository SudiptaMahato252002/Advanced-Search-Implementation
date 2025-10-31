package com.DatabaseOperations.cotnroller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.DatabaseOperations.dtos.BrandDto;
import com.DatabaseOperations.dtos.CategoryDto;
import com.DatabaseOperations.dtos.ProductDto;
import com.DatabaseOperations.requests.ProductRequest;
import com.DatabaseOperations.services.CreatingService;

@RestController
@RequestMapping("/api/create/")
public class CreateController 
{
    @Autowired
    private CreatingService creatingService;
    

    @PostMapping("/brands")
    public ResponseEntity<BrandDto> createBrand(@RequestBody BrandDto brandDto) {
        // Call service to save brand
        BrandDto savedBrand = creatingService.createBrand(brandDto);

        // Return 201 Created with the saved brand DTO
        return new ResponseEntity<>(savedBrand,HttpStatus.CREATED);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto CategoryDto) {
        // Call service to save category
        CategoryDto savedCategory = creatingService.createCategory(CategoryDto);

        // Return 201 Created with the saved category DTO
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductRequest req)
    {
        ProductDto productDto=creatingService.creaetProduct(req);
        return new ResponseEntity<ProductDto>(productDto, HttpStatus.CREATED);
    }
}
