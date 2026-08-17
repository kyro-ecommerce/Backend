# Bộ câu hỏi tự luyện bảo vệ Kyro Backend

> Trả lời thành tiếng, không nhìn sổ tay. Với câu tình huống, luôn trả lời theo 4 ý: trạng thái ban đầu → từng bước xử lý → dữ liệu cuối ở từng service → rủi ro/biện pháp. Đánh dấu `[x]` khi có thể trả lời dưới 2 phút và chịu được ít nhất hai câu hỏi vặn lại.

## A. Kiến trúc tổng quan

- [ ] 1. Vì sao dự án chọn microservices thay vì monolith? Chi phí phải trả là gì?
- [ ] 2. Kể tên 9 Maven module và trách nhiệm của từng module.
- [ ] 3. Compose có bao nhiêu container? Vì sao số container khác số module?
- [ ] 4. Request từ browser đến Order đi qua những lớp nào?
- [ ] 5. API Gateway làm gì và cố ý không làm gì?
- [ ] 6. Eureka giải quyết vấn đề gì? Service nào dùng service discovery?
- [ ] 7. Config Server được đọc lúc nào? Nếu Config Server chết sau khi các service đã khởi động thì sao?
- [ ] 8. Database-per-service nghĩa chính xác là gì? Một PostgreSQL container có vi phạm không?
- [ ] 9. Vì sao Notification không cần database trong thiết kế hiện tại? Hậu quả là gì?
- [ ] 10. AI service thuộc phạm vi repository nào? Có thể chứng minh những gì về AI từ backend này?
- [ ] 11. Dozzle có vai trò business không? Có nên để public ở production không?
- [ ] 12. Single point of entry đem lại lợi ích và rủi ro gì?

## B. Security, JWT và identity

- [ ] 13. Luồng login email/password từ request đến access token và refresh cookie.
- [ ] 14. Access token và refresh token khác mục đích/thời hạn/lưu trữ thế nào?
- [ ] 15. Logout hiện tại có vô hiệu token đã bị đánh cắp không? Vì sao?
- [ ] 16. Gateway lấy userId/email/roles từ đâu?
- [ ] 17. Vì sao phải xóa `X-User-*` do client gửi trước khi inject?
- [ ] 18. Route public, authenticated và admin được phân biệt ở đâu?
- [ ] 19. Downstream service có tự verify JWT không? Trust boundary là gì?
- [ ] 20. Nếu business-service port bị expose trực tiếp thì có rủi ro gì?
- [ ] 21. `/api/v1/internal/**` được bảo vệ thế nào?
- [ ] 22. Shared internal token khác mTLS/service identity ra sao?
- [ ] 23. Tại sao dùng constant-time comparison cho internal token/signature?
- [ ] 24. CSRF bị disable có hợp lý không khi refresh token ở cookie? Cần xem thêm thuộc tính cookie/flow nào?
- [ ] 25. OAuth2 success flow khác login local ở đâu?
- [ ] 26. Account `active` và `banned` khác nhau thế nào?

## C. OTP và account

- [ ] 27. Đăng ký user ghi DB trước hay gửi OTP trước?
- [ ] 28. OTP sinh bằng gì, dài bao nhiêu, TTL/cooldown bao lâu?
- [ ] 29. OTP được lưu ở đâu? Restart Auth gây gì?
- [ ] 30. Chạy hai instance Auth ảnh hưởng verify/resend OTP thế nào?
- [ ] 31. Nếu RabbitMQ down lúc đăng ký, API và dữ liệu user ra sao?
- [ ] 32. Nếu SMTP down sau khi consumer nhận message, message có retry không?
- [ ] 33. Vì sao log OTP plaintext là rủi ro?
- [ ] 34. Verify OTP có giới hạn số lần thử không? Nên bổ sung gì?
- [ ] 35. Forgot password tái sử dụng OTP flow ra sao? Có nguy cơ phân biệt loại OTP không?
- [ ] 36. Refresh token có server-side session/rotation/revocation không?

## D. Feign và RabbitMQ

- [ ] 37. Định nghĩa synchronous và asynchronous bằng ví dụ trong Kyro.
- [ ] 38. Kể đủ tất cả hướng Feign đang dùng.
- [ ] 39. Kể các Feign method khai báo nhưng không có caller.
- [ ] 40. YAML khai báo connect/read timeout Feign bao nhiêu? Đã có bằng chứng runtime property binding chưa?
- [ ] 41. Feign tìm host thật bằng cách nào?
- [ ] 42. Chỉ client nào có fallback? Fallback trả gì và business xử lý ra sao?
- [ ] 43. Vì sao lấy cart/address dùng Feign thay vì event?
- [ ] 44. Vì sao cleanup cart dùng RabbitMQ thay vì Feign?
- [ ] 45. Vì sao hoàn stock khi cancel lại dùng Feign?
- [ ] 46. Exchange, routing key, queue và consumer khác nhau thế nào?
- [ ] 47. Tại sao `stock.reserved` cần hai queue?
- [ ] 48. Nếu Order và Cart cùng listen một queue thì điều gì xảy ra?
- [ ] 49. Durable queue bảo đảm và không bảo đảm điều gì?
- [ ] 50. Auto ack/default ack khác manual ack thế nào? Source hiện cấu hình gì?
- [ ] 51. Hệ thống có DLQ/retry policy/publisher confirm/outbox không?
- [ ] 52. At-most-once, at-least-once, exactly-once khác nhau thế nào?
- [ ] 53. Exactly-once effect thường được tạo bằng gì?
- [ ] 54. Event contract thay đổi tương thích ngược cần lưu ý gì?

## E. Cart và checkout input

- [ ] 55. PostgreSQL và Redis trong Cart giữ vai trò gì?
- [ ] 56. Cache key/TTL là gì? Redis down thì request nào vẫn chạy?
- [ ] 57. Tại sao cache hit vẫn gọi Catalog?
- [ ] 58. Cart lưu giá nhưng vì sao không được xem là authority giá?
- [ ] 59. Khi add/update item, Cart kiểm tra những gì?
- [ ] 60. Unique constraint nào tránh hai line cùng variant?
- [ ] 61. Cart row lock giải quyết race nào?
- [ ] 62. `cartVersion` và `expectedTotalDiscountedPrice` chống vấn đề gì?
- [ ] 63. Vì sao frontend phải gửi `cartItemIds`, không chỉ productIds?
- [ ] 64. Selection kiểm tra item thuộc user thế nào?
- [ ] 65. Nếu giá đổi sau lúc frontend xem cart nhưng trước checkout thì response gì?
- [ ] 66. Nếu stock đổi sau Cart validation nhưng trước Catalog reserve thì ai quyết định cuối?
- [ ] 67. User tăng quantity sau tạo order nhưng trước cleanup thì Cart xử lý sao?
- [ ] 68. Cleanup event giao hai lần có xóa hai lần không? Nhờ đâu?

## F. Checkout saga và stock

- [ ] 69. Vẽ từng bước checkout từ POST order đến CONFIRMED/CANCELLED.
- [ ] 70. Order gọi Cart trước hay Auth trước? Dữ liệu nào được validate?
- [ ] 71. Vì sao order tạo ở `PENDING` chứ không `CONFIRMED` ngay?
- [ ] 72. Event `order.created` publish trước hay sau commit? Nhờ annotation nào?
- [ ] 73. Payload giữ stock chứa `cartItemId`, productId, variantId và quantity để làm gì?
- [ ] 74. Catalog chống oversell bằng cơ chế nào?
- [ ] 75. Nếu item thứ hai hết stock sau khi item đầu đã giảm thì transaction cuối ra sao?
- [ ] 76. Vì sao kết luận “trừ stock dở nhưng không rollback” là sai với source hiện tại?
- [ ] 77. Duplicate `order.created` nguy hiểm thế nào?
- [ ] 78. Nếu Catalog commit stock nhưng publish success fail thì mỗi DB ra sao?
- [ ] 79. Nếu Order publish `order.created` fail, COD và VNPay khác hậu quả gì?
- [ ] 80. `stockReserved` dùng để làm gì?
- [ ] 81. COD và VNPay có điều kiện confirm khác nhau thế nào?
- [ ] 82. Tại sao Order và Cart có thể tạm thời khác trạng thái sau stock event?
- [ ] 83. Saga hiện là choreography hay orchestration? Order có vai trò gì?
- [ ] 84. Compensation trong hệ thống là gì? Có đảm bảo tuyệt đối không?

## G. Order lifecycle và snapshot

- [ ] 85. State machine Order có những transition hợp lệ nào?
- [ ] 86. Customer và admin được phép đổi trạng thái khác nhau ra sao?
- [ ] 87. Vì sao không cho cancel PENDING khi `stockReserved=false`?
- [ ] 88. Cancel PENDING/CONFIRMED phục hồi stock thế nào?
- [ ] 89. Cancel SHIPPED hiện có phục hồi stock không? Đây là bug hay business rule chưa chốt?
- [ ] 90. Khi nào order được phép delete?
- [ ] 91. Vì sao order snapshot address, email và item?
- [ ] 92. Snapshot gây duplicate data nhưng tại sao vẫn hợp lý?
- [ ] 93. `orderCode` khác database ID ở mục đích gì?
- [ ] 94. Pessimistic lock Order ngăn race nào?
- [ ] 95. Khi delivered, payment status và deliveryDate đổi thế nào?
- [ ] 96. `quantity_sold` được tăng ở service nào, vào thời điểm nào?

## H. VNPay và refund

- [ ] 97. Luồng tạo URL VNPay gồm những validation nào?
- [ ] 98. Vì sao amount nhân 100? Dữ liệu tiền dùng kiểu gì?
- [ ] 99. Params được ký thế nào và vì sao phải sort/encode?
- [ ] 100. Callback kiểm tra những trường nào ngoài signature?
- [ ] 101. Điều kiện nào tạo `COMPLETED`, điều kiện nào tạo `FAILED`?
- [ ] 102. Callback duplicate được xử lý ra sao?
- [ ] 103. Failure muộn có kéo Completed về Failed không?
- [ ] 104. Payment status được truyền sang Order bằng gì và khi nào publish?
- [ ] 105. Payment fail thì Order/stock thay đổi ngay không?
- [ ] 106. TTL 15 phút và grace 5 phút kết hợp ra sao?
- [ ] 107. Scheduler chạy tần suất/batch bao nhiêu?
- [ ] 108. Catalog down khi scheduler hoàn stock thì sao?
- [ ] 109. Callback success đến sau expire tạo bất đồng gì?
- [ ] 110. Order xử lý late completed event thế nào?
- [ ] 111. Payment DB commit nhưng Rabbit fail có hậu quả gì?
- [ ] 112. `AFTER_COMMIT` giải quyết gì và chưa giải quyết gì?
- [ ] 113. Return URL frontend và backend callback endpoint liên hệ thế nào? Backend repo chưa chứng minh điều gì?
- [ ] 114. Tạo URL lần hai thay transactionId gây edge case gì?
- [ ] 115. Refund hiện có thật không? Bằng chứng nào trong source?
- [ ] 116. Vì sao paid order bị cancel vẫn giữ payment `COMPLETED`?
- [ ] 117. Thiết kế refund production cần thêm state/idempotency/audit nào?

## I. Product, search, filter và pagination

- [ ] 118. Product/variant khác nhau thế nào? Stock và price thuộc cấp nào?
- [ ] 119. `minPrice`, `totalStock`, rating được lưu hay tính động?
- [ ] 120. Keyword search chạy trên field nào? Exact hay contains? Case-sensitive không?
- [ ] 121. Vì sao `%keyword%` có thể chậm khi dữ liệu lớn?
- [ ] 122. Filter category cha gồm những category nào? Có recursive vô hạn không?
- [ ] 123. `minPrice/maxPrice` đang filter giá nào?
- [ ] 124. `inStock` có tính inactive variant không?
- [ ] 125. Public loại product không còn active variant ra sao?
- [ ] 126. `color` filter hiện có hoạt động không? Vì sao controller có param vẫn chưa đủ?
- [ ] 127. Admin có thêm sort field nào so với public?
- [ ] 128. Tại sao whitelist sort thay vì nhận tên column tùy ý?
- [ ] 129. Tại sao thêm `id` tie-breaker?
- [ ] 130. Page number bắt đầu từ mấy và max size bao nhiêu?
- [ ] 131. Khi nào nên chuyển sang full-text search/pg_trgm/AI semantic search?
- [ ] 132. Semantic search có nằm trong Java repo không?

## J. Order/user filter và analytics

- [ ] 133. Admin order search trên những field nào?
- [ ] 134. Search product name trong order dùng join hay subquery? Vì sao tránh duplicate row?
- [ ] 135. End date được chuyển thành timestamp nào?
- [ ] 136. Customer order query bị scope theo user ở đâu?
- [ ] 137. User admin search field nào và filter status hiện map ra sao?
- [ ] 138. Vì sao `status=active` hiện chưa đúng nghĩa activated?
- [ ] 139. Role không hợp lệ trong user filter đang bị reject hay bỏ qua?
- [ ] 140. Revenue summary tính order nào?
- [ ] 141. Daily revenue query hiện tính trạng thái nào? Có nhất quán summary không?
- [ ] 142. Top-selling lấy dữ liệu từ Order rồi ghép Catalog thế nào?
- [ ] 143. Nếu product đã disabled/deleted khỏi kết quả Catalog, analytics ghép ra sao?
- [ ] 144. `order.delivered` giao lại có cộng quantitySold hai lần không?

## K. Review, category và image

- [ ] 145. Điều kiện user được review product là gì?
- [ ] 146. Review eligibility cần Feign nào?
- [ ] 147. Vì sao review snapshot first/last name?
- [ ] 148. Constraint nào chống hai review cùng user/product?
- [ ] 149. User có sửa/xóa review người khác được không? Check ở đâu?
- [ ] 150. Category có tối đa mấy cấp theo service logic?
- [ ] 151. Vì sao không cho xóa category đang có product?
- [ ] 152. Xóa parent kiểm tra product ở child thế nào?
- [ ] 153. Upload image chấp nhận type/size/count nào?
- [ ] 154. Lock product lúc upload ảnh giải quyết race gì?
- [ ] 155. Xóa ảnh URL và ảnh Cloudinary khác nhau thế nào?
- [ ] 156. Nếu Cloudinary delete lỗi, DB image có bị xóa không?

## L. Database và transaction

- [ ] 157. Flyway và `ddl-auto=validate` chia trách nhiệm thế nào?
- [ ] 158. Kể database/table chính của từng service.
- [ ] 159. Những cross-service ID nào không có foreign key? Vì sao?
- [ ] 160. Vì sao tiền dùng `long/bigint`, không dùng float/double?
- [ ] 161. Pessimistic lock khác optimistic version thế nào? Kyro dùng mỗi loại ở đâu?
- [ ] 162. Unique constraint quan trọng nào bảo vệ invariant?
- [ ] 163. Check constraint nào bảo vệ stock/quantity/rating/discount?
- [ ] 164. Local transaction có rollback được Rabbit publish/Feign call không?
- [ ] 165. Self-invocation `adjustStock` trong `reserveStock` có làm mất transaction tổng không? Vì sao `reserveStock` bản thân đã transactional?
- [ ] 166. Snapshot/denormalization khác duplication vô tổ chức thế nào?

## M. Reliability, observability và nâng cấp

- [ ] 167. Transactional outbox giải quyết failure window nào?
- [ ] 168. Outbox không tự giải quyết duplicate consumer; cần thêm gì?
- [ ] 169. Inbox/idempotency key nên đặt ở event nào trước tiên?
- [ ] 170. DLQ dùng để làm gì? Tại sao không được coi là auto-fix?
- [ ] 171. Reconciliation job Payment–Order–Catalog nên so những invariant nào?
- [ ] 172. Metric/alert nào cần cho queue lag, payment mismatch, stuck order và email?
- [ ] 173. Correlation ID/event ID giúp debug flow phân tán ra sao?
- [ ] 174. Circuit breaker khác retry thế nào? Retry bừa có thể gây duplicate gì?
- [ ] 175. Backoff và jitter giải quyết thundering herd thế nào?
- [ ] 176. Nếu scale nhiều instance, queue consumer và DB lock phối hợp ra sao?
- [ ] 177. Thứ tự event có được bảo đảm toàn hệ thống không?
- [ ] 178. Product event publish trước commit có rủi ro gì? Sửa tối thiểu/thực tế thế nào?
- [ ] 179. Notification nên thay đổi gì để email lỗi được retry nhưng không spam duplicate?
- [ ] 180. Ba nâng cấp reliability ưu tiên cao nhất của bạn là gì và vì sao?

## N. Câu hỏi phản biện tình huống

- [ ] 181. RabbitMQ down đúng lúc Order vừa commit COD order: trạng thái cuối và cách phát hiện/sửa?
- [ ] 182. `order.created` được redeliver hai lần: stock/order/cart có thể ra sao?
- [ ] 183. Catalog giảm stock thành công, Order consumer down, Cart consumer chạy: user thấy gì?
- [ ] 184. Order confirm, email consumer lỗi SMTP: API/order/message cuối ra sao?
- [ ] 185. User thanh toán thành công nhưng đóng browser trước redirect: hệ thống biết bằng cách nào theo source hiện tại?
- [ ] 186. Payment callback thành công ở phút 20:00 trong lúc scheduler expire: các interleaving nào có thể xảy ra?
- [ ] 187. User bấm tạo payment URL hai tab rồi thanh toán URL tab đầu: chuyện gì xảy ra?
- [ ] 188. Admin cancel paid VNPay order: Order, Payment, stock và tiền thật ra sao?
- [ ] 189. Redis trả cart cũ nhưng Catalog giá mới: response/checkout dùng giá nào?
- [ ] 190. Hai user mua variant còn 1 stock cùng lúc: lock xử lý thế nào?
- [ ] 191. Hai request add cùng variant vào cùng cart: lock/unique/version xử lý thế nào?
- [ ] 192. Admin đổi giá đúng lúc checkout: cartVersion có bắt được mọi race không? Authority cuối là gì?
- [ ] 193. User sửa địa chỉ sau khi đặt hàng: đơn cũ có đổi không?
- [ ] 194. Product bị disable sau order nhưng trước delivery: order/history/quantitySold ra sao?
- [ ] 195. Order delivered event mất: doanh thu Order và quantitySold Catalog lệch thế nào?
- [ ] 196. AI exchange chưa được tạo: update product có rollback không? AI index ra sao?
- [ ] 197. Auth restart sau gửi OTP: user/email/OTP ra sao?
- [ ] 198. Một client gọi trực tiếp internal endpoint không token: response gì?
- [ ] 199. Một client tự gửi `X-User-Id` qua Gateway: header cuối là gì?
- [ ] 200. Nếu hội đồng hỏi “hệ thống đã production-ready chưa?”, trả lời trung thực và có lộ trình thế nào?

## O. Bài thực hành không nhìn code

- [ ] Vẽ system context và database ownership trong 5 phút.
- [ ] Vẽ sequence checkout COD.
- [ ] Vẽ sequence checkout VNPay thành công.
- [ ] Vẽ timeline VNPay failed → expire → restore stock.
- [ ] Viết bảng exchange/routing key/queue/consumer từ trí nhớ.
- [ ] Viết bảng Feign caller/callee/mục đích từ trí nhớ.
- [ ] Viết state machine Order và Payment.
- [ ] Giải thích một inconsistency bằng timeline và đề xuất outbox + inbox + reconciliation.
- [ ] Mở Swagger/Scalar và xác định route public/auth/admin.
- [ ] Chọn một orderCode và lần theo log giữa Order–Catalog–Cart–Payment bằng correlation/order ID.

## P. Thang tự chấm

Mỗi câu chấm 0–3:

- `0`: không trả lời được.
- `1`: nhớ khái niệm nhưng không gắn được vào class/flow Kyro.
- `2`: mô tả đúng hiện trạng và dữ liệu cuối.
- `3`: trả lời đúng, nêu được failure case và hướng nâng cấp không nói quá feature hiện có.

Mục tiêu trước bảo vệ: toàn bộ A–H đạt ít nhất 2; câu 69, 77–79, 97–117, 167–180 và 181–200 đạt 3.
