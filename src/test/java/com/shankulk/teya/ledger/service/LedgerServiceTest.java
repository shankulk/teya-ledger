package com.shankulk.teya.ledger.service;

import com.shankulk.teya.ledger.exception.InsufficientBalanceException;
import com.shankulk.teya.ledger.model.TransactionRequest;
import com.shankulk.teya.ledger.model.TransactionType;
import com.shankulk.teya.ledger.store.LedgerStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void record_withdrawal_whenSufficientBalance_returnsTransactionWithCorrectFields() {
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("100.00")));

        var result = service.record(new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("40.00")));

        assertThat(result.id()).isNotNull();
        assertThat(result.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(result.timestamp()).isNotNull();
    }

    @Test
    void record_withdrawal_whenSufficientBalance_reducesBalance() {
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("100.00")));

        service.record(new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("40.00")));

        assertThat(service.getBalance()).isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void record_withdrawal_whenExactBalance_succeeds() {
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("50.00")));

        service.record(new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("50.00")));

        assertThat(service.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void record_withdrawal_whenInsufficientBalance_throwsInsufficientBalanceException() {
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("30.00")));

        assertThatThrownBy(() -> service.record(new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("31.00"))))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessage("Insufficient balance");
    }

    @Test
    void record_withdrawal_whenInsufficientBalance_doesNotPersistTransaction() {
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("30.00")));

        assertThatThrownBy(() -> service.record(new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("31.00"))))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(store.getAll()).hasSize(1);
    }

    @Test
    void getBalance_withNoTransactions_returnsZero() {
        assertThat(service.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getBalance_afterDepositsAndWithdrawals_returnsCorrectBalance() {
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("100.00")));
        service.record(new TransactionRequest(TransactionType.DEPOSIT, new BigDecimal("50.00")));
        service.record(new TransactionRequest(TransactionType.WITHDRAWAL, new BigDecimal("30.00")));

        assertThat(service.getBalance()).isEqualByComparingTo(new BigDecimal("120.00"));
    }
}
