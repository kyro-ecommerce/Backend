# Sổ tay bảo vệ đồ án Kyro Backend

> Cách dùng: học phần 1–5 trước để trình bày tổng quan; học phần 6–13 để trả lời truy vấn sâu; phần 14 là các câu trả lời ngắn nên nói trước hội đồng. Mọi nhận định “hiện tại” bám source ngày 2026-08-17.

## 1. Bài trình bày kiến trúc trong 90 giây

Kyro là backend thương mại điện tử theo microservices. Client đi qua API Gateway; Gateway xác minh JWT, phân quyền theo route và gắn identity đã xác thực xuống service. Eureka cung cấp service discovery, Config Server cấp cấu hình tập trung. Mỗi domain sở hữu database riêng: Auth, Catalog, Cart, Order và Payment. Cart dùng PostgreSQL làm source of truth, Redis chỉ cache.

Giao tiếp nội bộ có hai kiểu. OpenFeign là HTTP đồng bộ khi request hiện tại cần câu trả lời ngay, ví dụ Order lấy cart selection/địa chỉ hay Payment lấy số tiền order. RabbitMQ là bất đồng bộ cho workflow qua nhiều database và side effect có thể xử lý sau, như giữ stock, dọn cart, đồng bộ payment, email và quantity sold.

Checkout là saga choreography: Order lưu snapshot `PENDING`, phát `order.created`; Catalog giữ stock rồi phát `stock.reserved/failed`; Order đổi trạng thái và Cart dọn item qua hai queue riêng. COD confirm sau khi giữ stock; VNPay phải có cả stock và payment completed. Đây là eventual consistency, chưa phải exactly-once vì chưa có outbox/DLQ/reconciliation đầy đủ.

## 2. Ranh giới service

### API Gateway

- Route theo path tới service hoặc AI URI.
- `AuthenticationFilter` yêu cầu Bearer token ở route bảo vệ, verify chữ ký/hết hạn, kiểm tra role khi route yêu cầu.
- Xóa identity/internal-token do client giả mạo, inject `X-User-Id`, `X-User-Email`, `X-User-Roles` từ claim.
- Public Catalog/AI route không cần JWT; AI thường có Redis rate limiter và circuit breaker, SSE route không gắn circuit breaker để tránh buffering.
- Không chứa business transaction hay DB domain.

### Auth

- Email/password, JWT access/refresh, OAuth2 Google/GitHub, OTP, profile/address, admin user.
- Sở hữu user/role/address. Service khác không đọc DB Auth.
- OTP gửi RabbitMQ nhưng mã OTP sống trong memory của Auth.

### Catalog

- Product, variant/SKU, category hai cấp, attribute, review, image, tồn kho.
- Sở hữu stock; chỉ Catalog được trừ/hoàn stock.
- Derived field bằng SQL formula: min price, total stock, active variant count, average rating, rating count.
- Consume order events, publish stock result/product event.

### Cart

- PostgreSQL giữ cart/item/version; Redis cache DTO 30 phút.
- Revalidate product/variant/price/stock bằng Catalog ở mỗi lần đọc/checkout.
- Không giữ stock. “Có hàng lúc ở cart” không đảm bảo còn hàng lúc checkout; Catalog quyết định cuối bằng lock.

### Order

- Sở hữu order lifecycle và snapshot lịch sử.
- Điều phối saga bằng event, gọi Feign cho dữ liệu cần tức thì/compensation.
- Thống kê doanh thu chỉ tính order `DELIVERED`.

### Payment

- Sở hữu payment transaction, ký URL và xác minh callback.
- Không sửa DB Order trực tiếp; publish payment status để Order consume.

### Notification

- Chỉ consume OTP/order confirmation và gọi SMTP.
- Không lưu trạng thái/history, hiện nuốt lỗi SMTP nên không redelivery.

### Bản đồ API để định hướng khi demo

| Nhóm | Endpoint family | Quyền qua Gateway |
| --- | --- | --- |
| Auth | `/api/v1/auth/login|register|verification|refresh|logout|password-reset` | Public |
| User self-service | `/api/v1/users/me`, `/me/addresses/**` | Authenticated |
| User admin/analytics | `/api/v1/admin/users/**`, `/api/v1/admin/analytics/users/**` | ADMIN |
| Product/category | `/api/v1/products/**`, `/api/v1/categories` | Public GET |
| Review | GET product reviews public; create/update/delete/eligibility qua review route | Authenticated khi mutation/eligibility |
| Catalog admin | `/api/v1/admin/products/**`, categories, images, analytics | ADMIN |
| Cart | `/api/v1/carts/**` | Authenticated |
| Order customer | `/api/v1/orders/**` | Authenticated và controller kiểm ownership ở detail/cancel |
| Order admin | `/api/v1/admin/orders/**`, `/api/v1/admin/analytics/orders/**` | ADMIN |
| Payment | create/read `/api/v1/orders/{id}/payment(s)` | Authenticated + ownership/admin check |
| VNPay callback | `/api/v1/payment-providers/vnpay/callback` | Public nhưng phải qua HMAC/tmnCode/amount validation |
| Internal | `/api/v1/internal/**` | Không route cho client; shared internal token |

OpenAPI JSON của mỗi service được Gateway proxy riêng và Scalar hiển thị nhiều source; `/v3/api-docs` ở Gateway không phải một JSON đã merge.

## 3. Feign và RabbitMQ: câu phân biệt chuẩn

| Tiêu chí | Feign | RabbitMQ |
| --- | --- | --- |
| Kiểu | Request/response đồng bộ | Message bất đồng bộ |
| Caller chờ? | Có | Không chờ consumer |
| Phụ thuộc thời gian | Cả hai service phải available cùng lúc | Consumer có thể xử lý sau nếu broker/queue còn message |
| Kết quả tức thì | Có | Không |
| Coupling | Caller biết API/response của callee | Publisher biết event contract/exchange, không cần biết consumer |
| Một → nhiều | Phải gọi từng service | Mỗi consumer có queue riêng |
| Lỗi | Timeout/error trả về request hiện tại | Retry/redelivery/DLQ/idempotency cần thiết kế |
| Consistency | Dễ quyết định trong request nhưng không tạo transaction xuyên DB | Eventual consistency |

Quy tắc của Kyro: “cần dữ liệu ngay để quyết định” dùng Feign; “thông báo một việc đã xảy ra hoặc fan-out/side effect” dùng RabbitMQ. Không nên trả lời rằng RabbitMQ luôn nhanh hơn hay Feign luôn đáng tin hơn; chúng giải hai nhu cầu khác nhau.

Shared YAML khai báo connect timeout 2 giây/read timeout 3 giây ở `feign.client.config`. Khi bảo vệ nên gọi đây là “giá trị khai báo”; với Spring Cloud version hiện tại cần kiểm tra runtime binding hoặc timeout test trước khi cam kết đây là timeout hiệu lực.

### Tất cả Feign đang gọi

1. Cart → Catalog `GET internal/products/{id}`: add/update cart.
2. Cart → Catalog `POST internal/products/lookup`: refresh cả cart/selection theo batch.
3. Order → Cart `POST internal/carts/{userId}/selection`: checkout selected items.
4. Order → Auth `GET internal/users/{userId}/addresses/{addressId}`: xác minh ownership và snapshot address.
5. Order → Catalog `PATCH internal/products/variants/{variantId}/stock`: hoàn stock khi cancel/expire.
6. Payment → Order `GET internal/orders/{id}`: ownership, total, trạng thái, expiry.
7. Catalog → Auth `GET internal/users/{id}`: lấy tên snapshot vào review.
8. Catalog → Order `GET internal/orders/purchases`: review eligibility.
9. Catalog → Order `GET top-selling` và `product-revenue`: admin analytics.

`CartClient.getCart` và `clearCart` trong Order được khai báo nhưng không gọi. Cleanup thật dùng RabbitMQ.

## 4. Checkout từ đầu đến cuối

### Request contract chống cart cũ

Frontend gửi `addressId`, `paymentMethod`, `cartItemIds`, `cartVersion`, `expectedTotalDiscountedPrice`. Order không tin giá frontend; nó lấy selection từ Cart. Sau đó so version và total do Cart trả với dữ liệu frontend vừa xác nhận. Sai thì trả `CART_CHANGED` để người dùng reload/confirm lại.

### Cart revalidation

Cart lấy PostgreSQL/Redis, batch lookup product IDs sang Catalog, tìm đúng `variantId`, cập nhật name/SKU/current price/current sale price, đánh dấu availability. Selection phải gồm toàn bộ ID được yêu cầu, không trùng, thuộc cart và available.

### Snapshot order

Order lấy địa chỉ thuộc đúng user từ Auth. Nó tính lại tổng bằng giá Cart trả, tạo một Order `PENDING`, `paymentStatus=PENDING`, `stockReserved=false`, và copy item/address vào DB Order. Snapshot là deliberate denormalization để lịch sử không đổi khi user sửa address, product đổi tên/giá/ảnh hoặc variant bị vô hiệu hóa.

### Giữ stock

Sau commit, Order phát `order.created`. Catalog đọc items, khóa từng variant bằng pessimistic write lock và giảm stock. `reserveStock` là một transaction: thiếu stock ở bất kỳ item nào rollback toàn bộ. Catalog phát success/failure.

Publish stock result nằm sau transaction reserve. Nếu stock đã commit nhưng publish `stock.reserved` lỗi, catch thử phát `stock.failed`: cả hai có thể mất, hoặc failed đến Order làm hủy đơn dù stock đã giảm. Đây là failure window cần outbox/reconciliation.

### Kết thúc saga

- Failure: Order `CANCELLED`, payment `CANCELLED` trừ trường hợp VNPay đã completed.
- Success COD: `stockReserved=true`, Order `CONFIRMED`, phát email confirmation; Cart xóa/subtract selected item.
- Success VNPay chưa trả: Order vẫn `PENDING`, giữ stock, Cart đã dọn item.
- Success VNPay đã trả: Order `CONFIRMED`, phát email.

Cart cleanup dùng `cartItemId`, không phải productId/variantId, nên xóa đúng line đã checkout. Nếu user tăng quantity sau checkout nhưng trước cleanup, Cart trừ quantity đã mua và giữ phần còn lại.

## 5. VNPay chi tiết

### Tạo URL

PaymentController gọi Order lần 1 để kiểm ownership/admin; PaymentService gọi lần 2 để validate `paymentMethod=VNPAY`, order `PENDING`, chưa completed, chưa hết hạn và lấy total/orderCode. Amount gửi VNPay bằng VND × 100. Params sort theo key, URL encode, ký HMAC-SHA512.

Payment row là một row/order. Nếu chưa completed, tạo URL mới tái sử dụng row và đổi transactionId. Đây là retry đơn giản nhưng callback của URL cũ có thể trả về transactionRef không còn trong DB.

### Callback

Backend yêu cầu transactionRef, verify signature constant-time, verify `tmnCode`, amount, response code và transaction status. Chỉ cả response/status `00` mới `COMPLETED`; còn lại `FAILED`. Completed là monotonic: duplicate callback hoặc failure muộn không kéo completed về failed.

Sau DB commit, Payment publish status. Order lock row, cập nhật payment; nếu stock đã reserved thì confirm. Payment status completed không được regression bởi event failed/cancelled muộn.

### Failure/timeout

Payment failure không trả stock ngay vì user còn cơ hội thanh toán lại. TTL Order là 15 phút; scheduler 30 giây cộng callback grace 5 phút nên cancel gần phút 20. Expire gọi Catalog hoàn stock đồng bộ; lỗi thì transaction rollback và vòng scheduler sau retry.

### Bất đồng bộ và race

| Tình huống | Hiện trạng |
| --- | --- |
| Payment DB commit, Rabbit publish lỗi | Payment đúng nhưng Order stale; không outbox |
| Callback không đến backend | Payment/Order không biết kết quả; order có thể expire |
| Success sau Order expire | Payment completed, Order cancelled, stock đã hoàn; late event bị bỏ qua; cần manual refund |
| Hai callback | Cùng trạng thái thì no-op; completed không regression |
| Hai lần tạo URL | Payment row đổi transactionId; callback URL cũ có thể `TRANSACTION_NOT_FOUND` |
| Catalog down lúc expire | Order rollback, scheduler retry; stock/order chưa đổi nửa vời trong transaction Order, nhưng Feign là external side effect nên crash ở ranh giới vẫn cần reconciliation |

### Refund

Refund chưa được implement. Hủy paid VNPay order phục hồi stock nếu cần và chuyển order `CANCELLED`, nhưng giữ payment status `COMPLETED`. Đây là trung thực với tiền đã thu. Vận hành phải refund thủ công; về sau cần refund API, idempotency key, audit log, trạng thái `REFUND_PENDING/REFUNDED/REFUND_FAILED` và reconciliation.

## 6. Order state machine

```text
PENDING --ready--> CONFIRMED --> SHIPPED --> DELIVERED
   |                    |          |
   +--------------------+----------+--> CANCELLED
```

- `ready` COD = stock reserved; VNPay = stock reserved + payment completed.
- Không cho về lại `PENDING`.
- Không hủy `DELIVERED` hoặc order đã `CANCELLED`.
- Không hủy `PENDING` đang chờ stock result (`stockReserved=false`) vì chưa biết có stock cần hoàn hay không.
- Hủy `PENDING/CONFIRMED` có reserved stock thì gọi Feign hoàn từng variant trước khi save cancel.
- Code hiện cho hủy `SHIPPED` nhưng không hoàn stock vì restore chỉ chạy cho PENDING/CONFIRMED. Đây là business nuance/rủi ro cần xác nhận nghiệp vụ.
- Chỉ xóa order đã `CANCELLED` và không phải VNPay completed.

## 7. Cart, cache và concurrency

PostgreSQL là source of truth. Redis key `cart:{userId}` lưu `CartDTO` 30 phút. Read cache failure → load DB; write/evict Redis failure bị bỏ qua, request vẫn dùng DB. Mỗi `getCart` dù cache hit vẫn gọi Catalog refresh, nên cache giảm đọc Cart DB chứ không loại Catalog call.

Mutation khóa cart row pessimistic, persist DB rồi evict cache. DB constraints bảo đảm một cart/user và một variant/cart. `version` là optimistic handshake với frontend, còn row lock xử lý concurrent mutation tại DB.

Cart lưu snapshot price/name để hiển thị/fallback, nhưng checkout dùng dữ liệu đã refresh từ Catalog. Cart không phải authority về stock/price.

## 8. Product search/filter/sort

Public và admin dùng cùng `Specification`; admin có thể thấy product không còn active variant và thêm sort inventory.

- `keyword`: trim, lowercase và `%keyword%` trên title, description, brand.
- `categoryId`: category con chỉ chính nó; category cha gồm chính nó và con trực tiếp.
- `brand`: exact, ignore case.
- `minPrice/maxPrice`: so với `minPrice` nhỏ nhất trong active variants.
- `inStock`: `totalStock > 0` hoặc `=0`, chỉ tính active variants.
- `minRating`: average review rating.
- Public thêm `activeVariantCount > 0`.
- `color`: controller nhận nhưng specification chưa áp dụng; không nên demo như feature đã chạy.

Sort public whitelist id/title/brand/price/minPrice/discount/createdAt/rating. Admin thêm quantity và quantitySold. Default `createdAt desc`; thêm `id` cùng direction làm tie-breaker. Page bắt đầu 0, size 1–100.

`LIKE %term%` đơn giản, case-insensitive nhưng không fuzzy, stemming, typo tolerance hay relevance rank. B-tree index thường không tối ưu leading wildcard; nếu dữ liệu lớn cân nhắc PostgreSQL full-text hoặc `pg_trgm`.

## 9. Order và user search/filter

### Order

Admin search một từ trên orderCode, userEmail, recipient name, phone và tồn tại order item có productName match. Các filter còn lại AND với nhau: userId/status/payment method/payment status/date range/min/max total. Date end được mở rộng đến cuối ngày. Customer dùng engine này với userId bắt buộc và không có search.

Sort whitelist id/orderCode/orderDate/deliveryDate/total/totalItems; default orderDate desc + id. Các index chính hỗ trợ `(user_id, order_date, id)`, `(order_status, order_date, id)` và partial index expired VNPay.

### User

Admin search email/first/last name, filter role và banned. `status=active` map thành `banned=false`; account chưa OTP (`active=false`) nhưng không banned vẫn xuất hiện ở nhóm này. Role không hợp lệ hiện bị bỏ qua thành “all”, còn status không hợp lệ bị reject.

## 10. Review, image, category và analytics

### Review

Catalog hỏi Order qua Feign xem user có order `DELIVERED` chứa product. Sau đó DB unique và count đảm bảo tối đa một review/user/product. Khi tạo, Catalog hỏi Auth lấy first/last name rồi snapshot vào review. User chỉ sửa/xóa review của chính mình. Rating average/count được formula trực tiếp từ review rows.

### Image

Admin thêm URL hoặc upload JPEG/PNG/WebP ≤10 MB lên Cloudinary; tối đa 10 ảnh/product. Product được lock khi check limit để tránh concurrent upload vượt giới hạn. Xóa ảnh Cloudinary trước rồi xóa DB; URL remote chỉ xóa DB.

### Category

Hai cấp. Child bắt buộc parent level 1; top không có parent. Name trim, 1–50, unique ignore-case ở service và unique case-sensitive ở DB. Không xóa category/tree đang có product; trả danh sách category bị block và product count.

### Analytics

- Order revenue/average chỉ tính delivered; daily revenue query hiện cộng mọi trạng thái, nên cần nói rõ khác biệt này khi diễn giải chart.
- Top-selling và product revenue lấy từ delivered order items ở Order rồi Catalog ghép product/category qua Feign.
- `quantity_sold` Catalog tăng từ `order.delivered`, idempotent theo orderId.
- User summary đếm theo role/status trong Auth.

## 11. Database và transaction

- Flyway tạo schema; Hibernate `ddl-auto=validate` chỉ kiểm tra mapping, không tự sửa schema.
- Tiền lưu `bigint/long` VND để tránh sai số floating point; payment URL nhân 100 theo contract VNPay.
- DB transaction chỉ atomic trong một service/database. Feign/Rabbit side effect không tự tham gia transaction local.
- Pessimistic lock dùng cho cart, variant stock và order state transition để serialize concurrent updates.
- Unique/check constraint là lớp bảo vệ cuối: email/SKU/orderCode/order-payment, quantity/stock/price/rating/discount.
- Cross-service ID không có FK do database ownership; consistency phải kiểm tra qua API/event.

## 12. Reliability và idempotency

| Luồng | Idempotency hiện có | Lỗ hổng |
| --- | --- | --- |
| Cart cleanup | `processed_cart_events(orderId)` | Publish có thể mất |
| Quantity sold | `processed_order_delivery(orderId)` | Publish có thể mất |
| Payment callback | Same status no-op, completed monotonic | transactionId bị thay khi tạo URL lại |
| Order stock result | State guard + row lock | Không có inbox/eventId |
| Catalog reserve | Lock/transaction chống oversell | Duplicate `order.created` trừ stock lại |
| Email | Không | Listener nuốt lỗi, duplicate có thể gửi lặp |
| Product AI sync | `event_id` trong payload | Backend không retry/outbox; consumer ngoài repo |

“Exactly once” không phải thuộc tính hiện có. Câu đúng: broker/queue hỗ trợ delivery, còn exactly-once effect phải ghép at-least-once delivery với idempotent consumer; Kyro mới làm được ở vài consumer.

## 13. Các rủi ro nên chủ động thừa nhận

1. Không outbox: DB commit rồi publish fail gây mất event.
2. Không DLQ/retry/backoff/monitor queue rõ ràng.
3. Reserve stock chưa idempotent, duplicate event có thể trừ hai lần.
4. Payment–Order có thể lệch, đặc biệt late callback; chưa auto refund/reconciliation.
5. Callback phụ thuộc flow redirect/frontend; backend repo không chứng minh IPN server-to-server.
6. OTP in-memory/log plaintext/không rate-limit verify, không scale ngang an toàn.
7. Notification nuốt lỗi SMTP, không audit/retry.
8. Product event trước commit, AI index có thể lệch.
9. `color` filter chưa hoạt động; user “active” filter không đúng nghĩa activated.
10. User update/delete address tải user nhưng chưa kiểm tra address entity thực sự thuộc user trước khi sửa/xóa; internal get address thì có kiểm tra ownership.
11. Business service tin Gateway headers; phải giữ chúng private trong network.
12. Cancel `SHIPPED` không restore stock theo code hiện tại; cần chốt business rule.

Trình bày rủi ro theo công thức: “hiện trạng → hậu quả → biện pháp hiện có → hướng sửa”, không chỉ liệt kê điểm yếu.

## 14. Câu trả lời mẫu ngắn

### “Luồng giao tiếp giữa các service?”

Client đi Gateway. Nội bộ dùng Feign khi cần response ngay và RabbitMQ khi truyền event/side effect. Checkout dùng cả hai: Feign lấy cart/address trước khi tạo order, RabbitMQ điều phối reserve stock, update order và cleanup cart; cancel/expire lại dùng Feign hoàn stock vì cần biết kết quả trước khi commit cancel.

### “Feign khác RabbitMQ ở đâu?”

Feign đồng bộ request-response, caller phụ thuộc callee và timeout ngay trong request. RabbitMQ bất đồng bộ, decouple theo thời gian, fan-out bằng nhiều queue và tạo eventual consistency; đổi lại phải xử lý duplicate, retry, DLQ, idempotency và quan sát queue.

### “VNPay lỗi có bất đồng dữ liệu không?”

Có khả năng. Failure bình thường được thiết kế để Payment/Order thành FAILED/PENDING và giữ stock đến khoảng 20 phút rồi scheduler hủy, hoàn stock. Nhưng event mất hoặc callback muộn có thể làm Payment completed trong khi Order cancelled và stock đã hoàn. Hiện có HMAC, state guard, row lock, TTL/grace và scheduler retry; chưa có outbox, reconciliation hay auto refund nên trường hợp late success cần refund thủ công.

### “Tại sao không dùng transaction chung?”

Mỗi service sở hữu DB riêng nên local transaction không bao trùm HTTP/broker. 2PC làm coupling và vận hành phức tạp. Hệ thống chọn saga/eventual consistency, nhưng để production-grade cần outbox, idempotent consumer, retry/DLQ và reconciliation.

### “Tại sao Order snapshot giá và địa chỉ?”

Đơn hàng là chứng từ lịch sử. Nếu chỉ giữ foreign ID, product/address thay đổi sẽ làm nội dung đơn cũ thay đổi hoặc không đọc được. Snapshot cũng giữ service autonomy và tránh join chéo database.

### “Redis có phải database chính của cart?”

Không. PostgreSQL là source of truth; Redis chỉ cache DTO 30 phút. Redis lỗi thì Cart fallback DB, còn mỗi lần đọc vẫn revalidate qua Catalog.

### “Chống oversell thế nào?”

Cart chỉ kiểm tra sớm để UX tốt. Authority là Catalog khi consume order: lock pessimistic từng variant, kiểm tra stock không âm và giảm trong một transaction. Điều còn thiếu là idempotency cho duplicate order event.

### “Refund hoạt động thế nào?”

Chưa có refund tự động. Hủy paid VNPay order giữ payment `COMPLETED` và log manual refund. Enum/config có sẵn không đồng nghĩa feature tồn tại.

## 15. Checklist trước ngày bảo vệ

- Vẽ được system map không nhìn tài liệu.
- Kể đúng 9 Feign call và 8 event binding chính.
- Mô tả checkout COD và VNPay khác nhau ở điều kiện confirm.
- Giải thích payment failed, expiry 15+5 phút và late callback.
- Nói rõ refund chưa có.
- Phân biệt DB source of truth, Redis cache và snapshot.
- Giải thích pessimistic lock, idempotency, outbox, DLQ, eventual consistency.
- Demo search/filter nhưng không demo `color` như đã hoạt động.
- Chủ động nêu 3 rủi ro lớn nhất và hướng khắc phục.
- Không khẳng định AI internals hay end-to-end consumer nếu chưa mở repo AI.
