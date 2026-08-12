# Checkout benchmark

Only one test is needed: [04_checkout_order.js](04_checkout_order.js).

```bash
K6_USER_EMAIL='customer@example.com' K6_USER_PASSWORD='...' k6 run k6/scenarios/04_checkout_order.js
```

It runs 10 sequential checkout journeys by default (`K6_ITERATIONS=30` changes that) and prints:

- `http_req_duration`, `http_reqs`: gateway request latency and throughput.
- `feign_path_latency` / `feign_path_success`: end-to-end latency and success rate of Order and Payment endpoints; each calls another service through Feign.
- `rabbitmq_payment_to_order_latency` / `rabbitmq_payment_to_order_success`: time and success rate from accepted payment callback until the order becomes `COMPLETED` + `CONFIRMED` through RabbitMQ.
- `checkout_success`: successful end-to-end journeys.

Use a dedicated user that already has one delivery address and product `1` / size `256GB` in stock. The script is deliberately one VU because one cart belongs to one user; change it to multiple accounts only when testing concurrency.

## VNPay risks to state in a defense

The current callback accepts any request with `vnp_TxnRef` and a `00` response; it does **not** verify `vnp_SecureHash`, amount, or transaction state ([PaymentController](/Users/tphuc263/Project/Kyro/backend/payment-service/src/main/java/com/kyro/payment/PaymentController.java:67), [PaymentService](/Users/tphuc263/Project/Kyro/backend/payment-service/src/main/java/com/kyro/payment/PaymentService.java:147)). Therefore a forged or replayed callback can mark a payment completed.

After the payment DB transaction commits, the event is sent directly with `RabbitTemplate.convertAndSend`; there is no transactional outbox, publisher confirm, retry or dead-letter policy ([PaymentStatusEventPublisher](/Users/tphuc263/Project/Kyro/backend/payment-service/src/main/java/com/kyro/payment/messaging/PaymentStatusEventPublisher.java:23)). If the broker send fails at that point, payment can be `COMPLETED` while the order remains pending. The benchmark detects that divergence as a 10-second convergence timeout; it does not fix it.
