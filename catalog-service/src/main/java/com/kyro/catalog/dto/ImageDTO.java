package com.kyro.catalog.dto;

import com.kyro.catalog.Image;

public class ImageDTO {
  private Long imageId;
  private String fileName;
  private String downloadUrl;

  public ImageDTO() {}

  public ImageDTO(Image image) {
    this.imageId = image.getId();
    this.fileName = image.getFileName();
    this.downloadUrl = image.getDownloadUrl();
  }

  public Long getImageId() {
    return imageId;
  }

  public void setImageId(Long imageId) {
    this.imageId = imageId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }
}
