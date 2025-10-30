package com.DatabaseOperations.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.DatabaseOperations.enums.StockStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Builder
public class Product 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true,length = 100)
    private String sku;

    @Column(nullable = false,length = 500)
    private String name;

    @Column(nullable = false,unique = true,length = 100)
    private String slug;

    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    
    @Column(name="short_description",length=500)
    private String shortDescription;

    @Column(name="full_description",columnDefinition = "TEXT")
    private String fullDescription;

    @Column(name="base_price",nullable = false,precision = 10,scale = 2)
    private BigDecimal basePrice;

    @Column(name="discounted_price",nullable = false,precision = 10,scale = 2)
    private Integer discountedPrice;

    @Column(name="discount_percentage")
    private Integer discountedPercentage;

    @Column(length=3,nullable = false)
    @Builder.Default
    private String currency="INR";

    @Column(name="stock_quantity",nullable=false)
    @Builder.Default
    private Integer stockQuantity=0;
    
    @Column(name="stock_status",nullable=false,length=20)
    @Builder.Default
    private StockStatus stockStatus=StockStatus.OUT_OF_STOCK;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id",nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id",nullable = false)
    private Brand brand;

    @ElementCollection
    @CollectionTable(name = "product_tags",joinColumns = @JoinColumn(name="product_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags=new HashSet<>();


    //necessary for determining rankings
    @Column(name="view_count",nullable = false)
    @Builder.Default
    private Long viewCount=0L;

    @Column(name="order_count",nullable = false)
    @Builder.Default
    private Integer orderCount=0;

    @Column(name="avg_rating",nullable = false)
    @Builder.Default
    private Double avgRating=0.0;

    //For search optimization

    @Column(name = "search_keywords", columnDefinition = "TEXT")
    private String searchKeywords;

    @Column(name = "search_boost", nullable = false)
    @Builder.Default
    private Integer searchBoost=1;


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;


    @OneToMany(mappedBy = "product",
                cascade = CascadeType.ALL,
                orphanRemoval = true,
                fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<ProductVariant> variants=new ArrayList<>();


    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductAttribute> attributes = new ArrayList<>();


    //helpers
    public BigDecimal getCurrentPrice()
    {
        BigDecimal price=discountedPrice!=null?BigDecimal.valueOf(discountedPrice):basePrice;
        return price;
    }



    public void addVariant(ProductVariant variant) 
    {
        variants.add(variant);
        variant.setProduct(this);
    }

    public void removeVariant(ProductVariant variant) 
    {
        variants.remove(variant);
        variant.setProduct(null);
    }

    public void addAttribute(ProductAttribute attribute) {
        attributes.add(attribute);
        attribute.setProduct(this);
    }

    public void removeAttribute(ProductAttribute attribute) {
        attributes.remove(attribute);
        attribute.setProduct(null);
    }

    @PrePersist
    protected void onCreate()
    {
        if (publishedAt == null && isActive) {
        publishedAt = LocalDateTime.now();
    }
    updateStockStatus();
    }

    @PreUpdate
    protected void onUpdate()
    {
        updateStockStatus();
    }

    public void updateStockStatus()
    {
        if (stockQuantity == 0) {
        this.stockStatus = StockStatus.OUT_OF_STOCK;
    } else if (stockQuantity <= 10) {
        this.stockStatus = StockStatus.LOW_STOCK;
    } else {
        this.stockStatus = StockStatus.IN_STOCK;
    }

    }
}
