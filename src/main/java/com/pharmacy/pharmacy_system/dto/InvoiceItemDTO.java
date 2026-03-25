package com.pharmacy.pharmacy_system.dto;

/**
 * DTO representing a single medicine line-item in an invoice request.
 */
public class InvoiceItemDTO {
    private Long medicineId;
    private Integer quantity;

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
