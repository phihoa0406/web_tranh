package com.example.web_tranh.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_image")
    private int idImage; // Mã ảnh
    @Column(name = "is_thumbnail")
    private boolean isThumbnail; // Có phải là thumbnail không
    @Column(name = "url_image", columnDefinition = "TEXT")
    private String urlImage; // Link hình ảnh

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "id_art", nullable = false)
    private Art art; // Thuộc quyển tranh nào
    public Image() {
    }

    // Constructor với tham số để Jackson có thể sử dụng khi tạo đối tượng từ chuỗi URL
    public Image(String urlImage) {
        this.urlImage = urlImage;
    }

}