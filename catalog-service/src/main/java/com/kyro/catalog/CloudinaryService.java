package com.kyro.catalog;

import com.cloudinary.Cloudinary;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

  private final Cloudinary cloudinary;
  private static final String DEFAULT_FOLDER = "tech_shop";

  public Map<String, Object> uploadImage(MultipartFile file) throws IOException {

    Map<String, Object> params = new HashMap<>();
    params.put("folder", "tech_shop");
    params.put("resource_type", "auto");
    params.put("unique_filename", true);

    try {
      return cloudinary.uploader().upload(file.getBytes(), params);
    } catch (IOException e) {
      log.error("Lỗi khi tải hình ảnh lên Cloudinary", e);
      throw e;
    }
  }

  public Map deleteImage(String publicId) throws IOException {
    try {
      return cloudinary.uploader().destroy(publicId, Map.of());
    } catch (IOException e) {
      log.error("Lỗi khi xóa hình ảnh từ Cloudinary: " + publicId, e);
      throw e;
    }
  }

  /**
   * Extract publicId from Cloudinary image URL.
   *
   * @param imageUrl Full Cloudinary image URL
   * @return publicId for deletion/transformation, or null if invalid
   */
  public String extractPublicIdFromUrl(String imageUrl) {
    try {
      if (imageUrl == null || !imageUrl.contains("/upload/")) {
        return null;
      }

      String[] parts = imageUrl.split("/upload/");
      if (parts.length < 2) return null;

      String afterUpload = parts[1];

      // Remove query parameters if present
      if (afterUpload.contains("?")) {
        afterUpload = afterUpload.substring(0, afterUpload.indexOf("?"));
      }

      // Remove file extension (.jpg, .png, etc.)
      int lastDotIndex = afterUpload.lastIndexOf('.');
      if (lastDotIndex > 0) {
        afterUpload = afterUpload.substring(0, lastDotIndex);
      }

      // Cloudinary URL format: /v1234567890/folder/filename
      // Remove version prefix (v1234567890) if present
      if (afterUpload.startsWith("/")) {
        afterUpload = afterUpload.substring(1);
      }

      String[] segments = afterUpload.split("/");
      if (segments.length > 0
          && segments[0].startsWith("v")
          && segments[0].substring(1).matches("\\d+")) {
        // Remove version prefix
        afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
      }

      return afterUpload;
    } catch (Exception e) {
      log.error("Lỗi khi trích xuất publicId từ URL: " + imageUrl, e);
      return null;
    }
  }
  //
  //    /**
  //     * Create Cloudinary image URL with transformation options
  //     *
  //     * @param publicId Cloudinary public ID of the image
  //     * @param width Desired width
  //     * @param height Desired height
  //     * @param crop Crop strategy (e.g. "fill", "crop", "scale", "fit", etc.)
  //     * @return Transformed image URL, or null if error occurs
  //     */
  //    public String generateUrl(String publicId, int width, int height, String crop) {
  //        try {
  //            Map<String, String> options = new HashMap<>();
  //            options.put("width", String.valueOf(width));
  //            options.put("height", String.valueOf(height));
  //            options.put("crop", crop); // fill, crop, scale, etc.
  //            options.put("quality", "auto");
  //            options.put("fetch_format", "auto");
  //
  //            return cloudinary.url().transformation(new
  // Transformation().params(options)).generate(publicId);
  //        } catch (Exception e) {
  //            log.error("Error creating Cloudinary image URL", e);
  //            return null;
  //        }
  //    }

}
