package com.DatabaseOperations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DatabaseOperations.entities.ProductVariant;

@Repository
public interface ProductVariantRepsitory extends JpaRepository<ProductVariant,Long>{
    
}
