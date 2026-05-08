package com.shankulk.teya.ledger.controller;

import com.shankulk.teya.ledger.model.BalanceResponse;
import com.shankulk.teya.ledger.model.Transaction;
import com.shankulk.teya.ledger.model.TransactionRequest;
import com.shankulk.teya.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction recordTransaction(@Valid @RequestBody TransactionRequest request) {
        return ledgerService.record(request);
    }

    @GetMapping("/balance")
    public BalanceResponse getBalance() {
        return new BalanceResponse(ledgerService.getBalance());
    }
}
