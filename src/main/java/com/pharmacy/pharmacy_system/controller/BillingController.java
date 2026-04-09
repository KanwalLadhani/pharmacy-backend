package com.pharmacy.pharmacy_system.controller;

import com.pharmacy.pharmacy_system.dto.InvoiceRequestDTO;
import com.pharmacy.pharmacy_system.dto.ProfitReportDTO;
import com.pharmacy.pharmacy_system.model.Invoice;
import com.pharmacy.pharmacy_system.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    /** GET /api/billing/history — returns all invoices with pagination. */
    @GetMapping("/history")
    public ResponseEntity<org.springframework.data.domain.Page<Invoice>> getInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(billingService.getAllInvoices(org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by("date").descending())));
    }

    /** GET /api/billing/invoices/{invoiceNumber} — fetch invoice by number */
    @GetMapping("/invoices/{invoiceNumber}")
    public ResponseEntity<Invoice> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        return billingService.getInvoiceByNumber(invoiceNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/billing/profit-report?date=YYYY-MM-DD — get daily profit report */
    @GetMapping("/profit-report")
    public ResponseEntity<ProfitReportDTO> getProfitReport(@RequestParam(required = false) String date) {
        LocalDate reportDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(billingService.getDailyProfitReport(reportDate));
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

    /** POST /api/billing/invoices/{invoiceNumber}/return — process a refund/return (FULL) */
    @PostMapping("/invoices/{invoiceNumber}/return")
    public ResponseEntity<Invoice> returnInvoice(@PathVariable String invoiceNumber) {
        try {
            return ResponseEntity.ok(billingService.returnInvoice(invoiceNumber));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /** POST /api/billing/invoices/partial-return — process a partial refund/return */
    @PostMapping("/invoices/partial-return")
    public ResponseEntity<Invoice> partialReturn(@RequestBody com.pharmacy.pharmacy_system.dto.PartialReturnRequestDTO request) {
        try {
            return ResponseEntity.ok(billingService.processPartialReturn(request));
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
