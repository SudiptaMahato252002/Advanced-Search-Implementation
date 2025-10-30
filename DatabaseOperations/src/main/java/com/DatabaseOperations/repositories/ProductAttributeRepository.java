package com.DatabaseOperations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DatabaseOperations.entities.ProductAttribute;

@Repository
public interface ProductAttributeRepository extends JpaRepository<ProductAttribute,Long>{
    
}
