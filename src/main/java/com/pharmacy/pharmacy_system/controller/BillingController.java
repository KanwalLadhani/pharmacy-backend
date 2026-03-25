package com.pharmacy.pharmacy_system.controller;

import com.pharmacy.pharmacy_system.dto.InvoiceRequestDTO;
import com.pharmacy.pharmacy_system.model.Invoice;
import com.pharmacy.pharmacy_system.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for billing / invoice operations.
 * Base path: /api/billing
 */
@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    private final BillingService billingService;

    @Autowired
    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    /** POST /api/billing/invoice — create a new invoice */
    @PostMapping("/invoice")
    public ResponseEntity<Invoice> createInvoice(@RequestBody InvoiceRequestDTO requestDTO) {
        return ResponseEntity.ok(billingService.createInvoice(requestDTO));
    }

    /** GET /api/billing/invoices — list all invoices */
    @GetMapping("/invoices")
    public List<Invoice> getAllInvoices() {
        return billingService.getAllInvoices();
    }

    /** GET /api/billing/invoices/{invoiceNumber} — fetch invoice by number */
    @GetMapping("/invoices/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        return billingService.getInvoiceByNumber(invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sales/summary")
    public ResponseEntity<BigDecimal> getSalesSummary(
            @RequestParam String start,
            @RequestParam String end) {
        try {
            // Flexible parsing of ISO strings into LocalDateTime
            String s = start.replace(" ", "T").replace("Z", "");
            String e = end.replace(" ", "T").replace("Z", "");
            if (s.length() == 16) s += ":00";
            if (e.length() == 16) e += ":00";
            
            LocalDateTime startDt = LocalDateTime.parse(s);
            LocalDateTime endDt = LocalDateTime.parse(e);
            
            return ResponseEntity.ok(billingService.getTotalSalesBetween(startDt, endDt));
        } catch (Exception ex) {
            return ResponseEntity.ok(BigDecimal.ZERO);
        }
    }

    /** POST /api/billing/invoices/{invoiceNumber}/return — process a refund/return */
    @PostMapping("/invoices/{invoiceNumber}/return")
    public ResponseEntity<Invoice> returnInvoice(@PathVariable String invoiceNumber) {
        try {
            return ResponseEntity.ok(billingService.returnInvoice(invoiceNumber));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** DELETE /api/billing/sales/clear — clear all sales history */
    @DeleteMapping("/sales/clear")
    public ResponseEntity<Void> clearSales() {
        billingService.clearAllSalesData();
        return ResponseEntity.noContent().build();
    }
}
