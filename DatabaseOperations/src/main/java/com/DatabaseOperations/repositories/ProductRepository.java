package com.DatabaseOperations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DatabaseOperations.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    
}
