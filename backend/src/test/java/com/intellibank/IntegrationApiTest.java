package com.intellibank;

import com.intellibank.repository.ValuationRepository;
import com.intellibank.repository.UserAccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "intellibank.demo-users.admin-password=Admin123!",
        "intellibank.demo-users.banker-password=Banker123!",
        "intellibank.demo-users.analyst-password=Analyst123!",
        "intellibank.demo-users.viewer-password=Viewer123!"
})
@AutoConfigureMockMvc
class IntegrationApiTest {
    @Autowired private MockMvc mvc;
    @Autowired private ValuationRepository valuations;
    @Autowired private UserAccountRepository accounts;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void customerRegistrationPersistsProfileAndCannotAccessStaffApis() throws Exception {
        String email = "customer-" + java.util.UUID.randomUUID() + "@example.com";
        String registration = objectMapper.writeValueAsString(java.util.Map.of(
            "fullName", "Demo Customer", "email", email, "phone", "+1 555 010 1234",
            "password", "Customer12345!", "confirmPassword", "Customer12345!", "acceptTerms", true,
            "role", "ADMIN"));
        String response = mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(registration))
            .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("CUSTOMER"))
            .andReturn().getResponse().getContentAsString();
        String customerToken = objectMapper.readTree(response).get("accessToken").asText();
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + customerToken))
            .andExpect(status().isOk()).andExpect(jsonPath("$.fullName").value("Demo Customer"))
            .andExpect(jsonPath("$.phone").value("+1 555 010 1234"));
        mvc.perform(get("/api/v1/admin/customers").header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/clients").header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/analytics/allocation").header("Authorization", "Bearer " + customerToken)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
        var account = accounts.findByEmailIgnoreCase(email).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("Customer12345!", account.getPasswordHash())).isTrue();
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(registration)).andExpect(status().isBadRequest());
        String adminToken = token("admin@intellibank.local", "Admin123!");
        mvc.perform(get("/api/v1/admin/customers").header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk());
    }

    @Test
    void invalidRegistrationIsRejected() throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content("""
            {"fullName":"", "email":"invalid", "phone":"bad", "password":"weak", "confirmPassword":"different", "acceptTerms":false}
            """))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.email").exists())
            .andExpect(jsonPath("$.fields.password").exists()).andExpect(jsonPath("$.fields.acceptTerms").exists());
    }

    @Test
    void demoPasswordsAreStoredAsHashesForAllRoles() {
        for (String role : new String[]{"admin", "banker", "analyst", "viewer"}) {
            var account = accounts.findByEmailIgnoreCase(role + "@intellibank.local").orElseThrow();
            org.assertj.core.api.Assertions.assertThat(account.getPasswordHash()).isNotEqualTo(
                    role.substring(0, 1).toUpperCase() + role.substring(1) + "123!");
            org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches(
                    role.substring(0, 1).toUpperCase() + role.substring(1) + "123!", account.getPasswordHash())).isTrue();
        }
    }

    @Test
    void deterministicDemoDatasetAndAnalyticsAreAvailable() throws Exception {
        String token = token("viewer@intellibank.local", "Viewer123!");
        mvc.perform(get("/api/v1/clients").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(15));
        mvc.perform(get("/api/v1/investments").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(60));
        String clientsBody = mvc.perform(get("/api/v1/clients").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        String clientId = objectMapper.readTree(clientsBody).get(0).get("id").asText();
        mvc.perform(get("/api/v1/investments/client/" + clientId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].clientId").value(clientId));
        org.assertj.core.api.Assertions.assertThat(valuations.count()).isEqualTo(720);
        mvc.perform(get("/api/v1/dashboard/trends").param("months", "12").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(12));
        mvc.perform(get("/api/v1/analytics/top-clients").param("limit", "3").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3));
        String summaryBody = mvc.perform(get("/api/v1/dashboard/summary").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientsWithHoldings").value(15))
                .andExpect(jsonPath("$.totalRoi").isNumber())
                .andExpect(jsonPath("$.profitableClients").isNumber())
                .andExpect(jsonPath("$.lossMakingClients").isNumber())
                .andReturn().getResponse().getContentAsString();
        JsonNode summary = objectMapper.readTree(summaryBody);
        var expectedRoi = summary.get("economicReturn").decimalValue().multiply(java.math.BigDecimal.valueOf(100))
                .divide(summary.get("investedAmount").decimalValue(), 2, java.math.RoundingMode.HALF_UP);
        org.assertj.core.api.Assertions.assertThat(summary.get("totalRoi").decimalValue()).isEqualByComparingTo(expectedRoi);
        String allocationBody = mvc.perform(get("/api/v1/analytics/allocation").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].assetType").isNotEmpty())
                .andExpect(jsonPath("$[0].currentValue").isNumber())
                .andReturn().getResponse().getContentAsString();
        int allocatedPositions = 0;
        for (JsonNode allocation : objectMapper.readTree(allocationBody)) {
            allocatedPositions += allocation.get("investmentCount").asInt();
        }
        org.assertj.core.api.Assertions.assertThat(allocatedPositions).isEqualTo(60);
        mvc.perform(get("/api/v1/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void invalidLedgerInputReturnsStructuredValidationError() throws Exception {
        String token = token("banker@intellibank.local", "Banker123!");
        mvc.perform(post("/api/v1/revenues").header("Authorization", "Bearer " + token).contentType("application/json")
                        .content("""
                                {"description":"","category":"Fees","amount":-1,"date":"2025-01-01"}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fields.description").exists())
                .andExpect(jsonPath("$.fields.amount").exists());
    }

    @Test
    void authenticationAndRoleAuthorizationAreEnforced() throws Exception {
        mvc.perform(get("/api/v1/clients")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/investments/client/" + java.util.UUID.randomUUID())).andExpect(status().isUnauthorized());
        String viewerToken = token("viewer@intellibank.local", "Viewer123!");
        mvc.perform(post("/api/v1/clients").header("Authorization", "Bearer " + viewerToken)
                        .contentType("application/json")
                        .content("""
                                {"name":"Blocked Client","email":"blocked@example.com","riskProfile":"Balanced","industry":"Technology"}
                                """))
                .andExpect(status().isForbidden());
        String bankerToken = token("banker@intellibank.local", "Banker123!");
        String createdClient = mvc.perform(post("/api/v1/clients").header("Authorization", "Bearer " + bankerToken)
                        .contentType("application/json")
                        .content("""
                                {"name":"Authorized Client","email":"authorized@example.com","riskProfile":"Balanced","industry":"Technology"}
                                """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String clientId = objectMapper.readTree(createdClient).get("id").asText();
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/clients/" + clientId)
                        .header("Authorization", "Bearer " + bankerToken))
                .andExpect(status().isForbidden());
        String adminToken = token("admin@intellibank.local", "Admin123!");
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/clients/" + clientId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content("""
                                {"email":"admin@intellibank.local","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String token(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content(objectMapper.writeValueAsString(new Credentials(email, password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.get("accessToken").asText();
    }

    private record Credentials(String email, String password) {}
}
