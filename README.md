# Tiny Ledger

A simple Spring Boot based REST API for recording deposits and withdrawals, querying the current balance, and viewing transaction history. Data is stored in memory. No database required.

## Requirements

- Java 21
- No other software installation required

## Running the application

```bash
./gradlew bootRun
```

The server starts on **http://localhost:8080**.

To run the tests:

```bash
./gradlew test
```

## API

Base path: `/api/v1`

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/transactions` | Record a deposit or withdrawal |
| `GET` | `/api/v1/transactions` | View transaction history |
| `GET` | `/api/v1/balance` | View current balance |

All requests and responses use `Content-Type: application/json`.

### Error responses

All errors return `{ "error": "<message>" }` with an appropriate HTTP status. No stack traces are exposed.

## curl examples

### Record a deposit

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"type": "DEPOSIT", "amount": 100.00}' | jq
```

```json
{
  "id": "a3f1c2d4-...",
  "type": "DEPOSIT",
  "amount": 100.00,
  "timestamp": "2026-05-08T10:00:00Z"
}
```

### Record a withdrawal

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"type": "WITHDRAWAL", "amount": 40.00}' | jq
```

```json
{
  "id": "b7e2d3a1-...",
  "type": "WITHDRAWAL",
  "amount": 40.00,
  "timestamp": "2026-05-08T10:01:00Z"
}
```

### View current balance

```bash
curl -s http://localhost:8080/api/v1/balance | jq
```

```json
{
  "balance": 60.00
}
```

### View transaction history

```bash
curl -s http://localhost:8080/api/v1/transactions | jq
```

```json
[
  {
    "id": "a3f1c2d4-...",
    "type": "DEPOSIT",
    "amount": 100.00,
    "timestamp": "2026-05-08T10:00:00Z"
  },
  {
    "id": "b7e2d3a1-...",
    "type": "WITHDRAWAL",
    "amount": 40.00,
    "timestamp": "2026-05-08T10:01:00Z"
  }
]
```

### Error: withdrawal exceeds balance

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"type": "WITHDRAWAL", "amount": 9999.00}' | jq
```

```json
{
  "error": "Insufficient balance"
}
```

### Error: invalid amount

```bash
curl -s -X POST http://localhost:8080/api/v1/transactions \
  -H "Content-Type: application/json" \
  -d '{"type": "DEPOSIT", "amount": -50}' | jq
```

```json
{
  "error": "Amount must be positive"
}
```

## Assumptions

- **Single account**: the ledger tracks one account with no concept of users or accounts.
- **In-memory storage**: all data is lost when the application stops. No database setup is required.
- **Amounts are positive decimals**: the `type` field (`DEPOSIT` / `WITHDRAWAL`) determines the direction of the movement.
- **Withdrawals are validated**: a withdrawal that would make the balance negative is rejected with `400 Bad Request`.
- **Timestamps are UTC**: all timestamps are returned as ISO-8601 UTC strings.
- **No authentication**: all endpoints are open, as specified in the requirements.
- **Thread safety**: the in-memory store uses a `ReentrantReadWriteLock`; concurrent reads do not block each other.
