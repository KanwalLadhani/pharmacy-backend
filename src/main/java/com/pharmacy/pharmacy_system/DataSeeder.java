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
            // --- Force-Reset Admin Password on Every Startup ---
            User admin = userRepository.findByUsername("admin")
                    .orElseGet(() -> {
                        User newAdmin = new User();
                        newAdmin.setUsername("admin");
                        newAdmin.setRole(Role.ROLE_ADMIN);
                        return newAdmin;
                    });
            admin.setPassword(passwordEncoder.encode("admin123"));
            userRepository.save(admin);
            System.out.println("[DataSeeder] 🔐 Admin password has been reset to: admin / admin123");
            // Seed logic for other roles or data can be added here if missing
        };
    }
}