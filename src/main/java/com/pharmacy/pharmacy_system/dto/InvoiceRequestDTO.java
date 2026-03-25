package com.pharmacy.pharmacy_system.dto;

import java.util.List;

/**
 * DTO wrapping the list of items sent when creating a new invoice.
 */
public class InvoiceRequestDTO {
    private List<InvoiceItemDTO> items;

    public List<InvoiceItemDTO> getItems() { return items; }
    public void setItems(List<InvoiceItemDTO> items) { this.items = items; }
}
