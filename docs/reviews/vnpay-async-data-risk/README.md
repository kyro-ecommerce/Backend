# Rủi ro bất đồng bộ dữ liệu khi thanh toán VNPay

> Phạm vi kiểm tra: mã nguồn backend tại commit `279be55` ngày 2026-08-13. Các thay đổi chưa commit trong `Taskfile.yml` và `k6/performance/*` không được dùng để kết luận về logic nghiệp vụ.

## Kết luận ngắn để phản biện

Hệ thống hiện chưa chỉ gặp vấn đề **eventual consistency** theo nghĩa “dữ liệu cập nhật chậm”. Có ba nhóm rủi ro độc lập và có thể kết hợp với nhau:

1. **Nguồn sự thật từ VNPay chưa được xác thực an toàn**: callback public cập nhật `COMPLETED` chỉ dựa vào `vnp_ResponseCode=00`; không kiểm checksum, số tiền, `vnp_TmnCode`, `vnp_TransactionStatus`, trạng thái hiện tại hay mã giao dịch VNPay.
2. **Ghi Payment DB và phát RabbitMQ không nguyên tử**: payment có thể đã `COMPLETED` nhưng event không tới Order Service. Không có outbox, publisher confirm/return, retry bền vững hoặc job đối soát.
3. **Order được cập nhật bởi các event độc lập nhưng không có state machine chung**: payment success có thể xác nhận đơn dù giữ hàng đã thất bại; stock failure có thể hủy đơn sau khi tiền đã thu. Không có trạng thái `stockReservationStatus`, version, event ID hay quy tắc chống event cũ.

Do đó có thể xuất hiện các trạng thái kinh doanh không chấp nhận được:

- VNPay đã thu tiền, Payment DB `COMPLETED`, Order DB vẫn `PENDING`.
- Order `CONFIRMED` dù không còn hàng.
- Order `CANCELLED`, nhưng Payment DB vẫn `COMPLETED` và khách chưa được hoàn tiền thật.
- Một request public giả mạo có thể khiến Payment và Order cùng báo đã thanh toán dù VNPay chưa thu tiền.
- Callback hợp lệ đến muộn có thể không tìm thấy giao dịch vì lần tạo link mới đã ghi đè `transaction_id` cũ.

## Một câu trả lời 30 giây

> “Kiến trúc bất đồng bộ không sai; vấn đề là hiện tại chưa có các điều kiện để eventual consistency hội tụ đúng. Callback chưa được xác thực và chưa dùng IPN đúng hợp đồng, Payment DB với RabbitMQ có dual-write gap, còn Order nhận payment và stock event mà không có state machine/idempotency. Vì vậy đây không chỉ là trễ đồng bộ: event có thể mất, đến lặp, đến đảo thứ tự hoặc ghi đè trạng thái cuối. Trường hợp nặng nhất là đã thu tiền nhưng đơn không xử lý, hoặc đơn hết hàng vẫn được xác nhận. Cần sửa theo thứ tự: xác thực IPN + invariant trạng thái, outbox + consumer idempotent, rồi queryDR/reconciliation và refund thật.”

## Mức độ ưu tiên

| Mức | Vấn đề | Tác động chính |
|---|---|---|
| P0 | Không kiểm chữ ký và amount tại callback public | Giả mạo thanh toán, sai số tiền |
| P0 | Payment success xác nhận order không xét stock; stock failure không refund | Thu tiền nhưng không giao được hàng |
| P0 | Không có IPN backend đúng hợp đồng; `returnUrl` trỏ frontend | Mất kết quả khi người dùng/mạng không hoàn tất redirect |
| P1 | DB commit rồi publish không có outbox/confirm | Payment và Order lệch vĩnh viễn |
| P1 | Không chống duplicate/out-of-order/state regression | `COMPLETED` có thể thành `FAILED`, `CANCELLED` có thể thành `CONFIRMED` |
| P1 | Một record/đơn và tạo link mới ghi đè attempt cũ | Mất khả năng nhận callback cũ và audit đầy đủ |
| P1 | “REFUNDED” chỉ là đổi enum local | Báo đã hoàn tiền nhưng VNPay chưa hoàn |
| P2 | Không có timeout/queryDR/reconciliation/alert | Sai lệch không tự phục hồi và khó phát hiện |

## Đọc tài liệu theo mục đích

- [01-current-flow.md](01-current-flow.md): hiểu luồng thật trong code, nguồn dữ liệu và các transaction boundary.
- [02-failure-analysis.md](02-failure-analysis.md): ma trận lỗi, timeline race condition, mức độ và bằng chứng.
- [03-defense-playbook.md](03-defense-playbook.md): câu hỏi phản biện thường gặp, cách trả lời, đề xuất theo thứ tự.
- [04-verification-runbook.md](04-verification-runbook.md): cách kiểm chứng trên môi trường test và cách đối soát khi sự cố xảy ra.

## Phân biệt ba khái niệm dễ bị trộn

### Eventual consistency

Hai service tạm thời khác nhau nhưng **cuối cùng tự hội tụ** nhờ cơ chế delivery/retry/reconciliation. Ví dụ Payment đã `COMPLETED`, Order chậm 300 ms rồi cũng thành `COMPLETED`.

### Inconsistency không tự hồi phục

Event bị mất sau khi Payment DB commit, không có outbox hay job đối soát. Order sẽ `PENDING` vô thời hạn. Đây không còn là eventual consistency đúng nghĩa.

### Integrity/security failure

Request giả mạo được chấp nhận vì không kiểm checksum/amount. Cả hai DB có thể “nhất quán” với nhau nhưng cùng sai so với VNPay. Đây là sai tính toàn vẹn, không thể chữa chỉ bằng retry RabbitMQ.

## Nguồn chuẩn dùng để đối chiếu

- [VNPay PAY integration](https://sandbox.vnpayment.vn/apis/docs/thanh-toan-pay/pay.html): phân biệt luồng payment URL, IPN, Return URL; yêu cầu kiểm checksum, order, amount, trạng thái và response `RspCode`/`Message`.
- [VNPay FAQ](https://sandbox.vnpayment.vn/apis/docs/faqs/): Return URL để hiển thị cho khách; IPN để server cập nhật kết quả, tránh phụ thuộc kết nối của khách.
- [VNPay queryDR/refund](https://sandbox.vnpayment.vn/apis/docs/truy-van-hoan-tien/querydr%26refund.html): API truy vấn trạng thái thật và hoàn tiền.
- [RabbitMQ reliability guide](https://www.rabbitmq.com/docs/reliability): publisher confirm, consumer acknowledgement, duplicate và idempotency.
- [Spring AMQP publishing](https://docs.spring.io/spring-amqp/reference/amqp/template.html): publish mặc định bất đồng bộ; message không route có thể bị drop nếu không dùng return/mandatory.
- [Spring transaction-bound events](https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html): `AFTER_COMMIT` chỉ chạy sau khi DB transaction đã commit; không tạo atomic transaction giữa DB và broker.

## Giới hạn của kết luận

Những nhận định trong bộ tài liệu này dựa trên code và config nằm trong repo. Cần xác minh riêng trên môi trường triển khai:

- VNPay Merchant Portal có khai báo IPN URL nào ngoài repo hay không.
- Frontend có chuyển toàn bộ query từ Return URL sang backend callback hay không.
- RabbitMQ có policy retry/DLX/quorum queue được cài ngoài source code hay không.
- Có hệ thống reconciliation/refund/alert bên ngoài repo hay thao tác vận hành thủ công hay không.

Các cơ chế bên ngoài, nếu tồn tại, có thể giảm xác suất hoặc thời gian ảnh hưởng; chúng không loại bỏ các lỗi xác thực và state transition đang có trong code.
