package com.kyro.catalog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kyro.catalog.dto.ImageDTO;
import com.kyro.exceptions.AppException;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {
  @Mock ImageRepository imageRepository;
  @Mock CloudinaryService cloudinaryService;
  @Mock ProductRepository productRepository;
  private ImageService service;
  private Product product;

  @BeforeEach
  void setUp() {
    service = new ImageService(imageRepository, cloudinaryService, productRepository);
    product = new Product();
    product.setId(7L);
  }

  @Test
  void rejectsInvalidMimeSizeAndLimitBeforeUpload() throws IOException {
    when(productRepository.findByIdWithLock(7L)).thenReturn(Optional.of(product));
    assertThrows(
        AppException.class,
        () ->
            service.uploadImageForProduct(
                new MockMultipartFile("image", "bad.gif", "image/gif", new byte[] {1}), 7L));
    assertThrows(
        AppException.class,
        () ->
            service.uploadImageForProduct(
                new MockMultipartFile(
                    "image", "large.jpg", "image/jpeg", new byte[10 * 1024 * 1024 + 1]),
                7L));
    when(imageRepository.countByProductId(7L)).thenReturn(10L);
    assertThrows(
        AppException.class,
        () ->
            service.uploadImageForProduct(
                new MockMultipartFile("image", "ok.jpg", "image/jpeg", new byte[] {1}), 7L));
    verify(cloudinaryService, never()).uploadImage(any());
  }

  @Test
  void storesCloudinarySecureUrl() throws IOException {
    when(productRepository.findByIdWithLock(7L)).thenReturn(Optional.of(product));
    when(cloudinaryService.uploadImage(any()))
        .thenReturn(Map.of("url", "http://unsafe", "secure_url", "https://secure/image.jpg"));
    when(imageRepository.save(any(Image.class)))
        .thenAnswer(
            invocation -> {
              Image image = invocation.getArgument(0);
              image.setId(8L);
              return image;
            });

    ImageDTO result =
        service.uploadImageForProduct(
            new MockMultipartFile("image", "ok.jpg", "image/jpeg", new byte[] {1}), 7L);

    assertEquals("https://secure/image.jpg", result.getDownloadUrl());
    assertEquals(8L, result.getImageId());
  }

  @Test
  void doesNotSaveWhenCloudinaryFails() throws IOException {
    when(productRepository.findByIdWithLock(7L)).thenReturn(Optional.of(product));
    when(cloudinaryService.uploadImage(any())).thenThrow(new IOException("down"));

    assertThrows(
        AppException.class,
        () ->
            service.uploadImageForProduct(
                new MockMultipartFile("image", "ok.png", "image/png", new byte[] {1}), 7L));
    verify(imageRepository, never()).save(any());
  }

  @Test
  void validatesAndStoresManualHttpUrlWithoutDownloading() throws IOException {
    when(productRepository.findByIdWithLock(7L)).thenReturn(Optional.of(product));
    when(imageRepository.save(any(Image.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    assertEquals(
        "photo.webp",
        service.addImageUrl(" https://example.com/a/photo.webp?x=1 ", 7L).getFileName());
    assertThrows(AppException.class, () -> service.addImageUrl("file:///tmp/image.png", 7L));
    assertThrows(AppException.class, () -> service.addImageUrl("not a url", 7L));
    verify(cloudinaryService, never()).uploadImage(any());
  }

  @Test
  void deletesManualUrlOnlyFromDatabase() throws IOException {
    Image image = image(4L, "URL", "https://example.com/image.jpg");
    when(imageRepository.findById(4L)).thenReturn(Optional.of(image));

    service.deleteImage(4L);

    verify(imageRepository).delete(image);
    verify(cloudinaryService, never()).deleteImage(any());
  }

  @Test
  void keepsDatabaseRecordWhenCloudinaryDeleteFails() throws IOException {
    Image image = image(4L, "CLOUDINARY", "https://res.cloudinary.com/demo/image/upload/v1/a.jpg");
    when(imageRepository.findById(4L)).thenReturn(Optional.of(image));
    when(cloudinaryService.extractPublicIdFromUrl(image.getDownloadUrl())).thenReturn("a");
    when(cloudinaryService.deleteImage("a")).thenThrow(new IOException("down"));

    assertThrows(AppException.class, () -> service.deleteImage(4L));
    verify(imageRepository, never()).delete(any());
  }

  private Image image(Long id, String type, String url) {
    Image image = new Image();
    image.setId(id);
    image.setFileType(type);
    image.setDownloadUrl(url);
    return image;
  }
}
