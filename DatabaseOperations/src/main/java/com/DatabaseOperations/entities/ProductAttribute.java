package com.DatabaseOperations.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.DatabaseOperations.enums.AttributeDataType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name="product_attributes")
public class ProductAttribute 
{
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id",nullable = false)
    private Product product;


     @Column(name = "attribute_name", nullable = false, length = 200)
    private String attributeName; // e.g., "RAM", "Processor", "Screen Size"

    @Column(name = "attribute_value", nullable = false, length = 500)
    private String attributeValue; // e.g., "16GB", "Intel Core i7", "15.6 inch"

    @Column(name = "attribute_group", length = 100)
    private String attributeGroup; // e.g., "Performance", "Display", "Connectivity"


     @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_searchable", nullable = false)
    @Builder.Default
    private Boolean isSearchable = true;

    @Column(name = "is_filterable", nullable = false)
    @Builder.Default
    private Boolean isFilterable = true;

     @Column(length = 20)
    private String unit; // e.g., "GB", "GHz", "inches", "kg"

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    @Builder.Default
    private AttributeDataType dataType = AttributeDataType.TEXT;



    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public String getDisplayName() {
        return attributeName;
    }

    public String getFormattedValue()
    {
        if (unit != null && !unit.isEmpty()) {
            return attributeValue + " " + unit;
        }
        return attributeValue;
    }

    public Double getNumericValue() 
    {
        if (dataType == AttributeDataType.NUMBER) {
            try {
                return Double.parseDouble(attributeValue.replaceAll("[^0-9.]", ""));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Boolean getBooleanValue()
    {
        if (dataType == AttributeDataType.BOOLEAN) {
            return Boolean.parseBoolean(attributeValue);
        }
        return null;
    }





    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @Override
    public String toString() {
        return "ProductAttribute{" +
                "id=" + id +
                ", attributeName='" + attributeName + '\'' +
                ", attributeValue='" + attributeValue + '\'' +
                ", unit='" + unit + '\'' +
                ", attributeGroup='" + attributeGroup + '\'' +
                '}';
    }

    
}
