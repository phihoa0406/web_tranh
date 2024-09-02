package com.example.web_tranh.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "feed_backs")
public class Feedbacks {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_feed_backs")
    private long idFeedBacks; // Mã đánh giá
    @Column(name = "content")
    private String content; // Nội dung đánh giá
    @Column(name = "timestamp")
    private Timestamp timestamp; // Thời gian mà comment

    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "id_art", nullable = false)
    private Art art; // Đánh giá sản phẩm nào

    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "id_user", nullable = false)
    private User user; // Khách hàng (ai là người đánh giá)

    @OneToOne( cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "id_order_detail")
    private OrderDetail orderDetail;

}

