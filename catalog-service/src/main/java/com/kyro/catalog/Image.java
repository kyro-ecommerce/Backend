package com.kyro.catalog;

import jakarta.persistence.*;

@Entity
public class Image {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "file_name")
  private String fileName;

  @Column(name = "file_type", length = 50)
  private String fileType;

  @Column(name = "download_url", length = 500)
  private String downloadUrl;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  public Image() {}

  public Image(Long id, String fileName, String fileType, String downloadUrl, Product product) {
    this.id = id;
    this.fileName = fileName;
    this.fileType = fileType;
    this.downloadUrl = downloadUrl;
    this.product = product;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType = fileType;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = downloadUrl;
  }

  public Product getProduct() {
    return product;
  }

  public void setProduct(Product product) {
    this.product = product;
  }
}
