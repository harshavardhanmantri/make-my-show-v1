package com.example.makemyshow.config;

import com.example.makemyshow.model.user.Role;
import com.example.makemyshow.model.user.User;
import com.example.makemyshow.model.user.UserRole;
import com.example.makemyshow.repository.RoleRepository;
import com.example.makemyshow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Initialize roles if they don't exist
        initRoles();

//        // Create admin user if it doesn't exist
//        createAdminIfNotExists();
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            System.out.println("Initializing roles...");

            Role customerRole = new Role(UserRole.ROLE_CUSTOMER);
            customerRole.setDescription("Regular customers who can book tickets");

            Role theaterOwnerRole = new Role(UserRole.ROLE_THEATER_OWNER);
            theaterOwnerRole.setDescription("Theater owners who can manage theaters and shows");

            Role adminRole = new Role(UserRole.ROLE_ADMIN);
            adminRole.setDescription("Administrators who can manage the entire system");

            roleRepository.save(customerRole);
            roleRepository.save(theaterOwnerRole);
            roleRepository.save(adminRole);

            System.out.println("Roles initialized successfully");
        }
    }

//    private void createAdminIfNotExists() {
//        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
//            System.out.println("Creating admin user...");
//
//            // Create admin user
//            User adminUser = new User();
//            adminUser.setEmail("admin@example.com");
//            adminUser.setPassword(passwordEncoder.encode("Admin@123"));
//            adminUser.setFullName("System Admin");
//            adminUser.setPhoneNumber("1234567890");
//            adminUser.setActive(true);
//            adminUser.setEmailVerified(true);
//            adminUser.setCreatedAt(LocalDateTime.now());
//            adminUser.setUpdatedAt(LocalDateTime.now());
//
//            // Save user first to get ID
//            User savedAdmin = userRepository.save(adminUser);
//
//            // Set admin role
//            Set<Role> roles = new HashSet<>();
//            Role adminRole = roleRepository.findByName(UserRole.ROLE_ADMIN)
//                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
//            roles.add(adminRole);
//            savedAdmin.setRoles(roles);
//
//            userRepository.save(savedAdmin);
//
//            System.out.println("Admin user created successfully");
//        } else {
//            System.out.println("Admin user already exists");
//        }
//    }
}