package com.DatabaseOperations.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class Category 
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory",
    cascade = CascadeType.ALL,
    fetch = FetchType.LAZY
    )
    private List<Category> subCategories;


    @Column(nullable = false)
    @Builder.Default
    private Integer level=0;

    @Column(name = "full_path",length = 1000)
    private String fullPath;

    @Column(name = "product_count", nullable = false)
    @Builder.Default
    private Integer productCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "category",fetch = FetchType.LAZY)
    @Builder.Default
    private List<Product> products=new ArrayList<>();


    //helper
    public void addSubCategory(Category subCategory) 
    {
        subCategories.add(subCategory);
        subCategory.setParentCategory(this);
        subCategory.setLevel(this.level + 1);
        subCategory.updateFullPath();
    }

    public void updateFullPath() 
    {
        if (parentCategory == null) {
            this.fullPath = this.name;
        } else {
            this.fullPath = parentCategory.getFullPath() + " > " + this.name;
        }
    }

    public boolean isRootCategory()
    {
        return this.parentCategory==null;
    }

    public boolean isLeafCategory()
    {
        return subCategories.isEmpty();
    }


      public List<Category> getAncestors() {
        List<Category> ancestors = new ArrayList<>();
        Category current = this.parentCategory;
        while (current != null) {
            ancestors.add(0, current); // Add to beginning to maintain order
            current = current.getParentCategory();
        }
        return ancestors;
    }

    @PrePersist
    @PreUpdate
    protected void updatePath() {
        updateFullPath();
    }

      @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", fullPath='" + fullPath + '\'' +
                '}';
    }

    
}
