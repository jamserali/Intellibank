package com.intellibank.security;

import com.intellibank.api.AuthController.TokenResponse;
import com.intellibank.domain.UserAccount;
import com.intellibank.repository.UserAccountRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository accounts;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager, UserAccountRepository accounts, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.accounts = accounts;
        this.jwtService = jwtService;
    }

    public TokenResponse login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(normalizedEmail, password));
        UserAccount account = accounts.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"));
        return new TokenResponse(jwtService.createToken(account), "Bearer", jwtService.expirationSeconds(), account.getRole().name());
    }
}
