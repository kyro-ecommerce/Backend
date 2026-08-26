# K6: giao tiếp giữa các service

Phần báo cáo chỉ cần hai luồng đại diện:

| Kịch bản | Luồng | Số liệu chính |
| --- | --- | --- |
| Feign đồng bộ | Gateway → Order → Auth/Cart/Catalog | `synchronous_path_latency` p95, `technical_success_rate`, `accepted_requests` |
| RabbitMQ bất đồng bộ | Payment callback → RabbitMQ → Order | `rabbitmq_propagation_latency` p95, `rabbitmq_observation_success_rate`, backlog |

Đây là latency end-to-end của luồng có Feign, không phải thời gian riêng bên trong từng Feign call. Scenario tạo VNPAY order ở trạng thái chờ thanh toán để vẫn kiểm tra giữ stock nhưng không phát hàng nghìn email qua SMTP. Không cần benchmark mọi endpoint; hai luồng trên đại diện cho hai kiểu giao tiếp trong hệ thống.

## Cách chạy

```bash
task run
task perf:feign
task perf:rabbitmq
task perf:reset
```

Mỗi test warm-up 10 giây, sau đó chạy mặc định 25 request/giây trong 30 giây. Muốn đổi tải:

```bash
PERF_RATE=50 PERF_DURATION=60s task perf:feign
PERF_RATE=50 PERF_DURATION=60s task perf:rabbitmq
```

Muốn lấy ba measured run cho báo cáo:

```bash
PERF_RUNS=3 PERF_RATE=50 PERF_DURATION=60s task perf:feign
PERF_RUNS=3 PERF_RATE=50 PERF_DURATION=60s task perf:rabbitmq
```

Kết quả được tách tại `k6/results/feign/` và `k6/results/rabbitmq/` và đã được Git ignore. Trong báo cáo, với mỗi kịch bản chỉ cần ghi: tải yêu cầu, throughput thực tế (`accepted_requests`/thời gian), p95 latency, success rate và backlog sau drain trong `runs.csv`. Chỉ publish khi mọi measured run đạt success rate từ 99%, p95 không quá 2 giây, không drop iteration và queue drain về 0.

Không dùng một capacity run ngay sau fresh Docker boot làm steady-state headline. Warm-up mặc định chỉ là smoke warm-up; với capacity cao, cần ổn định Eureka/Gateway và precondition JVM, connection pools cùng DB working set trước khi reset fixture và bắt đầu measured runs. Báo cáo phải ghi rõ warm hay cold scope. Kết quả 250 req/s trong `docs/verification.md` là warm local capacity; ba published run không dùng index tạm hoặc schema khác repository.

Các scenario dùng fixture user và JWT ký local bằng `JWT_SECRET`, không cần tài khoản customer thật. RabbitMQ scenario ký callback bằng `VNPAY_HASH_SECRET` và kiểm tra `vnp_TmnCode`, amount, response code cùng transaction status như implementation hiện tại.

`task perf:payment-failure` là test phụ: callback bị từ chối phải làm Payment và payment projection trong Order cùng thành `FAILED`. Không đưa kết quả này vào bảng hiệu năng chính. Callback được ký đúng nhưng vẫn là synthetic local traffic, không đo mạng VNPay thật.

Rủi ro cần biết khi phản biện: publish sau khi DB commit có thể mất event nếu RabbitMQ lỗi; consumer giữ stock chưa idempotent và chưa có DLQ/retry policy rõ ràng. Giải pháp production tiếp theo là transactional outbox, idempotent consumer và DLQ/retry có quan sát.
