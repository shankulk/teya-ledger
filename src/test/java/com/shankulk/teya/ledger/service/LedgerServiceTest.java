package com.shankulk.teya.ledger.service;

import com.shankulk.teya.ledger.model.TransactionRequest;
import com.shankulk.teya.ledger.model.TransactionType;
import com.shankulk.teya.ledger.store.LedgerStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerServiceTest {

    private LedgerStore store;
    private LedgerService service;

    @BeforeEach
    void setUp() {
        store = new LedgerStore();
        service = new LedgerService(store);
    }

    @Test
    void record_deposit_returnsTransactionWithCorrectFields() {
        var request = new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("100.00"));

        var result = service.record(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.timestamp()).isNotNull();
    }

    @Test
    void record_deposit_persistsTransactionToStore() {
        var request = new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("50.00"));

        service.record(request);

        assertThat(store.getAll()).hasSize(1);
        assertThat(store.getAll().get(0).amount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(store.getAll().get(0).type()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void record_multipleDeposits_allPersistedInOrder() {
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("10.00")));
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("20.00")));

        var all = store.getAll();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).amount()).isEqualByComparingTo(new BigDecimal("10.00"));
        assertThat(all.get(1).amount()).isEqualByComparingTo(new BigDecimal("20.00"));
    }
}
