package com.kyro.payment;

import com.kyro.enums.PaymentMethod;
import com.kyro.enums.PaymentStatus;
import com.kyro.order.CartService;
import com.kyro.order.Order;
import com.kyro.order.OrderService;



import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.pay-url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return-url}")
    private String vnp_Returnurl;

    private final OrderService orderService;

    private final PaymentRepository paymentRepository;

    private final CartService CartService;

        @Transactional
    public String createPayment(Long orderId) {
        try {
            // Lấy thông tin đơn hàng
            Order order = orderService.findOrderById(orderId);
            if (order == null) {
                throw new RuntimeException("Không tìm thấy đơn hàng");
            }

            order.setPaymentMethod(PaymentMethod.VNPAY);
            // Tạo mã giao dịch ngẫu nhiên - đảm bảo duy nhất
            String vnp_TxnRef = orderId + "_" + getRandomNumber(8);

            // Thông tin thanh toán
            String vnp_OrderInfo = "Thanh toan don hang #" + orderId;
            String vnp_OrderType = "other"; // Thay đổi từ "billpayment" sang "other"
            String vnp_IpAddr = getIpAddress();
            long amount = order.getTotalDiscountedPrice() * 100L;

            // Tạo map các tham số
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", vnp_OrderType);
            vnp_Params.put("vnp_Locale", "vn");

            // Sửa Return URL - không đính kèm orderId vào URL
            vnp_Params.put("vnp_ReturnUrl", vnp_Returnurl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            ZoneId vietnamZoneId = ZoneId.of("Asia/Ho_Chi_Minh");

            // Lấy thời gian hiện tại theo múi giờ Việt Nam
            LocalDateTime now = LocalDateTime.now(vietnamZoneId);

            // Định dạng theo yêu cầu của VNPAY (yyyyMMddHHmmss)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            // Tạo ngày tạo và ngày hết hạn
            String vnp_CreateDate = now.format(formatter);
            String vnp_ExpireDate = now.plusMinutes(15).format(formatter); // Thêm 15 phút cho thời gian hết hạn

            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // Tạo chi tiết thanh toán và lưu vào DB
            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setOrder(order);
            paymentDetail.setPaymentMethod(PaymentMethod.VNPAY);
            paymentDetail.setPaymentStatus(PaymentStatus.PENDING);
            paymentDetail.setTotalAmount(order.getTotalDiscountedPrice());
            paymentDetail.setTransactionId(vnp_TxnRef);
            paymentDetail.setCreatedAt(LocalDateTime.now());

            // Sắp xếp tham số và tạo chuỗi hash
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Xây dựng dữ liệu hash
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                    // Xây dựng query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8)); // SỬA THÀNH UTF_8
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));

                    if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            // Tạo secure hash
            String queryUrl = query.toString();
            String vnp_SecureHash = hmacSHA512(vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

//            queryUrl += "&orderId=" + orderId; // Thêm orderId vào queryUrl

            paymentDetail.setVnp_SecureHash(vnp_SecureHash);
            // Lưu chi tiết thanh toán
            paymentRepository.save(paymentDetail);

            // URL thanh toán hoàn chỉnh
            return vnp_PayUrl + "?" + queryUrl;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo yêu cầu thanh toán: " + e.getMessage());
        }
    }

        public PaymentDetail getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán: " + paymentId));
    }

        @Transactional
    public PaymentDetail processPaymentCallback(Map<String, String> vnpParams) {
        try {
            // Lấy các tham số quan trọng
            String vnp_ResponseCode = vnpParams.get("vnp_ResponseCode");
            String vnp_TxnRef = vnpParams.get("vnp_TxnRef");
            String vnp_Amount = vnpParams.get("vnp_Amount");
            String vnp_OrderInfo = vnpParams.get("vnp_OrderInfo");
            String vnp_TransactionNo = vnpParams.get("vnp_TransactionNo");
            String vnp_BankCode = vnpParams.get("vnp_BankCode");
            String vnp_PayDate = vnpParams.get("vnp_PayDate");

            // Xác minh chữ ký
            String secureHash = vnpParams.get("vnp_SecureHash");

            // Tìm PaymentDetail dựa trên vnp_TxnRef
            PaymentDetail payment = paymentRepository.findByTransactionId(vnp_TxnRef)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch: " + vnp_TxnRef));

            // Kiểm tra mã phản hồi
            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toán thành công


                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setPaymentLog(new Gson().toJson(vnpParams));
                payment.setVnp_ResponseCode(vnp_ResponseCode);

                // Cập nhật trạng thái thanh toán cho đơn hàng
                Order order = payment.getOrder();
                order.setPaymentStatus(PaymentStatus.COMPLETED);

                // Lưu thông tin thanh toán
                return paymentRepository.save(payment);
            } else {
                // Thanh toán thất bại
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setPaymentLog(new Gson().toJson(vnpParams));
                return paymentRepository.save(payment);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý callback thanh toán: " + e.getMessage());
        }
    }

    // Các phương thức hỗ trợ
    private String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String getIpAddress() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac sha512_HMAC = Mac.getInstance("HmacSHA512");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            sha512_HMAC.init(secret_key);
            byte[] hash = sha512_HMAC.doFinal(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}