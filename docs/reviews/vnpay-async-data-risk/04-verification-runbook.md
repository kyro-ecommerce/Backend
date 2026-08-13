# Runbook kiểm chứng và đối soát

> Chỉ thực hiện test làm thay đổi trạng thái trên sandbox/local với fixture riêng. Không gửi callback giả, kill service hoặc gọi refund trên production.

## 1. Mục tiêu kiểm chứng

Runbook này trả lời bốn câu hỏi:

1. Callback có xác minh đúng nguồn VNPay không?
2. Payment update có được chuyển chắc chắn sang Order không?
3. Duplicate/out-of-order event có phá state machine không?
4. Khi lệch, hệ thống có tự phát hiện và hồi phục không?

Không dùng một bài load test happy path để trả lời cả bốn câu.

## 2. Bằng chứng nên thu thập trước buổi phản biện

### Từ deployment/config

- URL Return và IPN đang khai báo trên VNPay Merchant Portal.
- Version API và Hash Secret tương ứng với từng environment.
- RabbitMQ exchange, queue, binding, queue type, durability và policies.
- Spring Rabbit publisher confirms/returns/mandatory, listener ack/retry/requeue settings sau khi merge toàn bộ config/env.
- Scheduler/reconciliation/refund worker nếu nằm ngoài repo.

### Từ dữ liệu

Chọn một khoảng thời gian nhỏ, thu theo `order_id`/`txnRef`:

- VNPay transaction status/queryDR result.
- Payment DB row/attempt và callback log.
- Order DB `payment_status`, `order_status`.
- Catalog stock movement hoặc quantity hiện tại.
- RabbitMQ/outbox/DLQ record.
- Timestamp logs ở mọi service.

### Từ observability

- Số callback/IPN theo response code.
- Invalid signature và amount mismatch.
- Payment→Order propagation latency p50/p95/p99/max.
- Count mismatch quá SLA.
- Outbox oldest age, retry count.
- Unroutable publish, nack, queue ready/unacked, DLQ depth.

Repo hiện chưa cung cấp đủ các signal trên; “không có alert” không có nghĩa “không có lỗi”.

## 3. Truy vấn đối soát read-only

Vì Payment và Order dùng hai database khác nhau, cách portable nhất là export hai tập rồi join ở công cụ vận hành. Nếu PostgreSQL có `postgres_fdw`/dblink thì có thể join server-side, nhưng không nên thêm chỉ để làm một lần.

### Payment DB

```sql
SELECT
  order_id,
  transaction_id,
  payment_status,
  total_amount,
  vnp_response_code,
  payment_date,
  updated_at
FROM payment_details
WHERE updated_at >= :from_time
ORDER BY order_id;
```

### Order DB

```sql
SELECT
  id AS order_id,
  payment_method,
  payment_status,
  order_status,
  total_discounted_price,
  order_date
FROM orders
WHERE order_date >= :from_time
  AND payment_method = 'VNPAY'
ORDER BY id;
```

### Các mismatch cần flag sau khi join theo `order_id`

```text
M1 payment.COMPLETED != order.payment.COMPLETED
M2 payment.total_amount != order.total_discounted_price
M3 order.CONFIRMED/SHIPPED/DELIVERED nhưng payment chưa COMPLETED
M4 order.CANCELLED nhưng payment COMPLETED và không có refund thật
M5 payment PENDING quá payment expiry/SLA
M6 order PENDING quá SLA dù payment COMPLETED
M7 transaction_id null/trùng hoặc callback transactionNo trùng
```

Schema hiện tại không có bảng refund/attempt/outbox nên M4/M7 không thể chứng minh đầy đủ chỉ bằng structured columns; phải đọc `payment_log` và đối chiếu VNPay.

## 4. Test matrix tối thiểu trên local/sandbox

| Test | Setup | Kích thích | Kỳ vọng an toàn | Kết quả code hiện tại dự đoán |
|---|---|---|---|---|
| T01 Missing signature | Payment PENDING | Callback `00`, không hash | Không mutation, invalid signature | Thành `COMPLETED` |
| T02 Bad signature | Payment PENDING | Callback hash sai | Không mutation | Thành `COMPLETED` nếu code `00` |
| T03 Amount mismatch | Payment 100k | Callback hợp lệ hình thức, amount 1k | Reject invalid amount | Thành `COMPLETED` |
| T04 Status mismatch | response `00`, transaction status khác `00` | Gọi callback | Không success | Thành `COMPLETED` |
| T05 Duplicate success | Payment PENDING | Gửi cùng callback 2 lần | Một transition/event side effect | Hai lần save/publish |
| T06 Success then failure | Payment PENDING | `00`, sau đó code fail | Vẫn COMPLETED | Thành FAILED |
| T07 Cancel then late success | Order CANCELLED | Callback success cũ | Không hồi sinh order | Order CONFIRMED |
| T08 Stock fail then payment | Hai event theo thứ tự | stock.failed → paid | Cancel + refund workflow | Order CONFIRMED |
| T09 Payment then stock fail | Hai event theo thứ tự | paid → stock.failed | Cancel + refund workflow | Order CANCELLED, payment vẫn COMPLETED |
| T10 Broker down at callback | Stop RabbitMQ | callback success | DB + durable outbox, publish sau restart | DB commit/event mất hoặc request lỗi |
| T11 No binding | Exchange có, queue/binding không có | callback success | Publish chưa được đánh sent | Message có thể drop im lặng |
| T12 Service crash after commit | Fault injection đúng boundary | callback success | Outbox replay sau restart | Event mất |
| T13 Create link twice | Một order PENDING | gọi create hai lần | Hai attempts, callback cả hai match được | Row/txnRef cũ bị ghi đè |
| T14 Create after paid | Payment COMPLETED | gọi create lại | Reject/idempotent existing result | Reset PENDING |
| T15 Abandoned payment | Stock reserved | không thanh toán 15+ phút | queryDR rồi expire/release | Giữ PENDING/stock vô hạn |
| T16 Partial stock fail | Order nhiều items | item sau thiếu hàng | rollback/compensate item trước | Item trước có thể đã bị trừ |
| T17 Real cancel paid order | Paid VNPAY | cancel | REFUND_PENDING → VNPay → REFUNDED | Order ghi REFUNDED local, Payment vẫn COMPLETED |
| T18 Ownership | User B biết order User A | create/read payment | 403/404 | Không có compare ownership |

## 5. Cách chứng minh callback thiếu xác thực — sandbox/local

Không cần tạo chữ ký giả. Điều kiện test là một `transaction_id` fixture đang `PENDING`. Gửi request tối thiểu:

```bash
curl -i -sS \
  'http://127.0.0.1:8080/api/v1/payment-providers/vnpay/callback?vnp_TxnRef=<fixture-txn-ref>&vnp_ResponseCode=00'
```

Sau đó kiểm tra Payment và Order DB. Nếu payment thành `COMPLETED`, test chứng minh handler không yêu cầu signature/amount. Dùng fixture disposable vì request hiện tại gây mutation tài chính nội bộ.

Acceptance sau khi sửa:

```text
HTTP có thể vẫn 200 theo hợp đồng IPN,
body: {"RspCode":"97","Message":"Invalid signature"},
Payment DB: không đổi,
Order DB: không đổi,
RabbitMQ/outbox: không có business event.
```

Mã cụ thể phải theo đặc tả VNPay đang áp dụng; không tái dùng response `success=false` dành cho UI.

## 6. Cách test dual-write gap

### Test broker unavailable

1. Tạo order/payment fixture PENDING.
2. Dừng RabbitMQ hoặc chặn kết nối từ Payment Service.
3. Gửi callback sandbox hợp lệ, hoặc trong test integration gọi service với dữ liệu đã validate.
4. Kiểm Payment DB.
5. Khởi động RabbitMQ/Order Service.
6. Chờ quá SLA dự kiến và kiểm Order DB.

Hiện tại dự đoán Payment có thể commit nhưng Order không bao giờ đổi vì không có outbox. Nếu send exception bubble lên sau commit, HTTP có thể báo lỗi dù Payment DB đã đổi — cần ghi lại cả response lẫn DB để tránh kết luận sai “HTTP fail nên rollback”.

### Test crash chính xác

Cần fault injection/hook ngay sau Payment DB commit và trước `convertAndSend`. Kill ngẫu nhiên service ít có giá trị vì khó biết đã chạm đúng window.

Sau khi có outbox, acceptance:

```text
Payment update và outbox row cùng commit.
Process chết trước publish.
Restart relay.
Event được publish.
Order cập nhật đúng một lần.
Outbox được đánh sent chỉ sau broker confirm/routing success.
```

## 7. Cách test duplicate và out-of-order

Không cần chạy RabbitMQ để test state machine lõi. Viết unit/integration test gọi transition function với permutations:

```text
Events: P = payment.completed, S = stock.reserved, F = stock.failed

P,S => CONFIRMED
S,P => CONFIRMED
F,P => CANCELLED + REFUND_PENDING
P,F => CANCELLED + REFUND_PENDING
P,P => giống P, side effect một lần
F,F => giống F, compensation một lần
```

Thêm terminal cases:

```text
CANCELLED + late P  => không CONFIRMED; reconcile/refund
REFUNDED  + late P  => không đổi
COMPLETED + failed callback cũ => không hạ FAILED
attempt B active + callback attempt A => xử lý theo attempt A, không ghi nhầm B
```

Test permutations là check nhỏ nhất có giá trị cho logic non-trivial này; throughput test không thay thế được.

## 8. Cách kiểm RabbitMQ runtime

Các câu lệnh chỉ đọc, tên vhost/user điều chỉnh theo môi trường:

```bash
rabbitmqctl list_exchanges name type durable auto_delete
rabbitmqctl list_queues name durable arguments messages_ready messages_unacknowledged
rabbitmqctl list_bindings source_name routing_key destination_name destination_kind
rabbitmqctl list_policies
```

Cần thấy ít nhất:

- `payment-exchange` tồn tại và durable.
- `order-payment-status-queue` tồn tại, durable và bind routing key `payment.status.updated`.
- Retry/DLX policy thực tế nếu đội ngũ tuyên bố đang có.
- Unroutable/drop/publisher confirm metrics ở broker/client.

Queue durable không chứng minh publisher đang dùng confirm/return. Config app và metrics client vẫn phải kiểm riêng.

## 9. Decision table cho IPN đúng chuẩn

Thứ tự xử lý quan trọng: validation trước mutation.

| Điều kiện | Hành động DB | Phản hồi IPN khái niệm |
|---|---|---|
| Signature sai/thiếu | Không đổi | Invalid signature |
| txnRef không tồn tại | Không đổi | Order not found |
| Merchant code sai | Không đổi | Invalid request/merchant |
| Amount mismatch | Không đổi | Invalid amount |
| Attempt đã xử lý cùng kết quả | Không đổi, không phát event mới | Already confirmed/idempotent success theo contract |
| Attempt terminal nhưng kết quả xung đột | Không ghi đè; mở incident/reconcile | Không xác nhận mutation mới |
| Pending + kết quả success hợp lệ | `COMPLETED` + outbox cùng transaction | Confirm success |
| Pending + kết quả fail hợp lệ | `FAILED` + outbox cùng transaction | Confirm success (đã ghi nhận kết quả) |
| DB/internal error | Rollback | Unknown/internal error để sender retry |

Phân biệt hai chữ “success”:

- Transaction success: khách thanh toán thành công.
- IPN handling success: merchant đã ghi nhận callback thành công, kể cả giao dịch bị ngân hàng từ chối.

Controller hiện trộn hai khái niệm này trong field `success` trả về.

## 10. Reconciliation algorithm tối thiểu

Job chạy định kỳ trên các attempt `PENDING` quá ngưỡng hoặc trạng thái mơ hồ:

```text
for each stale payment attempt:
    query VNPay queryDR bằng txnRef + transaction date
    verify response checksum
    if VNPay says successful:
        conditional transition -> COMPLETED
        insert outbox event in same DB transaction
    else if VNPay says definitively failed/expired:
        conditional transition -> FAILED/EXPIRED
        insert outbox event
    else:
        keep pending, increment reconciliation metadata, alert after SLA
```

Yêu cầu vận hành:

- Bounded batch và lock/claim để nhiều worker không xử lý cùng row.
- Idempotent request ID theo quy định VNPay.
- Backoff/rate limit.
- Lưu last query time/result/error.
- Không cancel mù khi query timeout.
- Alert cho trạng thái không quyết định được.

Reconciliation cũng nên quét mismatch Payment↔Order/outbox, không chỉ hỏi VNPay.

## 11. Runbook khi có sự cố thật

### Dấu hiệu: khách báo đã trừ tiền nhưng order PENDING

1. Không yêu cầu khách thanh toán lại ngay.
2. Tìm attempt bằng order ID/txnRef và transaction time.
3. QueryDR hoặc Merchant Portal xác minh trạng thái VNPay.
4. Kiểm signature/log callback và Payment DB.
5. Nếu Payment `COMPLETED`, kiểm outbox/Rabbit/Order event.
6. Nếu stock không còn, chuyển compensation/refund workflow; không tự confirm giao hàng.
7. Ghi incident/audit trước khi sửa thủ công.
8. Sau correction, verify cả Payment, Order, stock và refund source.

### Dấu hiệu: Order CONFIRMED nhưng VNPay không có success

1. Chặn fulfillment nếu chưa ship.
2. Xác định status đến từ callback nào/event nào.
3. Kiểm callback có signature/amount hợp lệ không.
4. QueryDR xác nhận nguồn thật.
5. Correct order/payment bằng quy trình audit được phê duyệt.
6. Điều tra endpoint public và rotate secret nếu có dấu hiệu lộ secret; thiếu signature đơn thuần không nhất thiết nghĩa secret bị lộ.

### Dấu hiệu: Order CANCELLED nhưng Payment COMPLETED

1. Kiểm stock failure/cancel reason và fulfillment.
2. Kiểm refund transaction thật tại VNPay; không tin enum local `REFUNDED`.
3. Nếu chưa refund, tạo refund request có ID/idempotency và theo dõi đến terminal.
4. Đối soát Payment DB, Order DB và VNPay sau hoàn tiền.

## 12. Dashboard/SLO đề xuất

| Signal | Mục tiêu ban đầu |
|---|---|
| Invalid signature | Alert khi > baseline; lưu source metadata an toàn |
| Amount mismatch | Alert ngay |
| Payment→Order lag | p99 dưới SLA do đội ngũ chọn; mismatch quá SLA = alert |
| Oldest unsent outbox | Không vượt SLA |
| Outbox retry/DLQ | Alert khi > 0 kéo dài |
| PENDING quá expiry + grace | Phải được query/reconcile |
| Paid + stock failed | Alert/auto refund workflow ngay |
| Local REFUNDED thiếu VNPay refund confirmation | Phải bằng 0 |
| Unroutable/nack | Alert ngay cho payment events |

Không đặt con số giả nếu chưa có business SLA. Điều quan trọng là có owner, threshold, dashboard và runbook.

## 13. Checklist trước production

- [ ] IPN URL production được xác nhận end-to-end từ VNPay.
- [ ] Return URL chỉ hiển thị/poll trạng thái, không là nguồn xác nhận duy nhất.
- [ ] Callback/IPN verify HMAC, merchant, txnRef, amount và transaction status.
- [ ] Payment attempt history và unique transaction reference.
- [ ] Conditional state transitions, không terminal regression.
- [ ] Payment update + outbox cùng DB transaction.
- [ ] Publisher confirm + mandatory return + retry/backoff.
- [ ] Consumer idempotent theo event ID/version.
- [ ] Order confirm cần cả paid và stock reserved.
- [ ] Payment failure/expiry release stock đúng một lần.
- [ ] Paid + stock failure chạy refund thật.
- [ ] queryDR reconciliation và mismatch alert.
- [ ] DLQ/redrive có owner và audit.
- [ ] Test đủ T01–T18 hoặc có lý do/risk acceptance rõ ràng.

## 14. Kết quả rà soát repo hiện tại

| Kiểm soát | Trạng thái thấy trong repo |
|---|---|
| Ký request payment URL | Có HMAC-SHA512 |
| Verify signature callback/IPN | Không |
| Verify callback amount/merchant/transaction status | Không |
| IPN endpoint/response chuẩn | Không thấy |
| Return URL frontend | Có |
| Payment DB transaction | Có |
| Transactional outbox | Không |
| Publisher confirm/mandatory return | Không thấy config |
| Payment event ID/version/txnRef | Không |
| Order consumer idempotency | Không |
| Cart consumer idempotency | Có theo `orderId` |
| Order optimistic version | Không |
| Persisted stock reservation status | Không |
| Payment timeout/expiry worker | Không thấy |
| queryDR implementation | Không; chỉ có config `apiUrl` chưa dùng |
| Refund API implementation | Không |
| DLQ/retry cho payment/order event | Không thấy trong source/config |
| Reconciliation/alert | Không thấy |

“Không thấy” được dùng có chủ đích cho những thứ có thể được cấu hình hoặc vận hành ngoài repo. Trước khi đóng risk, yêu cầu bằng chứng runtime tương ứng.
