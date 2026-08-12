package com.kyro.catalog;

import com.kyro.catalog.dto.ImageDTO;
import com.kyro.exceptions.AppException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {
  private static final Logger log = LoggerFactory.getLogger(ImageService.class);

  private final ImageRepository imageRepository;
  private final CloudinaryService cloudinaryService;
  private final ProductRepository productRepository;

  public ImageService(
      ImageRepository imageRepository,
      CloudinaryService cloudinaryService,
      ProductRepository productRepository) {
    this.imageRepository = imageRepository;
    this.cloudinaryService = cloudinaryService;
    this.productRepository = productRepository;
  }

  @Transactional
  public ImageDTO uploadImageForProduct(MultipartFile file, Long productId) {
    Product product = lockedProduct(productId);
    checkLimit(productId);
    if (file.isEmpty() || file.getSize() > 10L * 1024 * 1024) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "INVALID_IMAGE_FILE",
          "Image must be non-empty and no larger than 10 MB");
    }
    if (!Set.of("image/jpeg", "image/png", "image/webp").contains(file.getContentType())) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "INVALID_IMAGE_TYPE",
          "Only JPEG, PNG and WebP images are allowed");
    }
    try {
      Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
      Image image = new Image();
      image.setProduct(product);
      image.setFileName(file.getOriginalFilename());
      image.setFileType("CLOUDINARY");
      image.setDownloadUrl((String) uploadResult.get("secure_url"));
      if (image.getDownloadUrl() == null) {
        throw new IOException("Cloudinary did not return secure_url");
      }
      return new ImageDTO(imageRepository.save(image));
    } catch (IOException e) {
      log.error("Lỗi khi tải hình ảnh lên cho sản phẩm: {}", product.getId(), e);
      throw new AppException(
          HttpStatus.BAD_GATEWAY, "IMAGE_STORAGE_ERROR", "Unable to upload image");
    }
  }

  @Transactional
  public ImageDTO addImageUrl(String value, Long productId) {
    Product product = lockedProduct(productId);
    checkLimit(productId);
    String url = value == null ? "" : value.trim();
    if (url.length() > 500 || !isHttpUrl(url)) {
      throw new AppException(
          HttpStatus.BAD_REQUEST,
          "INVALID_IMAGE_URL",
          "Image URL must be a valid HTTP(S) URL up to 500 characters");
    }
    Image image = new Image();
    image.setProduct(product);
    image.setFileType("URL");
    image.setFileName(fileName(url));
    image.setDownloadUrl(url);
    return new ImageDTO(imageRepository.save(image));
  }

  @Transactional
  public void deleteImage(Long imageId) {
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại"));

    if ("CLOUDINARY".equals(image.getFileType())) {
      String publicId = cloudinaryService.extractPublicIdFromUrl(image.getDownloadUrl());
      if (publicId == null) {
        throw new AppException(
            HttpStatus.BAD_GATEWAY, "IMAGE_STORAGE_ERROR", "Invalid Cloudinary image URL");
      }
      try {
        cloudinaryService.deleteImage(publicId);
      } catch (IOException e) {
        log.error("Không thể xóa hình ảnh từ Cloudinary: {}", publicId, e);
        throw new AppException(
            HttpStatus.BAD_GATEWAY,
            "IMAGE_STORAGE_ERROR",
            "Unable to delete image from Cloudinary");
      }
    }
    imageRepository.delete(image);
  }

  @Transactional(readOnly = true)
  public List<ImageDTO> getProductImages(Long productId) {
    List<Image> images = imageRepository.findByProductId(productId);
    List<ImageDTO> imagesDTO = images.stream().map(ImageDTO::new).toList();
    return imagesDTO;
  }

  @Transactional
  public void deleteAllProductImages(Long productId) {
    List<Image> images = imageRepository.findByProductId(productId);
    for (Image image : images) {
      deleteImage(image.getId());
    }
    log.info("Đã xóa thành công tất cả hình ảnh của sản phẩm {}", productId);
  }

  private Product lockedProduct(Long productId) {
    return productRepository
        .findByIdWithLock(productId)
        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Product not found"));
  }

  private void checkLimit(Long productId) {
    if (imageRepository.countByProductId(productId) >= 10) {
      throw new AppException(
          HttpStatus.CONFLICT, "IMAGE_LIMIT_EXCEEDED", "A product can have at most 10 images");
    }
  }

  private boolean isHttpUrl(String value) {
    try {
      URI uri = new URI(value);
      return ("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
          && uri.getHost() != null;
    } catch (URISyntaxException exception) {
      return false;
    }
  }

  private String fileName(String url) {
    try {
      String path = new URI(url).getPath();
      return path == null || path.isBlank() || path.endsWith("/")
          ? "remote-image"
          : path.substring(path.lastIndexOf('/') + 1);
    } catch (URISyntaxException exception) {
      return "remote-image";
    }
  }
}
