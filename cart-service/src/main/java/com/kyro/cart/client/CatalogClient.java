package com.kyro.cart.client;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
@FeignClient(name="catalog-service") public interface CatalogClient {
 @GetMapping("/api/v1/internal/products/{productId}") ProductResponse getProductById(@PathVariable("productId")Long id);
 @PostMapping("/api/v1/internal/products/lookup") List<ProductResponse> getProducts(@RequestBody ProductLookupRequest r);
 record ProductResponse(Long id,String title,int discountPercent,List<ImageResponse> images,List<VariantResponse> variants){}
 record ImageResponse(Long id,String downloadUrl){}
 record VariantResponse(Long id,String sku,String variantName,long price,long salePrice,int stock,boolean active){}
 record ProductLookupRequest(List<Long> productIds){}
}
