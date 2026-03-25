package com.pharmacy.pharmacy_system;

import com.pharmacy.pharmacy_system.model.Role;
import com.pharmacy.pharmacy_system.model.User;
import com.pharmacy.pharmacy_system.repository.MedicineRepository;
import com.pharmacy.pharmacy_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the database with default users and sample medicines on first startup.
 * Users are only created if none exist. Medicines are only logged if table is empty.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(MedicineRepository medicineRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // --- Seed Default Users ---
            if (userRepository.count() == 0) {
                User admin = new User(
                        "admin",
                        passwordEncoder.encode("admin123"),
                        Role.ROLE_ADMIN
                );
                User pharmacist = new User(
                        "pharmacist",
                        passwordEncoder.encode("pharma123"),
                        Role.ROLE_PHARMACIST
                );
                userRepository.save(admin);
                userRepository.save(pharmacist);
                System.out.println("[DataSeeder] ✅ Created default users: admin (ROLE_ADMIN), pharmacist (ROLE_PHARMACIST)");
            } else {
                System.out.println("[DataSeeder] Users already exist — skipping user seed.");
            }

            // --- Report Medicine Count ---
            long count = medicineRepository.count();
            if (count == 0) {
                System.out.println("[DataSeeder] No medicines found — please import your medicine.csv via MySQL Workbench.");
            } else {
                System.out.println("[DataSeeder] Found " + count + " medicines in database.");
            }
        };
    }
}
