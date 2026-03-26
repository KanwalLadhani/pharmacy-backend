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
            // --- Seed Initial Admin (If missing) ---
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);
                userRepository.save(admin);
                System.out.println("[DataSeeder] 🔐 Created initial admin account: admin / admin123");
            }
            // Seed logic for other roles or data can be added here if missing
        };
    }
}