package com.pharmacy.pharmacy_system.service;

import com.pharmacy.pharmacy_system.dto.InvoiceRequestDTO;
import com.pharmacy.pharmacy_system.dto.InvoiceItemDTO;
import com.pharmacy.pharmacy_system.model.Invoice;
import com.pharmacy.pharmacy_system.model.InvoiceItem;
import com.pharmacy.pharmacy_system.model.Medicine;
import com.pharmacy.pharmacy_system.repository.InvoiceRepository;
import com.pharmacy.pharmacy_system.repository.MedicineRepository;
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

    @Autowired
    public BillingService(InvoiceRepository invoiceRepository,
                          MedicineRepository medicineRepository) {
        this.invoiceRepository = invoiceRepository;
        this.medicineRepository = medicineRepository;
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
                med.setQuantity(currentQty + item.getQuantity());
                medicineRepository.save(med);
            }
        }

        invoice.setReturned(true);
        return invoiceRepository.save(invoice);
    }

    @Transactional
    public void clearAllSalesData() {
        invoiceRepository.deleteAll();
    }
}
