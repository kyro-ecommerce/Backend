package com.kyro.catalog;

import com.kyro.catalog.dto.ImageDTO;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("${api.prefix}/images")
public class ImageController {
  private static final Logger log = LoggerFactory.getLogger(ImageController.class);

  private final ImageService imageService;
  private final ProductService productService;

  public ImageController(ImageService imageService, ProductService productService) {
    this.imageService = imageService;
    this.productService = productService;
  }

  @PostMapping("/upload/{productId}")
  public ResponseEntity<Map<String, Object>> uploadImage(
      @PathVariable Long productId, @RequestParam("image") MultipartFile file) throws IOException {

    log.info("Nhận yêu cầu tải lên hình ảnh cho sản phẩm ID: {}", productId);

    Product product = productService.findProductById(productId);
    Image image = imageService.uploadImageForProduct(file, product);

    log.info("Tải lên hình ảnh thành công, ID: {}", image.getId());

    Map<String, Object> data =
        Map.of(
            "imageId", image.getId(),
            "url", image.getDownloadUrl());

    return ResponseEntity.ok(data);
  }

  @DeleteMapping("/delete/{imageId}")
  public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long imageId) {
    log.info("Nhận yêu cầu xóa hình ảnh ID: {}", imageId);
    imageService.deleteImage(imageId);
    log.info("Xóa hình ảnh thành công, ID: {}", imageId);

    return ResponseEntity.ok(Map.of("message", "Xóa hình ảnh thành công"));
  }

  @GetMapping("/product/{productId}")
  public ResponseEntity<List<ImageDTO>> getProductImages(@PathVariable Long productId) {
    productService.findProductById(productId);
    List<ImageDTO> imagesDTO = imageService.getProductImages(productId);

    log.info("Tìm thấy {} hình ảnh cho sản phẩm ID: {}", imagesDTO.size(), productId);

    return ResponseEntity.ok(imagesDTO);
  }
}
