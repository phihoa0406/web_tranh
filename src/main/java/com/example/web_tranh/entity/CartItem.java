package com.example.web_tranh.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cart_item")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cart")
    private int idCart;
    @Column (name = "quantity")
    private int quantity;
    @ManyToOne()
    @JoinColumn(name = "id_art", nullable = false)
    private Art art;
    @ManyToOne()
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Override
    public String toString() {
        return "CartItem{" +
                "idCart=" + idCart +
                ", quantity=" + quantity +
                ", art=" + art.getIdArt() +
                '}';
    }
}
