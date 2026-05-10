package com.shankulk.teya.ledger.controller;

import com.shankulk.teya.ledger.api.TransactionApi;
import com.shankulk.teya.ledger.model.BalanceResponse;
import com.shankulk.teya.ledger.model.Transaction;
import com.shankulk.teya.ledger.model.TransactionRequest;
import com.shankulk.teya.ledger.service.LedgerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TransactionController implements TransactionApi {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @Override
    public ResponseEntity<Transaction> recordTransaction(TransactionRequest transactionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerService.record(transactionRequest));
    }

    @Override
    public ResponseEntity<List<Transaction>> getTransactions() {
        return ResponseEntity.ok(ledgerService.getTransactions());
    }

    @Override
    public ResponseEntity<BalanceResponse> getBalance() {
        return ResponseEntity.ok(new BalanceResponse().balance(ledgerService.getBalance()));
    }
}
