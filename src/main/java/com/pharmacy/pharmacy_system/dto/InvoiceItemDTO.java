package com.pharmacy.pharmacy_system.dto;

import java.io.Serializable;

/**
 * DTO representing a single medicine line-item in an invoice request.
 */
public class InvoiceItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long medicineId;
    private Integer quantity;
    private java.math.BigDecimal discount;

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public java.math.BigDecimal getDiscount() { return discount; }
    public void setDiscount(java.math.BigDecimal discount) { this.discount = discount; }
}
