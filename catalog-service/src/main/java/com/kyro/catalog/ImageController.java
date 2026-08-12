package com.kyro.catalog;

import com.kyro.catalog.dto.ImageDTO;
import com.kyro.catalog.dto.ImageUrlRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/admin")
public class ImageController {
  private static final Logger log = LoggerFactory.getLogger(ImageController.class);

  private final ImageService imageService;
  private final ProductService productService;

  public ImageController(ImageService imageService, ProductService productService) {
    this.imageService = imageService;
    this.productService = productService;
  }

  @PostMapping(value = "/products/{productId}/images", consumes = "multipart/form-data")
  public ResponseEntity<ImageDTO> uploadImage(
      @PathVariable Long productId, @RequestParam("image") MultipartFile file) {

    log.info("Nhận yêu cầu tải lên hình ảnh cho sản phẩm ID: {}", productId);

    ImageDTO image = imageService.uploadImageForProduct(file, productId);
    log.info("Tải lên hình ảnh thành công, ID: {}", image.getImageId());
    return ResponseEntity.ok(image);
  }

  @PostMapping(value = "/products/{productId}/images", consumes = "application/json")
  public ResponseEntity<ImageDTO> addImageUrl(
      @PathVariable Long productId, @Valid @RequestBody ImageUrlRequest request) {
    return ResponseEntity.ok(imageService.addImageUrl(request.url(), productId));
  }

  @DeleteMapping("/images/{imageId}")
  public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long imageId) {
    log.info("Nhận yêu cầu xóa hình ảnh ID: {}", imageId);
    imageService.deleteImage(imageId);
    log.info("Xóa hình ảnh thành công, ID: {}", imageId);

    return ResponseEntity.ok(Map.of("message", "Xóa hình ảnh thành công"));
  }

  @GetMapping("/products/{productId}/images")
  public ResponseEntity<List<ImageDTO>> getProductImages(@PathVariable Long productId) {
    productService.findProductById(productId);
    List<ImageDTO> imagesDTO = imageService.getProductImages(productId);

    log.info("Tìm thấy {} hình ảnh cho sản phẩm ID: {}", imagesDTO.size(), productId);

    return ResponseEntity.ok(imagesDTO);
  }
}
