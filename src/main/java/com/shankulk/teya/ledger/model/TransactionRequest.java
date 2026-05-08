package com.shankulk.teya.ledger.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotNull(message = "Type must not be null") TransactionType type,
        @NotNull(message = "Amount must not be null") @Positive(message = "Amount must be positive") BigDecimal amount
) {}
