package com.teamforge.config;

import com.teamforge.entity.Account;
import com.teamforge.entity.Role;
import com.teamforge.repository.AccountRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    @ConditionalOnProperty(
            name = "teamforge.admin.bootstrap-enabled",
            havingValue = "true"
    )
    public CommandLineRunner createInitialAdmin(
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {

        return args -> {

            String email = System.getenv("TEAMFORGE_ADMIN_EMAIL");
            String password = System.getenv("TEAMFORGE_ADMIN_PASSWORD");
            String name = System.getenv("TEAMFORGE_ADMIN_NAME");

            if (email == null || email.isBlank()
                    || password == null || password.length() < 12) {

                throw new IllegalStateException(
                        "Admin email and password (minimum 12 characters) are required."
                );
            }

            email = email.trim().toLowerCase();

            if (accountRepository.existsByEmail(email)) {
                System.out.println(
                        "Admin bootstrap skipped: account already exists."
                );
                return;
            }

            Account admin = new Account();

            admin.setName(
                    name == null || name.isBlank()
                            ? "TeamForge Admin"
                            : name.trim()
            );

            admin.setEmail(email);

            admin.setPassword(
                    passwordEncoder.encode(password)
            );

            admin.setRole(Role.ADMIN);

            admin.setCandidate(null);

            accountRepository.save(admin);

            System.out.println(
                    "Initial TeamForge admin account created."
            );
        };
    }
}