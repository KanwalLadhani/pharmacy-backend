package com.pharmacy.pharmacy_system.controller;

import com.pharmacy.pharmacy_system.model.Medicine;
import com.pharmacy.pharmacy_system.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for medicine inventory operations.
 * Base path: /api/medicines
 */
@RestController
@RequestMapping("/api/medicines")
@CrossOrigin(origins = "*")
public class MedicineController {

    private final InventoryService inventoryService;

    @Autowired
    public MedicineController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /** GET /api/medicines — list medicines with pagination */
    @GetMapping
    public Page<Medicine> getAllMedicines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return inventoryService.getAllMedicines(PageRequest.of(page, size));
    }

    /** GET /api/medicines/{brandId} — get single medicine */
    @GetMapping("/{brandId}")
    public ResponseEntity<Medicine> getMedicineById(@PathVariable Long brandId) {
        return ResponseEntity.ok(inventoryService.getMedicineById(brandId));
    }

    /** POST /api/medicines — add a new medicine */
    @PostMapping
    public ResponseEntity<Medicine> addMedicine(@RequestBody Medicine medicine) {
        return ResponseEntity.ok(inventoryService.addMedicine(medicine));
    }

    /** PUT /api/medicines/{brandId} — update an existing medicine */
    @PutMapping("/{brandId}")
    public ResponseEntity<Medicine> updateMedicine(@PathVariable Long brandId,
                                                   @RequestBody Medicine medicineDetails) {
        return ResponseEntity.ok(inventoryService.updateMedicine(brandId, medicineDetails));
    }

    /** DELETE /api/medicines/{brandId} — delete a medicine */
    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> deleteMedicine(@PathVariable Long brandId) {
        inventoryService.deleteMedicine(brandId);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/medicines/search?query=xxx — search by brand name with pagination */
    @GetMapping("/search")
    public Page<Medicine> searchMedicines(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return inventoryService.searchMedicines(query, PageRequest.of(page, size));
    }

    /** GET /api/medicines/{brandId}/alternates — get alternates by salt formula */
    @GetMapping("/{brandId}/alternates")
    public List<Medicine> getAlternates(@PathVariable Long brandId) {
        return inventoryService.getAlternates(brandId);
    }

    /** GET /api/medicines/low-stock — get all medicines with stock <= 20 */
    @GetMapping("/low-stock")
    public List<Medicine> getLowStock() {
        return inventoryService.getLowStockMedicines(20);
    }

    /** GET /api/medicines/adjust-inventory — temporary endpoint to reduce valuation */
    @GetMapping("/adjust-inventory")
    public ResponseEntity<String> adjustInventory() {
        inventoryService.adjustInventoryValues();
        return ResponseEntity.ok("Inventory quantities successfully reduced to target ~2 Crore valuation!");
    }
}
