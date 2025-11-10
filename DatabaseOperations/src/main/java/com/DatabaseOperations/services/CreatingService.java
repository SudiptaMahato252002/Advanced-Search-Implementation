package com.DatabaseOperations.services;


import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.DatabaseOperations.dtos.BrandDto;
import com.DatabaseOperations.dtos.CategoryDto;
import com.DatabaseOperations.dtos.ProductDto;
import com.DatabaseOperations.entities.Brand;
import com.DatabaseOperations.entities.Category;
import com.DatabaseOperations.entities.Product;
import com.DatabaseOperations.entities.ProductAttribute;
import com.DatabaseOperations.entities.ProductVariant;
import com.DatabaseOperations.mappers.ProductVariantMapper;
import com.DatabaseOperations.repositories.BrandRepository;
import com.DatabaseOperations.repositories.CategoryRepository;
import com.DatabaseOperations.repositories.ProductAttributeRepository;
import com.DatabaseOperations.repositories.ProductRepository;
import com.DatabaseOperations.repositories.ProductVariantRepsitory;
import com.DatabaseOperations.requests.ProductRequest;

@Service
public class CreatingService 
{
    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private CategoryRepository categoryRepo;
    @Autowired
    private BrandRepository brandRepo;
    @Autowired
    private ProductAttributeRepository productAttributeRepo;
    @Autowired
    private ProductVariantRepsitory productVariantRepo;

    @Autowired
    private ProductVariantMapper mapper;

    public BrandDto createBrand(BrandDto brandDto)
    {
         Brand brand = Brand.builder()
                .name(brandDto.getName())
                .slug(brandDto.getSlug()) // optional, will auto-generate if null
                .description(brandDto.getDescription())
                .isPopular(brandDto.getIsPopular() != null ? brandDto.getIsPopular() : false)
                .displayOrder(brandDto.getDisplayOrder() != null ? brandDto.getDisplayOrder() : 0)
                .isActive(brandDto.getIsActive() != null ? brandDto.getIsActive() : true)
                .productCount(brandDto.getProductCount() != null ? brandDto.getProductCount() : 0)
                .averageRating(brandDto.getAverageRating() != null ? brandDto.getAverageRating() : 0.0)
                .country(brandDto.getCountry())
                .foundedYear(brandDto.getFoundedYear())
                .build();

                Brand savedBrand = brandRepo.save(brand);

        // Convert saved entity back to DTO
        BrandDto savedBrandDto = BrandDto.builder()
                .id(savedBrand.getId())
                .name(savedBrand.getName())
                .slug(savedBrand.getSlug())
                .description(savedBrand.getDescription())
                .isPopular(savedBrand.getIsPopular())
                .displayOrder(savedBrand.getDisplayOrder())
                .isActive(savedBrand.getIsActive())
                .productCount(savedBrand.getProductCount())
                .averageRating(savedBrand.getAverageRating())
                .country(savedBrand.getCountry())
                .foundedYear(savedBrand.getFoundedYear())
                .build();

        return savedBrandDto;

    }

 
    public CategoryDto createCategory(CategoryDto categoryDto)   
    {
        // 1️⃣ Convert DTO to Entity
        Category category = Category.builder()
                .name(categoryDto.getName())
                .slug(categoryDto.getSlug())
                .description(categoryDto.getDescription())
                .level(0) // default, will update if parent exists
                .productCount(categoryDto.getProductCount() != null ? categoryDto.getProductCount() : 0)
                .build();

        // 2️⃣ Handle parent category if parentId is provided
        if (categoryDto.getParentId() != null) {
            Category parent = categoryRepo.findById(categoryDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found with ID: " + categoryDto.getParentId()));
            parent.addSubCategory(category); // sets parentCategory, level, and fullPath
        } else {
            category.updateFullPath(); // root category
        }

        // 3️⃣ Save entity
        Category savedCategory = categoryRepo.save(category);

        // 4️⃣ Convert back to DTO
        return CategoryDto.builder()
                .id(savedCategory.getId())
                .name(savedCategory.getName())
                .slug(savedCategory.getSlug())
                .description(savedCategory.getDescription())
                .level(savedCategory.getLevel())
                .fullPath(savedCategory.getFullPath())
                .productCount(savedCategory.getProductCount())
                .parentId(savedCategory.getParentCategory() != null ? savedCategory.getParentCategory().getId() : null)
                .build();

        
    }

    public ProductDto creaetProduct(ProductRequest req)
    {
        Category category = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 2️⃣ Validate Brand
        Brand brand = brandRepo.findById(req.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));


        Product product=Product.builder()
                        .sku(req.getSku())
                        .name(req.getName())
                        .slug(req.getSlug())
                        .isActive(req.getIsActive()!=null?req.getIsActive():true)
                        .shortDescription(req.getShortDescription())
                        .fullDescription(req.getFullDescription())
                        .basePrice(req.getBasePrice())
                        .discountedPrice(req.getDiscountedPrice() != null ? req.getDiscountedPrice() : req.getBasePrice())
                        .discountedPercentage(req.getDiscountedPercentage())
                        .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                        .stockQuantity(req.getStockQuantity() != null ? req.getStockQuantity() : 0)
                        .category(category)
                        .brand(brand)
                        .tags(req.getTags()!=null?req.getTags():new HashSet<>())
                        .searchKeywords(req.getSearchKeywords()!=null?req.getSearchKeywords():generateKeywords(req, category, brand))
                        .build();
        productRepo.save(product);

        if(req.getVariants()!=null)
        {
            req.getVariants().forEach(vreq->{
                ProductVariant variant=ProductVariant.builder()
                                        .sku(vreq.getSku())
                                        .variantName(vreq.getVariantName())
                                        .color(vreq.getColor())
                                        .size(vreq.getSize())
                                        .storage(vreq.getStorage())
                                        .material(vreq.getMaterial())
                                        .additionalPrice(vreq.getAdditionalPrice())
                                        .stockQuantity(vreq.getStockQuantity())
                                        .isDefault(vreq.getIsDefault())
                                        .displayOrder(vreq.getDisplayOrder())
                                        .build();
                
                product.addVariant(variant);
                
            });
        }

        if(req.getAttributes()!=null)
        {
            req.getAttributes().forEach(attreq->{
                ProductAttribute attribute=ProductAttribute.builder()
                                            .attributeName(attreq.getAttributeName())
                                            .attributeValue(attreq.getAttributeValue())
                                            .attributeGroup(attreq.getAttributeGroup())
                                            .displayOrder(attreq.getDisplayOrder())
                                            .isSearchable(attreq.getIsSearchable())
                                            .isFilterable(attreq.getIsFilterable())
                                            .unit(attreq.getUnit())
                                            .dataType(attreq.getDataType())
                                            .build();
                product.addAttribute(attribute);
            });         
        }
        Product savedProduct=productRepo.save(product);

        ProductDto productDto=ProductDto.builder()
                                .id(savedProduct.getId())
                                .sku(savedProduct.getSku())
                                .slug(savedProduct.getSlug())
                                .isActive(savedProduct.getIsActive())
                                .shortDescription(savedProduct.getShortDescription())
                                .fullDescription(savedProduct.getFullDescription())
                                .basePrice(savedProduct.getBasePrice())
                                .discountedPrice(savedProduct.getDiscountedPrice())
                                .discountedPercentage(savedProduct.getDiscountedPercentage())
                                .currency(savedProduct.getCurrency())
                                .stockQuantity(savedProduct.getStockQuantity())
                                .stockStatus(savedProduct.getStockStatus())
                                .categoryId(savedProduct.getCategory().getId())
                                .brandId(savedProduct.getBrand().getId())
                                .tags(savedProduct.getTags())
                                .viewCount(savedProduct.getViewCount())
                                .orderCount(savedProduct.getOrderCount())
                                .avgRating(savedProduct.getAvgRating())
                                .searchKeywords(savedProduct.getSearchKeywords())
                                .searchBoost(savedProduct.getSearchBoost())
                                .createdAt(savedProduct.getCreatedAt())
                                .updatedAt(savedProduct.getUpdatedAt())
                                .publishedAt(savedProduct.getPublishedAt())
                                .variants(savedProduct.getVariants().stream().map(mapper::objectToDto).toList())
                                .build();
        
        return productDto;
    }

    private String generateKeywords(ProductRequest req,Category category,Brand brand)
    {
        StringBuilder keyword=new StringBuilder();
        if(req.getName()!=null)
        {
            keyword.append(req.getName().toLowerCase()).append(" ");
        }
        keyword.append(brand.getName().toLowerCase()).append(" ");
        keyword.append(category.getName().toLowerCase()).append(" ");
        if(req.getTags()!=null)
        {
            req.getTags().forEach(tag->keyword.append(tag.toLowerCase()).append(" "));
        }

        if (req.getShortDescription() != null) 
        {
            keyword.append(req.getShortDescription().toLowerCase()).append(" ");
        }

        if (req.getFullDescription() != null) 
        {
            keyword.append(req.getFullDescription().toLowerCase()).append(" ");
        }
        return keyword.toString().trim();
    }
}
