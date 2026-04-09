package com.pharmacy.pharmacy_system.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Represents a single line-item inside an invoice.
 * Maps to the 'invoice_items' table in the database.
 */
@Entity
@Table(name = "invoice_items")
public class InvoiceItem implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Back-reference to the parent invoice (not serialised to JSON). */
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer quantity;

    /** Unit price at the time of sale (snapshot). */
    @Column(nullable = false, precision = 19, scale = 3)
    private BigDecimal price;

    @Column(name = "purchase_price", precision = 19, scale = 3)
    private BigDecimal purchasePrice;

    @Column(name = "item_discount", precision = 19, scale = 3)
    private BigDecimal itemDiscount = BigDecimal.ZERO;

    @Column(name = "returned_quantity", columnDefinition = "integer default 0")
    private Integer returnedQuantity = 0;


    public InvoiceItem() {}

    public InvoiceItem(Medicine medicine, Integer quantity, BigDecimal price, BigDecimal purchasePrice, BigDecimal itemDiscount) {
        this.medicine = medicine;
        this.quantity = quantity;
        this.price = price;
        this.purchasePrice = purchasePrice;
        this.itemDiscount = itemDiscount != null ? itemDiscount : BigDecimal.ZERO;
    }

    // ---------- Getters & Setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getItemDiscount() { return itemDiscount; }
    public void setItemDiscount(BigDecimal itemDiscount) { this.itemDiscount = itemDiscount; }

    public Integer getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(Integer returnedQuantity) { this.returnedQuantity = returnedQuantity; }
}
