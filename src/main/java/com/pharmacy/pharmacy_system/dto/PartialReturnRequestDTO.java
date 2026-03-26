package com.pharmacy.pharmacy_system.dto;

import java.io.Serializable;
import java.util.List;

public class PartialReturnRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String invoiceNumber;
    private List<ReturnItemDTO> returnItems;

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public List<ReturnItemDTO> getReturnItems() { return returnItems; }
    public void setReturnItems(List<ReturnItemDTO> returnItems) { this.returnItems = returnItems; }

    public static class ReturnItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long itemId;
        private Integer returnQuantity;

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }

        public Integer getReturnQuantity() { return returnQuantity; }
        public void setReturnQuantity(Integer returnQuantity) { this.returnQuantity = returnQuantity; }
    }
}
