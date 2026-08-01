package com.kyro.catalog;

import com.kyro.catalog.dto.ImageDTO;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {
  private static final Logger log = LoggerFactory.getLogger(ImageService.class);

  private final ImageRepository imageRepository;
  private final CloudinaryService cloudinaryService;

  public ImageService(ImageRepository imageRepository, CloudinaryService cloudinaryService) {
    this.imageRepository = imageRepository;
    this.cloudinaryService = cloudinaryService;
  }

  public Image uploadImageForProduct(MultipartFile file, Product product) throws IOException {
    try {
      Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);

      Image image = new Image();
      image.setProduct(product);
      image.setFileName(file.getOriginalFilename());
      image.setFileType(file.getContentType());
      image.setDownloadUrl((String) uploadResult.get("url"));

      return imageRepository.save(image);
    } catch (IOException e) {
      log.error("Lỗi khi tải hình ảnh lên cho sản phẩm: {}", product.getId(), e);
      throw new IOException("Không thể tải lên hình ảnh: " + e.getMessage());
    }
  }

  @Transactional
  public void deleteImage(Long imageId) {
    Image image =
        imageRepository
            .findById(imageId)
            .orElseThrow(() -> new RuntimeException("Hình ảnh không tồn tại"));

    // Delete from Cloudinary
    String publicId = cloudinaryService.extractPublicIdFromUrl(image.getDownloadUrl());
    if (publicId != null) {
      try {
        cloudinaryService.deleteImage(publicId);
      } catch (Exception e) {
        log.error("Không thể xóa hình ảnh từ Cloudinary: {}", publicId, e);
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

    // Delete each image on Cloudinary
    for (Image image : images) {
      String publicId = cloudinaryService.extractPublicIdFromUrl(image.getDownloadUrl());
      if (publicId != null) {
        try {
          cloudinaryService.deleteImage(publicId);
        } catch (Exception e) {
          // Log error but continue deleting other images
          log.error("Không thể xóa hình ảnh từ Cloudinary: {}", publicId, e);
        }
      }
    }

    // Delete all images from database
    imageRepository.deleteByProductId(productId);
    log.info("Đã xóa thành công tất cả hình ảnh của sản phẩm {}", productId);
  }
}
