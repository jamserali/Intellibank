package com.intellibank.config;

import com.intellibank.domain.UserAccount;
import com.intellibank.domain.UserRole;
import com.intellibank.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="intellibank.demo.enabled", havingValue="true", matchIfMissing=true)
@Component
@Order(0)
public class DemoAccountsInitializer implements CommandLineRunner {
    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final Map<UserRole, String> passwords;

    public DemoAccountsInitializer(UserAccountRepository accounts, PasswordEncoder passwordEncoder,
                                   @Value("${intellibank.demo-users.admin-password}") String adminPassword,
                                   @Value("${intellibank.demo-users.banker-password}") String bankerPassword,
                                   @Value("${intellibank.demo-users.analyst-password}") String analystPassword,
                                   @Value("${intellibank.demo-users.viewer-password}") String viewerPassword) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.passwords = Map.of(UserRole.ADMIN, adminPassword, UserRole.BANKER, bankerPassword,
                UserRole.ANALYST, analystPassword, UserRole.VIEWER, viewerPassword);
    }

    @Override
    @Transactional
    public void run(String... args) {
        passwords.forEach((role, password) -> {
            String email = role.name().toLowerCase() + "@intellibank.local";
            if (!accounts.existsByEmailIgnoreCase(email)) {
                accounts.save(new UserAccount(email, passwordEncoder.encode(password), role));
            }
        });
    }
}
