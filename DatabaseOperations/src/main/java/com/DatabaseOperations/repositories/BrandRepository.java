package com.DatabaseOperations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DatabaseOperations.entities.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand,Long> {
    
}
