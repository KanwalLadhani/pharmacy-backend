package com.pharmacy.pharmacy_system.service;

import com.pharmacy.pharmacy_system.dto.DashboardStatsDTO;
import com.pharmacy.pharmacy_system.model.Medicine;
import com.pharmacy.pharmacy_system.repository.InvoiceRepository;
import com.pharmacy.pharmacy_system.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer providing aggregated stats for the dashboard.
 */
@Service
public class DashboardService {

    private static final int LOW_STOCK_THRESHOLD = 10;

    private final MedicineRepository medicineRepository;
    private final InvoiceRepository invoiceRepository;

    @Autowired
    public DashboardService(MedicineRepository medicineRepository,
                            InvoiceRepository invoiceRepository) {
        this.medicineRepository = medicineRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public DashboardStatsDTO getStats() {
        Long totalMedicines = medicineRepository.countTotalMedicines();
        if (totalMedicines == null) totalMedicines = 0L;

        BigDecimal totalSales = invoiceRepository.calculateTotalSales();
        if (totalSales == null) totalSales = BigDecimal.ZERO;

        BigDecimal totalInventoryValue = medicineRepository.calculateTotalInventoryValue();
        if (totalInventoryValue == null) totalInventoryValue = BigDecimal.ZERO;

        // Use the database reorder level logic instead of a fixed threshold of 10
        List<Medicine> lowStockItems = medicineRepository.findLowStockMedicines();

        return new DashboardStatsDTO(totalMedicines, totalSales, totalInventoryValue, lowStockItems);
    }
}
