package com.kyro.notification.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class OrderEmailTemplateTest {

  @Test
  void rendersMultipleOrderItems() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding("UTF-8");
    SpringTemplateEngine engine = new SpringTemplateEngine();
    engine.setTemplateResolver(resolver);

    Map<String, Object> address =
        Map.of(
            "street", "1 Main St",
            "ward", "Ward 1",
            "district", "District 1",
            "province", "HCM");
    List<Map<String, Object>> items =
        List.of(
            Map.of(
                "productName",
                "Laptop",
                "variantName",
                "16GB",
                "sku",
                "LAPTOP-16GB",
                "quantity",
                1,
                "price",
                20_000_000L,
                "discountedPrice",
                19_000_000L),
            Map.of(
                "productName",
                "Mouse",
                "variantName",
                "Black",
                "sku",
                "MOUSE-BLACK",
                "quantity",
                2,
                "price",
                500_000L,
                "discountedPrice",
                450_000L));
    Context context = new Context();
    context.setVariable(
        "order",
        Map.of(
            "id",
            1L,
            "orderCode",
            "KYR-A1B2C3D4E5F6",
            "status",
            "CONFIRMED",
            "recipientName",
            "Ky Ro",
            "orderDate",
            "15/08/2026 10:30",
            "paymentMethod",
            "COD",
            "totalAmount",
            19_900_000L,
            "shippingAddress",
            address,
            "items",
            items));
    context.setVariable(
        "companyLogoUrl",
        "https://res.cloudinary.com/drfflth1x/image/upload/v1786785073/kyro-logo_kueuqi.jpg");
    context.setVariable("contactEmail", "contact@kyro.com");

    String html = engine.process("mail/order-confirmation-email", context);

    assertTrue(html.contains("Laptop"));
    assertTrue(html.contains("Mouse"));
    assertTrue(html.contains("Ky Ro"));
    assertTrue(html.contains("KYR-A1B2C3D4E5F6"));
    assertTrue(html.contains("kyro-logo_kueuqi.jpg"));
    assertTrue(html.contains(" - 16GB"));
    assertTrue(html.contains("SKU: LAPTOP-16GB"));
    assertFalse(html.contains("' - ' +"));
    assertTrue(html.contains("Kyro"));
    assertTrue(html.contains("contact@kyro.com"));
  }
}
