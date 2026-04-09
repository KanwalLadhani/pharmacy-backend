package com.pharmacy.pharmacy_system.repository;

import com.pharmacy.pharmacy_system.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    @Query(value = "SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.items it LEFT JOIN FETCH it.medicine m",
           countQuery = "SELECT COUNT(i) FROM Invoice i")
    org.springframework.data.domain.Page<Invoice> findAllWithItems(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items it LEFT JOIN FETCH it.medicine m WHERE i.invoiceNumber = :invoiceNumber")
    Optional<Invoice> findByInvoiceNumberWithItems(String invoiceNumber);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.isReturned = false")
    BigDecimal calculateTotalSales();

    /** Returns invoices for a specific period. */
    List<Invoice> findByDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    org.springframework.data.domain.Page<Invoice> findByDateBetweenOrderByDateDesc(java.time.LocalDateTime start, java.time.LocalDateTime end, org.springframework.data.domain.Pageable pageable);
    
    /** Returns all invoices for a period (used for profit reports) */
    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items it LEFT JOIN FETCH it.medicine m WHERE i.date BETWEEN :start AND :end ORDER BY i.date DESC")
    List<Invoice> findByDateBetweenOrderByDateDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
