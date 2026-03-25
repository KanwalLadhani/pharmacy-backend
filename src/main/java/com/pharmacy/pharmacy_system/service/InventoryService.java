package com.pharmacy.pharmacy_system.service;

import com.pharmacy.pharmacy_system.model.Medicine;
import com.pharmacy.pharmacy_system.repository.MedicineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for handling logic related to medicine inventory.
 */
@Service
public class InventoryService {

    private final MedicineRepository medicineRepository;

    @Autowired
    public InventoryService(MedicineRepository medicineRepository) {
        this.medicineRepository = medicineRepository;
    }

    public Page<Medicine> getAllMedicines(Pageable pageable) {
        return medicineRepository.findAll(pageable);
    }

    public Medicine getMedicineById(Long id) {
        return medicineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found ID: " + id));
    }

    public Medicine addMedicine(Medicine medicine) {
        return medicineRepository.save(medicine);
    }

    public Medicine updateMedicine(Long id, Medicine medicineDetails) {
        Medicine medicine = getMedicineById(id);
        
        medicine.setBrandName(medicineDetails.getBrandName());
        medicine.setGeneric(medicineDetails.getGeneric());
        medicine.setDosageForm(medicineDetails.getDosageForm());
        medicine.setStrength(medicineDetails.getStrength());
        medicine.setManufacturer(medicineDetails.getManufacturer());
        medicine.setPrice(medicineDetails.getPrice());
        medicine.setQuantity(medicineDetails.getQuantity());
        medicine.setReorderLevel(medicineDetails.getReorderLevel());
        medicine.setSupplier(medicineDetails.getSupplier());
        medicine.setIsNarcotic(medicineDetails.getIsNarcotic());

        return medicineRepository.save(medicine);
    }

    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }

    public Page<Medicine> searchMedicines(String query, Pageable pageable) {
        return medicineRepository.findByBrandNameContainingIgnoreCase(query, pageable);
    }

    /**
     * Finds alternate medicines with the same salt formula (generic).
     */
    public List<Medicine> getAlternates(Long brandId) {
        Medicine original = getMedicineById(brandId);
        String saltFormula = original.getGeneric();
        
        if (saltFormula == null || saltFormula.trim().isEmpty()) {
            return List.of();
        }

        // 1. Get exact matches for the salt formula (same generic name)
        List<Medicine> exactMatches = medicineRepository.findByGenericAndQuantityGreaterThan(saltFormula, 0);
        
        // Filter out the original medicine itself
        List<Medicine> alternates = exactMatches.stream()
            .filter(m -> !m.getBrandId().equals(brandId))
            .toList();

        // 2. If no exact matches, or to provide more options, do a partial match
        if (alternates.size() < 5) {
            List<Medicine> similarMatches = medicineRepository.findByGenericContainingIgnoreCaseAndQuantityGreaterThan(saltFormula, 0);
            return similarMatches.stream()
                .filter(m -> !m.getBrandId().equals(brandId))
                .limit(10)
                .toList();
        }

        return alternates;
    }

    @org.springframework.transaction.annotation.Transactional
    public void adjustInventoryValues() {
        List<Medicine> all = medicineRepository.findAll();
        for (Medicine m : all) {
            if (m.getPrice() == null || m.getQuantity() == null) continue;
            
            double currentPrice = m.getPrice().doubleValue();
            
            // Radically reduce quantity based on price tier to reach target 2 Crore valuation
            if (currentPrice > 5000) {
                m.setQuantity(Math.min(m.getQuantity(), 2));
            } else if (currentPrice > 1000) {
                m.setQuantity(Math.min(m.getQuantity(), 5)); 
            } else if (currentPrice > 500) {
                m.setQuantity(Math.min(m.getQuantity(), 15));
            } else {
                m.setQuantity(Math.min(m.getQuantity(), 25));
            }
        }
        medicineRepository.saveAll(all);
    }
}
