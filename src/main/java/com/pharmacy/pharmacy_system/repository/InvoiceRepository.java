package com.pharmacy.pharmacy_system.repository;

import com.pharmacy.pharmacy_system.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.items it LEFT JOIN FETCH it.medicine m ORDER BY i.date DESC")
    List<Invoice> findAllWithItemsOrderByDateDesc();

    List<Invoice> findByDateBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    List<Invoice> findByDateBetweenOrderByDateDesc(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
