package com.kyro.admin;

import com.kyro.auth.UserRepository;
import com.kyro.catalog.Product;
import com.kyro.catalog.ProductRepository;
import com.kyro.enums.OrderStatus;
import com.kyro.order.Order;
import com.kyro.order.OrderItem;
import com.kyro.order.OrderRepository;




import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;


    @Transactional(readOnly = true)
        public Map<String, Object> getProductStatistics() {
        Map<String, Object> res = new HashMap<>();
        long totalProducts = productRepository.count();
        long inStock = productRepository.findAll().stream()
                .mapToLong(product -> product.getQuantity())
                .sum();
        long soldItems = productRepository.findAll().stream()
                .mapToLong(product -> product.getQuantitySold() != null ? product.getQuantitySold() : 0)
                .sum();

        res.put("totalProducts", totalProducts);
        res.put("inStock", inStock);
        res.put("soldItems", soldItems);

        return res;
    }

    @Transactional(readOnly = true)
        public Map<String, Object> getRevenueAnalytics(String period) {
        Map<String, Object> result = new HashMap<>();

        if ("week".equalsIgnoreCase(period)) {
            // Doanh thu theo tuần - 7 ngày
            List<Map<String, Object>> weeklyData = getWeeklyRevenueData();
            result.put("data", weeklyData);
            result.put("period", "week");

            // Tính tổng doanh thu tuần hiện tại
            BigDecimal currentWeekRevenue = weeklyData.stream()
                    .map(day -> (BigDecimal) day.get("revenue"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.put("currentRevenue", currentWeekRevenue);

        } else if ("month".equalsIgnoreCase(period)) {
            // Doanh thu theo tháng - 4 tuần
            List<Map<String, Object>> monthlyData = getMonthlyRevenueByWeeks();
            result.put("data", monthlyData);
            result.put("period", "month");

            // Tính tổng doanh thu tháng hiện tại
            BigDecimal currentMonthRevenue = monthlyData.stream()
                    .map(week -> (BigDecimal) week.get("revenue"))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.put("currentRevenue", currentMonthRevenue);
        }

        return result;
    }

    @Transactional(readOnly = true)
        public Map<String, Object> getCategoryRevenue() {
        Map<String, Object> result = new HashMap<>();
        List<Product> products = productRepository.findAll();
        Map<String, BigDecimal> categoryRevenueMap = new HashMap<>();

        // Tính doanh thu cho từng danh mục
        for (Product product : products) {
            String category = product.getCategory() != null ? product.getCategory().getName() : "Không phân loại";
            Long quantitySold = product.getQuantitySold() != null ? product.getQuantitySold() : 0L;
            BigDecimal revenue = BigDecimal.valueOf(product.getDiscountedPrice()).multiply(BigDecimal.valueOf(quantitySold));

            categoryRevenueMap.put(category,
                    categoryRevenueMap.getOrDefault(category, BigDecimal.ZERO).add(revenue));
        }

        // Sắp xếp danh mục theo doanh thu từ cao đến thấp
        List<Map.Entry<String, BigDecimal>> sortedEntries = new ArrayList<>(categoryRevenueMap.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Tính tổng doanh thu
        BigDecimal totalRevenue = categoryRevenueMap.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> categoryData = new LinkedHashMap<>();

        // Xử lý top 3 danh mục
        int topCount = Math.min(3, sortedEntries.size());
        for (int i = 0; i < topCount; i++) {
            Map.Entry<String, BigDecimal> entry = sortedEntries.get(i);
            Map<String, Object> categoryInfo = new HashMap<>();
            categoryInfo.put("value", entry.getValue());
            categoryInfo.put("percentage", entry.getValue()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue.max(BigDecimal.ONE), 2, RoundingMode.HALF_UP));

            categoryData.put(entry.getKey(), categoryInfo);
        }

        // handle others
        if (sortedEntries.size() > 3) {
            BigDecimal othersRevenue = BigDecimal.ZERO;
            for (int i = 3; i < sortedEntries.size(); i++) {
                othersRevenue = othersRevenue.add(sortedEntries.get(i).getValue());
            }

            Map<String, Object> othersInfo = new HashMap<>();
            othersInfo.put("value", othersRevenue);
            othersInfo.put("percentage", othersRevenue
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue.max(BigDecimal.ONE), 2, RoundingMode.HALF_UP));

            categoryData.put("Khác", othersInfo);
        }

        result.put("categories", categoryData);
        result.put("totalRevenue", totalRevenue);

        return result;
    }

    @Transactional(readOnly = true)
        public List<Map<String, Object>> getRecentOrders(int limit) {
        // Lấy tất cả đơn hàng, sắp xếp theo thời gian giảm dần
        List<Order> allOrders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
        List<Map<String, Object>> recentOrders = new ArrayList<>();

        // Lấy limit đơn hàng đầu tiên
        int count = Math.min(limit, allOrders.size());
        for (int i = 0; i < count; i++) {
            Order order = allOrders.get(i);
            Map<String, Object> orderData = new HashMap<>();

            // Thông tin đơn hàng
            orderData.put("id", order.getId());
            orderData.put("trackingNo", "TN-" + order.getId());
            orderData.put("orderDate", order.getOrderDate());
            orderData.put("totalAmount", order.getTotalDiscountedPrice());
            orderData.put("status", order.getOrderStatus());

            // Thông tin người dùng
            if (order.getUser() != null) {
                orderData.put("customerName", order.getUser().getFirstName() + " " + order.getUser().getLastName());
                orderData.put("customerEmail", order.getUser().getEmail());
            }

            // Thông tin sản phẩm đầu tiên trong đơn hàng (để hiển thị)
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                OrderItem firstItem = order.getOrderItems().iterator().next();
                orderData.put("productId", firstItem.getProduct().getId());
                orderData.put("productName", firstItem.getProduct().getTitle());

                // Lấy hình ảnh sản phẩm (nếu có)
                if (firstItem.getProduct().getImages() != null && !firstItem.getProduct().getImages().isEmpty()) {
                    orderData.put("productImg", firstItem.getProduct().getImages().get(0).getDownloadUrl());
                }

                orderData.put("price", firstItem.getPrice());
                orderData.put("quantity", firstItem.getQuantity());
            }

            recentOrders.add(orderData);
        }

        return recentOrders;
    }

    @Transactional(readOnly = true)
        public List<Map<String, Object>> getTopSellingProducts(int limit) {
        // Lấy tất cả sản phẩm và sắp xếp theo số lượng bán giảm dần
        List<Product> allProducts = productRepository.findAll();
        allProducts.sort((p1, p2) -> {
            Long sold1 = p1.getQuantitySold() != null ? p1.getQuantitySold() : 0L;
            Long sold2 = p2.getQuantitySold() != null ? p2.getQuantitySold() : 0L;
            return sold2.compareTo(sold1);
        });

        List<Map<String, Object>> topProducts = new ArrayList<>();

        // Lấy limit sản phẩm đầu tiên
        int count = Math.min(limit, allProducts.size());
        for (int i = 0; i < count; i++) {
            Product product = allProducts.get(i);
            Map<String, Object> productData = new HashMap<>();

            productData.put("id", product.getId());
            productData.put("name", product.getTitle());
            productData.put("price", product.getPrice());
            productData.put("discountedPrice", product.getDiscountedPrice());

            // Lấy thông tin danh mục
            if (product.getCategory() != null) {
                productData.put("category", product.getCategory().getName());
            }

            // Lấy hình ảnh sản phẩm (nếu có)
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                productData.put("imageUrl", product.getImages().get(0).getDownloadUrl());
            }

            productData.put("quantitySold", product.getQuantitySold() != null ? product.getQuantitySold() : 0);

            // Tính doanh thu từ sản phẩm này
            int revenue = product.getDiscountedPrice() * (product.getQuantitySold() != null ? product.getQuantitySold().intValue() : 0);
            productData.put("revenue", revenue);

            topProducts.add(productData);
        }

        return topProducts;
    }

    @Transactional(readOnly = true)
        public List<Map<String, Object>> getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Order> ordersInRange = orderRepository.findByOrderDateBetweenAndOrderStatus(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59),
                OrderStatus.DELIVERED
        );

        Map<LocalDate, BigDecimal> revenueByDay = ordersInRange.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().toLocalDate(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                order -> BigDecimal.valueOf(order.getTotalDiscountedPrice()),
                                BigDecimal::add
                        )
                ));

        return revenueByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("name", entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM")));
                    dayData.put("revenue", entry.getValue());
                    return dayData;
                })
                .collect(Collectors.toList());
    }


    // helper method
    private List<Map<String, Object>> getWeeklyRevenueData() {
        List<Map<String, Object>> weeklyData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime startOfDay = day.atStartOfDay();
            LocalDateTime endOfDay = day.atTime(23, 59, 59);

            List<Order> dayOrders = orderRepository.findByOrderDateBetweenAndOrderStatus(
                    startOfDay, endOfDay, OrderStatus.DELIVERED);

            BigDecimal revenue = dayOrders.stream()
                    .map(order -> BigDecimal.valueOf(order.getTotalDiscountedPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", getDayOfWeek(day));
            dayData.put("revenue", revenue);
            dayData.put("orders", dayOrders.size());

            weeklyData.add(dayData);
        }

        return weeklyData;
    }
    private String getDayOfWeek(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            case SUNDAY: return "CN";
            default: return "";
        }
    }

    private List<Map<String, Object>> getMonthlyRevenueByWeeks() {
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // Lấy 4 tuần gần nhất
        for (int i = 3; i >= 0; i--) {
            // Ngày bắt đầu của tuần: today - (i*7 + dayOfWeek - 1) ngày
            LocalDate weekStart = today.minusDays(i * 7 + today.getDayOfWeek().getValue() - 1);
            // Ngày kết thúc của tuần: weekStart + 6 ngày
            LocalDate weekEnd = weekStart.plusDays(6);

            LocalDateTime startDateTime = weekStart.atStartOfDay();
            LocalDateTime endDateTime = weekEnd.atTime(23, 59, 59);

            // Lấy các đơn hàng đã giao trong tuần
            List<Order> weekOrders = orderRepository.findByOrderDateBetweenAndOrderStatus(
                    startDateTime, endDateTime, OrderStatus.DELIVERED);

            // Tính tổng doanh thu
            BigDecimal revenue = weekOrders.stream()
                    .map(order -> BigDecimal.valueOf(order.getTotalDiscountedPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Tạo dữ liệu cho tuần này
            Map<String, Object> weekData = new HashMap<>();
            weekData.put("week", "Tuần " + (4 - i));
            weekData.put("revenue", revenue);
            weekData.put("orders", weekOrders.size());

            // Thêm thông tin ngày
            weekData.put("startDate", weekStart);
            weekData.put("endDate", weekEnd);

            monthlyData.add(weekData);
        }

        return monthlyData;
    }
}