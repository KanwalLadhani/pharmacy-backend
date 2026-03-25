package com.pharmacy.pharmacy_system;

import com.pharmacy.pharmacy_system.model.Role;
import com.pharmacy.pharmacy_system.model.User;
import com.pharmacy.pharmacy_system.repository.MedicineRepository;
import com.pharmacy.pharmacy_system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(MedicineRepository medicineRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // --- Seed/Reset Admin User ---
            // This ensures admin/admin123 ALWAYS works, even if the DB already has data.
            User admin = userRepository.findByUsername("admin").orElse(new User());

            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ROLE_ADMIN);

            userRepository.save(admin);
            System.out.println("[DataSeeder] 🔐 Admin account verified/reset: admin / admin123");

            // --- Seed Pharmacist (Only if missing) ---
            if (userRepository.findByUsername("pharmacist").isEmpty()) {
                User pharmacist = new User(
                        "pharmacist",
                        passwordEncoder.encode("pharma123"),
                        Role.ROLE_PHARMACIST);
                userRepository.save(pharmacist);
                System.out.println("[DataSeeder] ✅ Created pharmacist user: pharmacist / pharma123");
            }

            // --- Report Medicine Count ---
            long count = medicineRepository.count();
            if (count == 0) {
                System.out.println("[DataSeeder] ⚠️ No medicines found — please import your medicine data.");
            } else {
                System.out.println("[DataSeeder] 📦 Found " + count + " medicines in database.");
            }
        };
    }
}