package com.DatabaseOperations.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Brand 
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;


     // Search / Filters
    @Column(name = "is_popular", nullable = false)
    @Builder.Default
    private Boolean isPopular = false;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    // Metadata
    @Column(name = "product_count", nullable = false)
    @Builder.Default
    private Integer productCount = 0;

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(length = 100)
    private String country;

    @Column(name = "founded_year")
    private Integer foundedYear;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "brand",fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products=new ArrayList<>();

    //helpers
     @PrePersist
    protected void onCreate() {
        // Slug auto generation if not provided
        if (slug == null && name != null) {
            this.slug = name.toLowerCase().replace(" ", "-");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Ensure slug stays consistent with name changes
        if (name != null) {
            this.slug = name.toLowerCase().replace(" ", "-");
        }
    }

    public void recalculateAverageRating()
    {
        List<Product> activeProducts=this.products.stream().filter(product->product.getIsActive()).toList();

        if (activeProducts.isEmpty()) {
            this.averageRating = 0.0;
            return;
        }
        double sum=activeProducts.stream().mapToDouble(product->product.getAvgRating()).sum();
        this.averageRating = Math.round((sum / activeProducts.size()) * 100.0) / 100.0;

    }

    public void addProduct(Product product) 
    {
            products.add(product);
            product.setBrand(this);
    }

    public void removeProduct(Product product) 
    {
        products.remove(product);
        product.setBrand(null);
    }


     @Override
    public String toString() {
        return "Brand{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isPopular=" + isPopular +
                ", productCount=" + productCount +
                '}';
    }

    
}
