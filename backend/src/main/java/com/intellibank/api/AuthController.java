package com.intellibank.api;
import com.intellibank.security.AuthenticationService;
import com.intellibank.domain.*;
import com.intellibank.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
 private final AuthenticationService authentication;
 private final UserAccountRepository accounts;
 private final CustomerProfileRepository profiles;
 private final PasswordEncoder encoder;
 public AuthController(AuthenticationService authentication, UserAccountRepository accounts, CustomerProfileRepository profiles, PasswordEncoder encoder) {
 this.authentication=authentication; this.accounts=accounts; this.profiles=profiles; this.encoder=encoder;
 }
 @PostMapping("/login") public TokenResponse login(@Valid @RequestBody LoginRequest request) { return authentication.login(request.email(), request.password()); }
 @PostMapping("/register") @Transactional
 public TokenResponse register(@Valid @RequestBody RegistrationRequest request) {
 if (!request.password().equals(request.confirmPassword())) throw new IllegalArgumentException("Passwords must match");
 if (request.password().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes");
 if (accounts.existsByEmailIgnoreCase(request.email().trim())) throw new IllegalArgumentException("Unable to register this email. Try signing in.");
 var account=accounts.saveAndFlush(new UserAccount(request.email(),encoder.encode(request.password()),UserRole.CUSTOMER));
 profiles.saveAndFlush(new CustomerProfile(account.getId(),request.fullName(),request.phone()));
 return authentication.login(account.getEmail(),request.password());
 }
 @GetMapping("/me") public Map<String,Object> me(Authentication principal) {
 var account=accounts.findByEmailIgnoreCase(principal.getName()).orElseThrow();
 var result=new LinkedHashMap<String,Object>();
 result.put("email",account.getEmail()); result.put("role",account.getRole().name());
 profiles.findById(account.getId()).ifPresent(p -> { result.put("fullName",p.getFullName()); result.put("phone",p.getPhone()); });
 return result;
 }
 public record LoginRequest(@NotBlank @Email @Size(max=180) String email, @NotBlank @Size(max=72) String password) {}
 public record RegistrationRequest(@NotBlank @Size(max=120) String fullName,
 @NotBlank @Email @Size(max=180) String email,
 @NotBlank @Pattern(regexp="[+0-9() -]{7,25}",message="Enter a valid phone number") String phone,
 @NotBlank @Size(min=12,max=72) @Pattern(regexp="(?s)(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).*",message="Use uppercase, lowercase, number and symbol") String password,
 @NotBlank String confirmPassword, @AssertTrue(message="Accept the terms to continue") boolean acceptTerms) {}
 public record TokenResponse(String accessToken,String tokenType,long expiresIn,String role) {}
}
