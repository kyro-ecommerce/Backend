# K6: giao tiếp giữa các service

Phần báo cáo chỉ cần hai luồng đại diện:

| Kịch bản | Luồng | Số liệu chính |
| --- | --- | --- |
| Feign đồng bộ | Gateway → Order → Auth/Cart/Catalog | `synchronous_path_latency` p95, `technical_success_rate`, `events_produced` |
| RabbitMQ bất đồng bộ | Payment callback → RabbitMQ → Order | `rabbitmq_propagation_latency` p95, `rabbitmq_observation_success_rate`, backlog |

Đây là latency end-to-end của luồng có Feign, không phải thời gian riêng bên trong từng Feign call. Không cần benchmark mọi endpoint; hai luồng trên đại diện cho hai kiểu giao tiếp trong hệ thống.

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

Kết quả được tách tại `k6/results/feign/` và `k6/results/rabbitmq/` và đã được Git ignore. Trong báo cáo, với mỗi kịch bản chỉ cần ghi: tải yêu cầu, throughput thực tế (`events_produced`/thời gian), p95 latency, success rate và backlog sau drain trong `runs.csv`.

`task perf:payment-failure` là test phụ để tìm hiểu rủi ro VNPay: callback lỗi phải làm Payment và Order cùng thành `FAILED`. Không cần đưa kết quả này vào bảng hiệu năng chính. Callback là synthetic, không đo mạng VNPay thật.

Rủi ro cần biết khi phản biện: callback hiện chưa kiểm tra chữ ký; publish sau khi DB commit có thể mất event nếu RabbitMQ lỗi; consumer chưa có idempotency/state-transition guard. Giải pháp khi cần độ tin cậy production là kiểm tra chữ ký, transactional outbox và idempotent consumer.
