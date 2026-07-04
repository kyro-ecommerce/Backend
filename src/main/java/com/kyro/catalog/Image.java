package com.kyro.catalog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="file_name")
    private String fileName;

    @Column(name="file_type", length = 50)
    private String fileType;

    @Column(name="download_url", length = 500)
    private String downloadUrl;

    @ManyToOne
    @JoinColumn(name="product")
    private Product product;
}
