package com.shankulk.teya.ledger.store;

import com.shankulk.teya.ledger.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class LedgerStore {

    private final List<Transaction> transactions = new ArrayList<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void add(Transaction transaction) {
        lock.writeLock().lock();
        try {
            transactions.add(transaction);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Transaction> getAll() {
        lock.readLock().lock();
        try {
            return List.copyOf(transactions);
        } finally {
            lock.readLock().unlock();
        }
    }
}
