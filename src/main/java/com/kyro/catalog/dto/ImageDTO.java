package com.kyro.catalog.dto;

import com.kyro.catalog.Image;



import lombok.Data;

@Data
public class ImageDTO {
    private String fileName;
    private String downloadUrl;

    public ImageDTO(Image image) {
        this.fileName = image.getFileName();
        this.downloadUrl = image.getDownloadUrl();
    }
}