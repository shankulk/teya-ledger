package com.shankulk.teya.ledger.controller;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BalanceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void getBalance_emptyAccount_returnsZero() throws Exception {
        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @Order(2)
    void getBalance_afterDeposit_reflectsDepositedAmount() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": 100.00 }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.0));
    }

    @Test
    @Order(3)
    void getBalance_afterMultipleDeposits_returnsSumOfDeposits() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": 50.00 }
                                """))
                .andExpect(status().isCreated());

        // store now has: 100 + 50 = 150
        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(150.0));
    }

    @Test
    @Order(4)
    void getBalance_afterWithdrawal_returnsDepositsMinusWithdrawals() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "WITHDRAWAL", "amount": 30.00 }
                                """))
                .andExpect(status().isCreated());

        // store now has: 100 + 50 - 30 = 120
        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(120.0));
    }

    @Test
    @Order(5)
    void getBalance_afterFractionalDeposit_returnsExactBalance() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": 10.55 }
                                """))
                .andExpect(status().isCreated());

        // store now has: 120 + 10.55 = 130.55
        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(130.55));
    }

    @Test
    @Order(6)
    void getBalance_afterFractionalWithdrawal_returnsExactBalance() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "WITHDRAWAL", "amount": 5.33 }
                                """))
                .andExpect(status().isCreated());

        // store now has: 130.55 - 5.33 = 125.22
        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(125.22));
    }

    @Test
    @Order(7)
    void getBalance_afterSmallestFractionalDeposit_returnsExactBalance() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "type": "DEPOSIT", "amount": 0.01 }
                                """))
                .andExpect(status().isCreated());

        // store now has: 125.22 + 0.01 = 125.23
        mockMvc.perform(get("/api/v1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(125.23));
    }
}
