package com.shankulk.teya.ledger.service;

import com.shankulk.teya.ledger.exception.InsufficientBalanceException;
import com.shankulk.teya.ledger.model.Transaction;
import com.shankulk.teya.ledger.model.TransactionRequest;
import com.shankulk.teya.ledger.model.TransactionType;
import com.shankulk.teya.ledger.store.LedgerStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
public class LedgerService {

    private final LedgerStore store;

    public LedgerService(LedgerStore store) {
        this.store = store;
    }

    public Transaction record(TransactionRequest request) {
        if (isWithdrawal(request)) {
            if (request.getAmount().compareTo(getBalance()) > 0) {
                throw new InsufficientBalanceException();
            }
        }
        var transaction = new Transaction()
                .id(UUID.randomUUID())
                .type(request.getType())
                .amount(request.getAmount())
                .timestamp(Instant.now());
        store.add(transaction);
        return transaction;
    }

    public List<Transaction> getTransactions() {
        return store.getAll();
    }

    public BigDecimal getBalance() {
        return store.getAll().stream()
                .map(toDepositOrWithdrawal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static Function<Transaction, BigDecimal> toDepositOrWithdrawal() {
        return t -> isDeposit(t) ? t.getAmount() : t.getAmount().negate();
    }

    private static boolean isWithdrawal(TransactionRequest request) {
        return request.getType() == TransactionType.WITHDRAWAL;
    }

    private static boolean isDeposit(Transaction t) {
        return t.getType() == TransactionType.DEPOSIT;
    }
}
