package com.pharmacy.pharmacy_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProfitReportDTO {
    private LocalDate date;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal totalDiscounts;
    private BigDecimal netProfit;

    public ProfitReportDTO() {}

    public ProfitReportDTO(LocalDate date, BigDecimal totalRevenue, BigDecimal totalCost, BigDecimal totalDiscounts, BigDecimal netProfit) {
        this.date = date;
        this.totalRevenue = totalRevenue;
        this.totalCost = totalCost;
        this.totalDiscounts = totalDiscounts;
        this.netProfit = netProfit;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public BigDecimal getTotalDiscounts() { return totalDiscounts; }
    public void setTotalDiscounts(BigDecimal totalDiscounts) { this.totalDiscounts = totalDiscounts; }

    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }
}
