# Payment Authorization Service

Microservicio de autorización de pagos con arquitectura hexagonal, validación de reglas de negocio, integración con proveedor antifraude simulado, resiliencia con Resilience4j y caché con Caffeine.

---

## Requisitos

- Java 17+
- Maven 3.x (o `./mvnw`)

## Ejecución

```bash
./mvnw spring-boot:run
```

---

## API REST

### Autorizar un pago

```bash
curl -s -X POST http://localhost:8080/api/payments/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TX-001",
    "customerId": "CUST-123",
    "amount": 1500.00,
    "currency": "COP",
    "merchantId": "MER-456",
    "paymentMethod": "CARD"
  }' | jq
```

**Respuesta exitosa (200 OK):**

```json
{
  "transactionId": "TX-001",
  "status": "APPROVED",
  "authorizationCode": "AUTH-abc123",
  "message": "Payment authorized"
}
```

**Respuesta con validación fallida (400 Bad Request):**

```json
{
  "transactionId": "El transactionId es obligatorio",
  "amount": "El monto debe ser mayor a cero"
}
```

### Consultar una transacción

```bash
curl -s http://localhost:8080/api/payments/TX-001 | jq
```

**Respuesta cuando existe (200 OK):**

```json
{
  "transactionId": "TX-001",
  "status": "APPROVED",
  "authorizationCode": "AUTH-abc123",
  "message": "Payment authorized"
}
```

**Respuesta cuando no existe (404 Not Found):**

```json

```

---

## Tests

```bash
./mvnw test
```
