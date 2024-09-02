package com.example.web_tranh.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;


@Data
@Entity
@Table(name = "discount")
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_discount")
    private int idDiscount;

    @Column(name = "discount_percentage", nullable = false)
    private double discountPercentage; // Phần trăm giảm giá (0 - 100)

    @Column(name = "start_date", nullable = false)
    private Date  startDate; // Ngày bắt đầu giảm giá

    @Column(name = "end_date", nullable = false)
    private Date  endDate; // Ngày kết thúc giảm giá

    @ManyToOne
    @JoinColumn(name = "id_art", nullable = false)
    private Art art; // Bức tranh được áp dụng giảm giá
}

