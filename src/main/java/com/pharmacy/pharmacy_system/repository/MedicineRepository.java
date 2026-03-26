package com.pharmacy.pharmacy_system.repository;

import com.pharmacy.pharmacy_system.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    /** Search by medicine name (itemName/brandName) with pagination. */
    Page<Medicine> findByBrandNameContainingIgnoreCase(String brandName, Pageable pageable);

    /** Returns medicines with stock below their reorder level. */
    @Query("SELECT m FROM Medicine m WHERE m.quantity IS NOT NULL AND m.reorderLevel IS NOT NULL AND m.quantity < m.reorderLevel")
    List<Medicine> findLowStockMedicines();

    /** Returns medicines with stock below the given threshold. */
    @Query("SELECT m FROM Medicine m WHERE m.quantity IS NOT NULL AND m.quantity < ?1")
    List<Medicine> findLowStockMedicines(Integer threshold);

    /** Returns total count of medicine entries. */
    @Query("SELECT COUNT(m) FROM Medicine m")
    Long countTotalMedicines();

    /** Returns total inventory value = SUM(quantity * price) across all medicines. */
    @Query("SELECT COALESCE(SUM(m.quantity * m.price), 0) FROM Medicine m WHERE m.quantity > 0 AND m.price IS NOT NULL")
    BigDecimal calculateTotalInventoryValue();

    /** Find medicines with exactly the same salt formula and in stock. */
    List<Medicine> findByGenericAndQuantityGreaterThan(String generic, Integer quantity);

    /** Find medicines with similar salt formula and in stock. */
    List<Medicine> findByGenericContainingIgnoreCaseAndQuantityGreaterThan(String generic, Integer quantity);
}
