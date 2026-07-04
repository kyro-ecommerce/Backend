package com.kyro.chat;

import com.kyro.catalog.FilterProduct;
import com.kyro.catalog.Product;
import com.kyro.catalog.ProductService;
import com.kyro.order.Order;
import com.kyro.order.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/chatbot")
public class ChatbotController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/products/recommendations")
    public ResponseEntity<List<Product>> getProductRecommendations(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String feature) {

        FilterProduct filterProduct = new FilterProduct();
        filterProduct.setTopLevelCategory(category);
        filterProduct.setMinPrice(minPrice);
        filterProduct.setMaxPrice(maxPrice);
        filterProduct.setColor(feature);

        List<Product> products = productService.findAllProductsByFilter(filterProduct);

        return ResponseEntity.ok(products);
    }

    @GetMapping("/order/{orderId}/status")
    public ResponseEntity<Order> getOrderStatus(@PathVariable Long orderId) {
        Order order = orderService.findOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/product/stock/{productId}")
    public ResponseEntity<Map<String, Object>> checkProductStock(@PathVariable Long productId) {
        Product product = productService.findProductById(productId);

        Map<String, Object> stock = Map.of(
                "productId", product.getId(),
                "productName", product.getTitle(),
                "inStock", product.getQuantity() > 0,
                "availableQuantity", product.getQuantity()
        );

        return ResponseEntity.ok(stock);
    }
}