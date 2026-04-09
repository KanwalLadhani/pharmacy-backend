package com.pharmacy.pharmacy_system.service;

import com.pharmacy.pharmacy_system.dto.InvoiceRequestDTO;
import com.pharmacy.pharmacy_system.dto.InvoiceItemDTO;
import com.pharmacy.pharmacy_system.model.Invoice;
import com.pharmacy.pharmacy_system.model.InvoiceItem;
import com.pharmacy.pharmacy_system.model.Medicine;
import com.pharmacy.pharmacy_system.repository.InvoiceRepository;
import com.pharmacy.pharmacy_system.repository.MedicineRepository;
import com.pharmacy.pharmacy_system.repository.InvoiceItemRepository;
import com.pharmacy.pharmacy_system.dto.ProfitReportDTO;

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
        if (invoiceDTO.getItems() == null || invoiceDTO.getItems().isEmpty()) {
            throw new RuntimeException("Cannot create an empty invoice.");
        }

        Invoice invoice = new Invoice();
        String dateStr = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd").format(java.time.LocalDate.now());
        String randomSuffix = java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        invoice.setInvoiceNumber("INV-" + dateStr + "-" + randomSuffix);
        
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;

        for (InvoiceItemDTO itemDTO : invoiceDTO.getItems()) {
            if (itemDTO.getMedicineId() == null) throw new RuntimeException("Medicine ID is missing for an item.");
            
            Medicine medicine = medicineRepository.findById(itemDTO.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine not found with ID: " + itemDTO.getMedicineId()));

            int requestedQty = itemDTO.getQuantity() != null ? itemDTO.getQuantity() : 0;
            if (requestedQty <= 0) throw new RuntimeException("Quantity must be greater than zero for " + medicine.getBrandName());

            int currentStock = medicine.getQuantity() != null ? medicine.getQuantity() : 0;
            if (currentStock < requestedQty) {
                throw new RuntimeException("Insufficient stock for '" + medicine.getBrandName() + "'. Available: " + currentStock);
            }

            // Update medicine record
            medicine.setQuantity(currentStock - requestedQty);
            medicineRepository.save(medicine);

            BigDecimal unitPrice = medicine.getPrice() != null ? medicine.getPrice() : BigDecimal.ZERO;
            BigDecimal purchasePrice = medicine.getPurchasePrice() != null ? medicine.getPurchasePrice() : BigDecimal.ZERO;
            BigDecimal itemDiscount = itemDTO.getDiscount() != null ? itemDTO.getDiscount() : BigDecimal.ZERO;
            
            BigDecimal itemSubtotal = unitPrice.multiply(BigDecimal.valueOf(requestedQty));
            subtotal = subtotal.add(itemSubtotal);
            totalDiscountAmount = totalDiscountAmount.add(itemDiscount);

            // Add item to invoice
            InvoiceItem newItem = new InvoiceItem(medicine, requestedQty, unitPrice, purchasePrice, itemDiscount);
            invoice.addItem(newItem);
        }

        invoice.setDiscountPercentage(0.0); // Deprecating global percentage
        invoice.setDiscountAmount(totalDiscountAmount);
        
        // Granular Requirement: Grand Total = Σ(Price * Qty - Individual Item Discount)
        invoice.setTotalAmount(subtotal.subtract(totalDiscountAmount));
        
        calculateProfit(invoice);
        
        return invoiceRepository.save(invoice);
    }

    private void calculateProfit(Invoice invoice) {
        BigDecimal totalCost = BigDecimal.ZERO;
        for (InvoiceItem item : invoice.getItems()) {
            int effectiveQty = item.getQuantity() - (item.getReturnedQuantity() != null ? item.getReturnedQuantity() : 0);
            if (effectiveQty > 0 && item.getPurchasePrice() != null) {
                totalCost = totalCost.add(item.getPurchasePrice().multiply(BigDecimal.valueOf(effectiveQty)));
            }
        }
        invoice.setTotalProfit(invoice.getTotalAmount().subtract(totalCost));
    }

    public ProfitReportDTO getDailyProfitReport(java.time.LocalDate date) {
        java.time.LocalDateTime startOfDay = date.atStartOfDay();
        java.time.LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        List<Invoice> dailyInvoices = invoiceRepository.findByDateBetweenOrderByDateDesc(startOfDay, endOfDay);
        
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;

        for (Invoice invoice : dailyInvoices) {
            if (invoice.isReturned()) continue;
            
            totalRevenue = totalRevenue.add(invoice.getTotalAmount());
            totalDiscounts = totalDiscounts.add(invoice.getDiscountAmount());
            
            for (InvoiceItem item : invoice.getItems()) {
                int effectiveQty = item.getQuantity() - (item.getReturnedQuantity() != null ? item.getReturnedQuantity() : 0);
                if (effectiveQty > 0 && item.getPurchasePrice() != null) {
                    totalCost = totalCost.add(item.getPurchasePrice().multiply(BigDecimal.valueOf(effectiveQty)));
                }
            }
        }
        
        BigDecimal netProfit = totalRevenue.subtract(totalCost);
        
        return new ProfitReportDTO(date, totalRevenue, totalCost, totalDiscounts, netProfit);
    }

    /** Returns all invoices with pagination. */
    public org.springframework.data.domain.Page<Invoice> getAllInvoices(org.springframework.data.domain.Pageable pageable) {
        return invoiceRepository.findAllWithItems(pageable);
    }

    @Transactional(readOnly = true)
    public java.util.Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumberWithItems(invoiceNumber);
    }

    /** Returns invoices for a specific period with pagination. */
    public org.springframework.data.domain.Page<Invoice> getInvoicesByPeriod(java.time.LocalDateTime start, java.time.LocalDateTime end, org.springframework.data.domain.Pageable pageable) {
        return invoiceRepository.findByDateBetweenOrderByDateDesc(start, end, pageable);
    }

    @Transactional(readOnly = true)
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
        BigDecimal totalItemDiscounts = BigDecimal.ZERO;
        
        for (InvoiceItem item : invoice.getItems()) {
            int originalQty = item.getQuantity();
            int returnedQty = item.getReturnedQuantity() != null ? item.getReturnedQuantity() : 0;
            int effectiveQty = originalQty - returnedQty;
            
            if (effectiveQty > 0) {
                // Gross amount for remaining quantity
                BigDecimal itemSub = item.getPrice().multiply(BigDecimal.valueOf(effectiveQty));
                newSubtotal = newSubtotal.add(itemSub);
                
                // Proportional discount: (TotalDiscount * EffectiveQty) / OriginalQty
                if (item.getItemDiscount() != null && item.getItemDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal proportionalDiscount = item.getItemDiscount()
                            .multiply(BigDecimal.valueOf(effectiveQty))
                            .divide(BigDecimal.valueOf(originalQty), 3, java.math.RoundingMode.HALF_UP);
                    totalItemDiscounts = totalItemDiscounts.add(proportionalDiscount);
                }
            }
        }

        invoice.setDiscountPercentage(0.0);
        invoice.setDiscountAmount(totalItemDiscounts);
        invoice.setTotalAmount(newSubtotal.subtract(totalItemDiscounts));
        
        calculateProfit(invoice);
    }

    @Transactional
    public void clearAllSalesData() {
        invoiceItemRepository.deleteAllInBatch();
        invoiceRepository.deleteAllInBatch();
    }
}
