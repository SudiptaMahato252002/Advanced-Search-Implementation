package com.DatabaseOperations.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="product_variants")
public class ProductVariant 
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;
    
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "variant_name", nullable = false, length = 300)
    private String variantName;

    // Variant Attributes
    @Column(length = 100)
    private String color;

    @Column(length = 50)
    private String size;

    @Column(length = 50)
    private String storage;

    @Column(length = 100)
    private String material;

    //Pricing & Stock
    @Column(name = "additional_price", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal additionalPrice = BigDecimal.ZERO;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    // Metadata
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    //HELPERS
    public BigDecimal getFinalPrice() {
        if (product == null) {
            return additionalPrice;
        }
        return product.getCurrentPrice().add(additionalPrice);
    }
    
    public boolean isInStock() {
        return isAvailable && stockQuantity > 0;
    }

    public void updateAvailability() {
        this.isAvailable = stockQuantity > 0;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updateAvailability();
    }

    @Override
    public String toString() {
        return "ProductVariant{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", variantName='" + variantName + '\'' +
                ", color='" + color + '\'' +
                ", size='" + size + '\'' +
                ", stockQuantity=" + stockQuantity +
                '}';
    }

}
