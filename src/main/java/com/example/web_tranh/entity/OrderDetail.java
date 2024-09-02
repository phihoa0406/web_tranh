package com.example.web_tranh.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "order_detail")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_order_detail")
    private long idOrderDetail; // Mã chi tiết đơn hàng

    @Column(name = "price")
    private double price; // Giá

    @ManyToOne(cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "id_art", nullable = false)
    private Art art; // Mỗi chi tiết đơn hàng sẽ liên kết với một bức tranh





    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "id_order", nullable = false)
    private Order order; // Đơn hàng mà chi tiết đơn hàng này thuộc về
}
