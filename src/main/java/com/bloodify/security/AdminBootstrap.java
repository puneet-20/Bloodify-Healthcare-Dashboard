package com.bloodify.security;

import com.bloodify.model.AppUser;
import com.bloodify.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminBootstrap(AppUserRepository users,
                          PasswordEncoder encoder,
                          @Value("${ADMIN_USERNAME:}") String adminUsername,
                          @Value("${ADMIN_PASSWORD:}") String adminPassword) {
        this.users = users;
        this.encoder = encoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        if (adminUsername.isBlank() || adminPassword.isBlank()) {
            log.warn("No users exist and ADMIN_USERNAME / ADMIN_PASSWORD are not set. "
                    + "Nobody will be able to log in.");
            return;
        }
        if (adminPassword.length() < 10) {
            log.warn("ADMIN_PASSWORD must be at least 10 characters. Admin was not created.");
            return;
        }
        users.save(new AppUser(adminUsername.trim(), encoder.encode(adminPassword), "ADMIN"));
        log.info("Created initial admin user '{}'.", adminUsername.trim());
    }
}