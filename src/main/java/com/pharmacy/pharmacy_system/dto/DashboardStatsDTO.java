package com.pharmacy.pharmacy_system.dto;

import com.pharmacy.pharmacy_system.model.Medicine;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO returned by the dashboard endpoint containing aggregated stats.
 */
public class DashboardStatsDTO {

    private Long totalMedicines;
    private BigDecimal totalSales;
    private BigDecimal totalInventoryValue;
    private List<Medicine> lowStockItems;

    public DashboardStatsDTO(Long totalMedicines, BigDecimal totalSales,
                             BigDecimal totalInventoryValue, List<Medicine> lowStockItems) {
        this.totalMedicines = totalMedicines;
        this.totalSales = totalSales;
        this.totalInventoryValue = totalInventoryValue;
        this.lowStockItems = lowStockItems;
    }

    public Long getTotalMedicines() { return totalMedicines; }
    public void setTotalMedicines(Long totalMedicines) { this.totalMedicines = totalMedicines; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public BigDecimal getTotalInventoryValue() { return totalInventoryValue; }
    public void setTotalInventoryValue(BigDecimal totalInventoryValue) { this.totalInventoryValue = totalInventoryValue; }

    public List<Medicine> getLowStockItems() { return lowStockItems; }
    public void setLowStockItems(List<Medicine> lowStockItems) { this.lowStockItems = lowStockItems; }
}
