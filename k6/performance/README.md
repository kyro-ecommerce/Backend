# K6: số liệu để báo cáo và phản biện

K6 ở đây là **integration/load test**, không thay thế unit test. Nó đo từ API Gateway qua các service; vì vậy `http_req_duration` và `synchronous_path_latency` là độ trễ API end-to-end, **không phải** latency riêng của một Feign call. Muốn số Feign riêng phải thêm tracing/metrics vào ứng dụng, chưa cần làm nếu thầy chỉ yêu cầu kết quả thực tế của hệ thống.

## Chạy tối thiểu

Chỉ chạy local Docker, có `k6`, `docker`, `task`, và `JWT_SECRET` trong `.env`. Không chạy với VNPay thật: callback đều là synthetic, không gọi mạng VNPay.

```bash
task run
task perf:payment-failure  # VNPay trả lỗi 24 -> Payment FAILED phải đến Order qua RabbitMQ
task perf:capacity         # 10, 25, 50, 100, 200 RPS: Payment, Order và checkout
task perf:spike            # 1,000 user: 10 SKU và tranh chấp 1 SKU
task perf:reset            # dọn fixture và khôi phục stock sau khi chụp kết quả
```

Mỗi lần chạy ghi vào `k6/results/`; các file sinh ra được Git ignore. Chỉ đính kèm bảng số liệu cuối vào báo cáo, không commit log/JSON/CSV của từng lần chạy.

| Câu hỏi | Số liệu / file |
| --- | --- |
| Latency API | `http_req_duration` (p(95), p(99)) trong `*.json` |
| Throughput và tỉ lệ thành công | `http_reqs`, `technical_success_rate`, `checkout_success_rate` trong `*.json` |
| RabbitMQ có theo kịp không | `rabbitmq_propagation_latency`, `rabbitmq_observation_success_rate`; backlog và thời gian drain trong `runs.csv`, `*-rabbit.csv` |
| Trạng thái cuối không bị lệch | `*-verify.csv` — Payment và Order phải cùng `FAILED` hoặc `COMPLETED` |

Chỉ kết luận throughput bền vững ở mức RPS cao nhất có mọi threshold pass, backlog sau drain = 0, và `*-verify.csv` không có trạng thái trái kỳ vọng. Ghi rõ cấu hình máy/Docker, version commit, thời lượng và mỗi test chạy lại tối thiểu 3 lần; lấy median p95 và success rate, không lấy một lần chạy đẹp nhất.

## Câu trả lời khi phản biện về VNPay lỗi và bất đồng bộ

`task perf:payment-failure` gửi callback mã `24` vào transaction fixture. Kỳ vọng: Payment lưu `FAILED`, event `payment.status.updated` được RabbitMQ chuyển cho Order, rồi Order cũng thành `FAILED`; K6 đo thời gian đến trạng thái đó. Đây chứng minh được *đường lỗi hiện tại*, không chứng minh độ tin cậy của VNPay thật.

Rủi ro thực tế đang còn:

1. Callback hiện không kiểm tra `vnp_SecureHash`; request giả có thể đổi trạng thái payment.
2. Payment commit xong rồi mới publish RabbitMQ (`AFTER_COMMIT`), nên broker/publish lỗi có thể làm Payment đổi trạng thái nhưng Order không đổi. Cần transactional outbox + retry/monitoring khi nâng mức tin cậy.
3. Consumer không có idempotency/state-transition guard; callback/event lặp hoặc đến sai thứ tự có thể ghi đè trạng thái. Cần event id, unique processed-event và chuyển trạng thái hợp lệ.

Vì vậy hãy nói đúng: “Kết quả xác nhận độ trễ, throughput và eventual consistency trong môi trường local với callback mô phỏng; các rủi ro delivery/duplicate/callback integrity đã được nhận diện, chưa tuyên bố exactly-once hay độ tin cậy của VNPay production.”
