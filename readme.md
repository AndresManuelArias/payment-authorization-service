version java 17
## Run proyect

```sh
./mvnw spring-boot:run

```

## view proyect run

<a href="http://localhost:8080/actuator/health"> proyect </a>



## Endpoint 

```sh
curl -X POST http://localhost:8080/api/payments/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TX-1001",
    "customerId": "CUST-001",
    "amount": 25000,
    "currency": "COP",
    "merchantId": "MER-900",
    "paymentMethod": "CARD"
  }'

``` 

## Test proyect

```sh
./mvnw test
```

