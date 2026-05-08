package com.shankulk.teya.ledger.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postTransaction_deposit_returns201WithAllFields() throws Exception {
        var result = mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": 100.00 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("stackTrace");
    }

    @Test
    void postTransaction_negativeAmount_returns400WithMessage() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": -10.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Amount must be positive"));
    }

    @Test
    void postTransaction_zeroAmount_returns400WithMessage() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": 0 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Amount must be positive"));
    }

    @Test
    void postTransaction_nullAmount_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void postTransaction_nullType_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "amount": 100.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void postTransaction_unknownType_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "INVALID", "amount": 100.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void postTransaction_malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void postTransaction_withdrawal_whenSufficientBalance_returns201WithAllFields() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": 200.00 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "WITHDRAWAL", "amount": 50.00 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").value("WITHDRAWAL"))
                .andExpect(jsonPath("$.amount").value(50.0))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void postTransaction_withdrawal_whenInsufficientBalance_returns400WithMessage() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "WITHDRAWAL", "amount": 999.00 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient balance"));
    }

    @Test
    void getBalance_afterDepositAndWithdrawal_returnsReducedBalance() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": 300.00 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "WITHDRAWAL", "amount": 100.00 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());
    }
}
