package com.intellibank.api;
import com.intellibank.repository.*;
import com.intellibank.domain.UserRole;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/admin")
public class AdminController {
 private final UserAccountRepository accounts;
 private final CustomerProfileRepository profiles;
 public AdminController(UserAccountRepository accounts,CustomerProfileRepository profiles) {this.accounts=accounts;this.profiles=profiles;}
 @GetMapping("/customers") public List<Map<String,Object>> customers() {
 return accounts.findAll().stream().filter(a -> a.getRole()==UserRole.CUSTOMER).map(a -> {
 Map<String,Object> row=new LinkedHashMap<>(); row.put("email",a.getEmail()); row.put("status",a.isEnabled()?"Active":"Inactive");row.put("createdAt",a.getCreatedAt());
 profiles.findById(a.getId()).ifPresent(p -> { row.put("fullName",p.getFullName());row.put("phone",p.getPhone()); });return row;
 }).toList();
 }
}
