package com.kyro.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/product-stats")
    public ResponseEntity<Map<String, Object>> getProductStatistics() {
        Map<String, Object> productStats = adminDashboardService.getProductStatistics();
        return ResponseEntity.ok(productStats);
    }

    @GetMapping("/revenue-by-time/{period}")
    public ResponseEntity<Map<String, Object>> getRevenueByPeriodOfTime(@PathVariable String period) {
        Map<String, Object> monthlyData = adminDashboardService.getRevenueAnalytics(period);
        return ResponseEntity.ok(monthlyData);
    }

    @GetMapping("/revenue-by-category")
    public ResponseEntity<Map<String, Object>> getRevenueByCategory() {
        Map<String, Object> categoryRevenue = adminDashboardService.getCategoryRevenue();
        return ResponseEntity.ok(categoryRevenue);
    }

    @GetMapping("/recent-orders/{limit}")
    public ResponseEntity<List<Map<String, Object>>> getRecentOrders(@PathVariable int limit) {
        List<Map<String, Object>> recentOrder = adminDashboardService.getRecentOrders(limit);
        return ResponseEntity.ok(recentOrder);
    }

    @GetMapping("/top-selling-products/{limit}")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingProduct(@PathVariable int limit) {
        List<Map<String, Object>> topSellingProducts = adminDashboardService.getTopSellingProducts(limit);
        return ResponseEntity.ok(topSellingProducts);
    }

    @GetMapping("/revenue-by-date-range")
    public ResponseEntity<List<Map<String, Object>>> getRevenueByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate)
    {
        List<Map<String, Object>> revenueData = adminDashboardService.getRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(revenueData);
    }
}
