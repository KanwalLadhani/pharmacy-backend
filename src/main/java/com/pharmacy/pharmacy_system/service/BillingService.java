package com.pharmacy.pharmacy_system.service;

import com.pharmacy.pharmacy_system.dto.InvoiceRequestDTO;
import com.pharmacy.pharmacy_system.dto.InvoiceItemDTO;
import com.pharmacy.pharmacy_system.model.Invoice;
import com.pharmacy.pharmacy_system.model.InvoiceItem;
import com.pharmacy.pharmacy_system.model.Medicine;
import com.pharmacy.pharmacy_system.repository.InvoiceRepository;
import com.pharmacy.pharmacy_system.repository.MedicineRepository;
import com.pharmacy.pharmacy_system.repository.InvoiceItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for managing invoices and billing operations.
 */
@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final MedicineRepository medicineRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    @Autowired
    public BillingService(InvoiceRepository invoiceRepository,
                          MedicineRepository medicineRepository,
                          InvoiceItemRepository invoiceItemRepository) {
        this.invoiceRepository = invoiceRepository;
        this.medicineRepository = medicineRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    /**
     * Creates a new invoice, updates medicine stock, and calculates total.
     */
    @Transactional
    public Invoice createInvoice(InvoiceRequestDTO invoiceDTO) {
        Invoice invoice = new Invoice();
        String invNum = "INV-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDate.now()) 
                        + "-" + java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        invoice.setInvoiceNumber(invNum);
        
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (InvoiceItemDTO itemDTO : invoiceDTO.getItems()) {
            Medicine medicine = medicineRepository.findById(itemDTO.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine not found ID: " + itemDTO.getMedicineId()));

            int requestedQty = itemDTO.getQuantity();
            int currentStock = medicine.getQuantity() != null ? medicine.getQuantity() : 0;

            if (currentStock < requestedQty || currentStock <= 0) {
                throw new RuntimeException(
                        "Insufficient stock for '" + medicine.getBrandName()
                        + "'. Available: " + (currentStock > 0 ? currentStock : 0));
            }

            // Deduct stock safely
            medicine.setQuantity(Math.max(0, currentStock - requestedQty));
            medicineRepository.save(medicine);

            BigDecimal unitPrice = medicine.getPrice() != null ? medicine.getPrice() : BigDecimal.ZERO;
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty));
            totalAmount = totalAmount.add(itemTotal);

            // Add item to invoice
            invoice.addItem(new InvoiceItem(medicine, requestedQty, unitPrice));
        }

        BigDecimal subtotal = totalAmount;
        BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(0.1)); // 10%
        BigDecimal finalTotal = subtotal.subtract(discount);

        invoice.setTotalAmount(finalTotal);
        invoice.setDiscountAmount(discount);
        invoice.setDiscountPercentage(10.0);
        
        return invoiceRepository.save(invoice);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByOrderByDateDesc();
    }

    public java.util.Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    public List<Invoice> getInvoicesByDateRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return invoiceRepository.findByDateBetweenOrderByDateDesc(start, end);
    }

    public BigDecimal getTotalSalesBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        List<Invoice> invoices = invoiceRepository.findByDateBetween(start, end);
        BigDecimal total = BigDecimal.ZERO;
        for (Invoice invoice : invoices) {
            if (invoice.getTotalAmount() != null && !invoice.isReturned()) {
                total = total.add(invoice.getTotalAmount());
            }
        }
        return total;
    }

    @Transactional
    public Invoice returnInvoice(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceNumber));

        if (invoice.isReturned()) {
            throw new RuntimeException("This invoice has already been returned/refunded.");
        }

        // Restore stock for all items
        for (InvoiceItem item : invoice.getItems()) {
            Medicine med = item.getMedicine();
            if (med != null) {
                int currentQty = med.getQuantity() != null ? med.getQuantity() : 0;
                int remainingToReturn = item.getQuantity() - item.getReturnedQuantity();
                if (remainingToReturn > 0) {
                    med.setQuantity(currentQty + remainingToReturn);
                    medicineRepository.save(med);
                    item.setReturnedQuantity(item.getQuantity()); // Mark as fully returned
                }
            }
        }

        invoice.setReturned(true);
        recalculateInvoiceTotals(invoice);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice processPartialReturn(com.pharmacy.pharmacy_system.dto.PartialReturnRequestDTO request) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber())
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + request.getInvoiceNumber()));

        if (invoice.isReturned()) {
            throw new RuntimeException("Invoice is already fully returned.");
        }

        for (com.pharmacy.pharmacy_system.dto.PartialReturnRequestDTO.ReturnItemDTO returnReq : request.getReturnItems()) {
            InvoiceItem item = invoice.getItems().stream()
                    .filter(i -> i.getId().equals(returnReq.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Item not found in invoice: " + returnReq.getItemId()));

            int alreadyReturned = item.getReturnedQuantity() != null ? item.getReturnedQuantity() : 0;
            int availableToReturn = item.getQuantity() - alreadyReturned;

            if (returnReq.getReturnQuantity() > availableToReturn) {
                throw new RuntimeException("Cannot return more than available quantity for " + item.getMedicine().getBrandName());
            }

            // Restore stock
            Medicine med = item.getMedicine();
            int currentStock = med.getQuantity() != null ? med.getQuantity() : 0;
            med.setQuantity(currentStock + returnReq.getReturnQuantity());
            medicineRepository.save(med);

            // Update item returned quantity
            item.setReturnedQuantity(alreadyReturned + returnReq.getReturnQuantity());
        }

        // Check if now fully returned
        boolean allReturned = invoice.getItems().stream()
                .allMatch(item -> item.getReturnedQuantity().equals(item.getQuantity()));
        if (allReturned) {
            invoice.setReturned(true);
        }

        recalculateInvoiceTotals(invoice);
        return invoiceRepository.save(invoice);
    }

    private void recalculateInvoiceTotals(Invoice invoice) {
        BigDecimal newSubtotal = BigDecimal.ZERO;
        for (InvoiceItem item : invoice.getItems()) {
            int effectiveQty = item.getQuantity() - (item.getReturnedQuantity() != null ? item.getReturnedQuantity() : 0);
            if (effectiveQty > 0) {
                BigDecimal itemTotal = item.getPrice().multiply(BigDecimal.valueOf(effectiveQty));
                newSubtotal = newSubtotal.add(itemTotal);
            }
        }

        BigDecimal discount = newSubtotal.multiply(BigDecimal.valueOf(invoice.getDiscountPercentage() / 100.0));
        invoice.setDiscountAmount(discount);
        invoice.setTotalAmount(newSubtotal.subtract(discount));
    }

    @Transactional
    public void clearAllSalesData() {
        invoiceItemRepository.deleteAllInBatch();
        invoiceRepository.deleteAllInBatch();
    }
}
