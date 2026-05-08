package com.shankulk.teya.ledger.service;

import com.shankulk.teya.ledger.model.Transaction;
import com.shankulk.teya.ledger.model.TransactionRequest;
import com.shankulk.teya.ledger.store.LedgerStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerStore store;

    public LedgerService(LedgerStore store) {
        this.store = store;
    }

    public Transaction record(TransactionRequest request) {
        var transaction = new Transaction(UUID.randomUUID(), request.type(), request.amount(), Instant.now());
        store.add(transaction);
        return transaction;
    }
}
