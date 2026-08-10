# 2.4 Thiết kế cơ sở dữ liệu

Thiết kế cơ sở dữ liệu giữ vai trò tổ chức và duy trì tính nhất quán của dữ liệu trong toàn hệ thống. Do hệ thống được xây dựng theo kiến trúc microservices, dữ liệu được phân chia theo mô hình **Database-per-Service**. Mỗi dịch vụ quản lý một nhóm dữ liệu nghiệp vụ riêng và chỉ trao đổi dữ liệu với dịch vụ khác thông qua API hoặc sự kiện. Nhờ đó, các dịch vụ có thể hoạt động và phát triển tương đối độc lập, đồng thời hạn chế sự phụ thuộc trực tiếp ở tầng dữ liệu.

Dữ liệu có cấu trúc được lưu trong bốn cơ sở dữ liệu PostgreSQL gồm `kyro_auth`, `kyro_catalog`, `kyro_order` và `kyro_payment`. Giỏ hàng có tính chất tạm thời nên được lưu trên Redis theo từng người dùng. Các tệp hình ảnh được lưu trên Cloudinary; cơ sở dữ liệu chỉ lưu đường dẫn và thông tin mô tả của tệp.

| Nhóm dữ liệu | Nơi lưu trữ | Nội dung chính |
|---|---|---|
| Người dùng | PostgreSQL `kyro_auth` | Tài khoản, vai trò và địa chỉ |
| Sản phẩm | PostgreSQL `kyro_catalog` | Danh mục, sản phẩm, tồn kho, hình ảnh và đánh giá |
| Đơn hàng | PostgreSQL `kyro_order` | Đơn hàng, sản phẩm trong đơn và địa chỉ giao hàng |
| Thanh toán | PostgreSQL `kyro_payment` | Thông tin và kết quả giao dịch |
| Giỏ hàng | Redis | Giỏ hàng tạm thời của người dùng |

## 2.4.1 Xác định các thực thể

Căn cứ vào các chức năng nghiệp vụ, hệ thống gồm các thực thể sau:

| STT | Thực thể | Mô tả |
|---:|---|---|
| 1 | Vai trò (`role`) | Xác định quyền của người dùng trong hệ thống, gồm quản trị viên và khách hàng. |
| 2 | Người dùng (`users`) | Lưu thông tin tài khoản, thông tin cá nhân, trạng thái hoạt động và thông tin đăng nhập. |
| 3 | Địa chỉ (`address`) | Lưu các địa chỉ nhận hàng được người dùng khai báo. |
| 4 | Danh mục (`category`) | Phân loại sản phẩm theo cấu trúc danh mục cha và danh mục con. |
| 5 | Sản phẩm (`product`) | Lưu thông tin mô tả, thông số kỹ thuật, giá bán, mức giảm giá và số liệu đánh giá của sản phẩm. |
| 6 | Biến thể sản phẩm (`sizes`) | Lưu tên biến thể và số lượng tồn kho tương ứng của từng sản phẩm. |
| 7 | Hình ảnh (`image`) | Lưu đường dẫn và thông tin tệp hình ảnh của sản phẩm. |
| 8 | Đánh giá (`review`) | Lưu điểm số, nội dung và thông tin người thực hiện đánh giá sản phẩm. |
| 9 | Đơn hàng (`orders`) | Lưu thông tin người mua, thời gian đặt hàng, tổng tiền và trạng thái của đơn hàng. |
| 10 | Chi tiết đơn hàng (`order_item`) | Lưu sản phẩm, số lượng, biến thể và giá tại thời điểm đặt hàng. |
| 11 | Địa chỉ giao hàng (`order_address`) | Lưu bản chụp địa chỉ được sử dụng cho một đơn hàng. |
| 12 | Chi tiết thanh toán (`payment_details`) | Lưu phương thức, số tiền, trạng thái và kết quả xử lý giao dịch. |
| 13 | Giỏ hàng (`Cart`) | Lưu danh sách sản phẩm và tổng giá trị giỏ hàng của một người dùng. |
| 14 | Sản phẩm trong giỏ (`CartItem`) | Lưu sản phẩm, biến thể, số lượng và giá tạm thời trong giỏ hàng. |

Một số thực thể lưu dữ liệu dưới dạng bản chụp nhằm bảo đảm lịch sử giao dịch không thay đổi theo dữ liệu hiện tại. Cụ thể, `order_address` lưu lại địa chỉ tại thời điểm đặt hàng; `order_item` lưu tên, ảnh và giá của sản phẩm; `review` lưu tên người đánh giá. Giỏ hàng được lưu dưới dạng một đối tượng trên Redis với khóa `cart:{userId}` và thời gian tồn tại 30 ngày kể từ lần cập nhật gần nhất.

## 2.4.2 Mô hình ERD

Mô hình ERD thể hiện các quan hệ nghiệp vụ chính như sau:

- Một vai trò có thể được gán cho nhiều người dùng; mỗi người dùng thuộc một vai trò.
- Một người dùng có thể lưu nhiều địa chỉ, viết nhiều đánh giá và tạo nhiều đơn hàng.
- Một danh mục có thể chứa nhiều danh mục con và nhiều sản phẩm.
- Một sản phẩm có thể có nhiều hình ảnh, nhiều biến thể tồn kho, nhiều đánh giá, xuất hiện trong nhiều giỏ hàng và nhiều đơn hàng.
- Một đơn hàng gồm một địa chỉ giao hàng và một hoặc nhiều chi tiết đơn hàng.
- Một đơn hàng có tối đa một bản ghi chi tiết thanh toán.
- Mỗi người dùng có tối đa một giỏ hàng; một giỏ hàng chứa nhiều sản phẩm.

Các quan hệ nằm trong cùng một cơ sở dữ liệu được biểu diễn bằng đường liền. Các quan hệ nét đứt là tham chiếu giữa các dịch vụ, được liên kết bằng mã định danh thay vì khóa ngoại vật lý. Cách biểu diễn này phản ánh đúng nguyên tắc mỗi dịch vụ tự quản lý dữ liệu của mình trong kiến trúc microservices.

*Hình 2.x. Mô hình thực thể – liên kết của hệ thống*

