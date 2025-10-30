package com.DatabaseOperations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DatabaseOperations.entities.Product;

public interface ProductRepository extends JpaRepository<Product,Long> {
    
}
