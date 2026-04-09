package com.pharmacy.pharmacy_system;

import com.pharmacy.pharmacy_system.model.*;
import com.pharmacy.pharmacy_system.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Temporary Data Extractor Utility.
 * Fetches all records and writes them as SQL INSERT statements to migration.sql.
 */
@Component
public class DataExtractor implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MedicineRepository medicineRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DataExtractor(UserRepository userRepository,
                         MedicineRepository medicineRepository,
                         InvoiceRepository invoiceRepository,
                         InvoiceItemRepository invoiceItemRepository) {
        this.userRepository = userRepository;
        this.medicineRepository = medicineRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
    }

    @Override
    public void run(String... args) {
        if (System.getProperty("extract.data") == null) {
            return;
        }

        System.out.println("\n--- DATA EXTRACTION START ---");
        System.out.println("Writing data to migration.sql...");

        try (PrintWriter writer = new PrintWriter(new FileWriter("migration.sql"))) {
            extractUsers(writer);
            extractInventory(writer);
            extractInvoices(writer);
            extractInvoiceItems(writer);
            System.out.println("Data extraction complete! Check migration.sql in the root directory.");
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }

        System.out.println("--- DATA EXTRACTION END ---\n");
        System.exit(0); // Exit so the Maven command finishes
    }

    private void extractUsers(PrintWriter writer) {
        writer.println("-- Table: users");
        userRepository.findAll().forEach(u -> {
            writer.printf("INSERT INTO users (username, password, role) VALUES ('%s', '%s', '%s');\n",
                    u.getUsername(), u.getPassword(), u.getRole().name());
        });
        writer.println();
    }

    private void extractInventory(PrintWriter writer) {
        writer.println("-- Table: inventory");
        medicineRepository.findAll().forEach(m -> {
            writer.printf("INSERT INTO inventory (item_id, item_name, generic_formula, dosage_form, strength, manufacturer, retail_value, available_qty, reorder_level, supplier, is_narcotic) " +
                    "VALUES (%d, '%s', %s, %s, %s, %s, %s, %d, %s, %s, %b);\n",
                    m.getBrandId(),
                    escapeSql(m.getBrandName()),
                    nullableString(m.getGeneric()),
                    nullableString(m.getDosageForm()),
                    nullableString(m.getStrength()),
                    nullableString(m.getManufacturer()),
                    m.getPrice(),
                    m.getQuantity(),
                    m.getReorderLevel() == null ? "NULL" : m.getReorderLevel(),
                    nullableString(m.getSupplier()),
                    m.getIsNarcotic() != null && m.getIsNarcotic());
        });
        writer.println();
    }

    private void extractInvoices(PrintWriter writer) {
        writer.println("-- Table: invoices");
        invoiceRepository.findAll().forEach(i -> {
            writer.printf("INSERT INTO invoices (id, invoice_number, date, total_amount, is_returned, discount_amount, discount_percentage) " +
                    "VALUES (%d, '%s', '%s', %s, %b, %s, %s);\n",
                    i.getId(),
                    i.getInvoiceNumber(),
                    i.getDate().format(formatter),
                    i.getTotalAmount(),
                    i.isReturned(),
                    i.getDiscountAmount(),
                    i.getDiscountPercentage());
        });
        writer.println();
    }

    private void extractInvoiceItems(PrintWriter writer) {
        writer.println("-- Table: invoice_items");
        invoiceItemRepository.findAll().forEach(item -> {
            writer.printf("INSERT INTO invoice_items (invoice_id, medicine_id, quantity, price, returned_quantity) " +
                    "VALUES (%d, %d, %d, %s, %d);\n",
                    item.getInvoice().getId(),
                    item.getMedicine().getBrandId(),
                    item.getQuantity(),
                    item.getPrice(),
                    item.getReturnedQuantity());
        });
        writer.println();
    }

    private String escapeSql(String val) {
        if (val == null) return "NULL";
        return val.replace("'", "''");
    }

    private String nullableString(String val) {
        if (val == null) return "NULL";
        return "'" + escapeSql(val) + "'";
    }
}

