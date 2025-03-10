package com.example.makemyshow.config;

import com.example.makemyshow.model.user.Role;
import com.example.makemyshow.model.user.UserRole;
import com.example.makemyshow.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        // Check if roles exist, if not create them
        if (roleRepository.count() == 0) {
            // Create roles
            Role customerRole = new Role(UserRole.ROLE_CUSTOMER);
            customerRole.setDescription("Regular customers who can book tickets");

            Role theaterOwnerRole = new Role(UserRole.ROLE_THEATER_OWNER);
            theaterOwnerRole.setDescription("Theater owners who can manage theaters and shows");

            Role adminRole = new Role(UserRole.ROLE_ADMIN);
            adminRole.setDescription("Administrators who can manage the entire system");

            // Save roles
            roleRepository.saveAll(Arrays.asList(customerRole, theaterOwnerRole, adminRole));

            System.out.println("Database initialized with default roles");
        }
    }
}