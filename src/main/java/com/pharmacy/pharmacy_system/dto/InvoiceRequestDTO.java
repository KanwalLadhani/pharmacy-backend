package com.pharmacy.pharmacy_system.dto;

import java.io.Serializable;

import java.util.List;

/**
 * DTO wrapping the list of items sent when creating a new invoice.
 */
public class InvoiceRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<InvoiceItemDTO> items;
    private Double discountPercentage = 0.0;

    public List<InvoiceItemDTO> getItems() { return items; }
    public void setItems(List<InvoiceItemDTO> items) { this.items = items; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }
}
