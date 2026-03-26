package com.pharmacy.pharmacy_system.model;

import java.io.Serializable;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Represents a medicine in the pharmacy inventory.
 * Maps to the 'inventory' table in the database.
 */
@Entity
@Table(name = "inventory")
public class Medicine implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @Column(name = "item_id")
    private Long brandId;

    @Column(name = "item_name", nullable = false)
    private String brandName;

    @Column(name = "generic_formula")
    private String generic;

    @Column(name = "dosage_form")
    private String dosageForm;

    private String strength;

    private String manufacturer;

    @Column(name = "retail_value")
    private BigDecimal price;

    @Column(name = "available_qty")
    private Integer quantity;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    private String supplier;

    @Column(name = "is_narcotic")
    private Boolean isNarcotic;

    public Medicine() {}

    public Medicine(Long brandId, String brandName, String generic,
                    BigDecimal price, Integer quantity) {
        this.brandId = brandId;
        this.brandName = brandName;
        this.generic = generic;
        this.price = price;
        this.quantity = quantity;
    }

    // ---------- Getters & Setters ----------
    // Note: Keeping existing getter/setter names (getBrandId, getBrandName, etc.) 
    // to maintain compatibility with the rest of the backend and frontend logic
    // while mapping to the new database column names.

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getGeneric() { return generic; }
    public void setGeneric(String generic) { this.generic = generic; }

    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }

    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public Boolean getIsNarcotic() { return isNarcotic; }
    public void setIsNarcotic(Boolean isNarcotic) { this.isNarcotic = isNarcotic; }
    
    // Keeping some old fields as transient or empty getters/setters if needed for compatibility, 
    // but the ones above cover the new schema.
}
