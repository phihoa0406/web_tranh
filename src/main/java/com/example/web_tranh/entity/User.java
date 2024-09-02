package com.example.web_tranh.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;
import java.util.List;
@Data
@Entity
@Table(name = "user")

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private int idUser; // id user
    @Column(name = "first_name")
    private String firstName; // Họ đệm
    @Column(name = "last_name")
    private String lastName; // Tên
    @Column(name = "username")
    private String username; // Tên tài khoản
    @Column(name = "password", length = 512)
    private String password; // Mật khẩu
    @Column(name = "gender")
    private char gender; // Giới tính
    @Column(name = "date_of_birth")
    private Date dateOfBirth; // Năm sinh
    @Column(name = "email")
    private String email; // Email
    @Column(name = "phone_number")
    private String phoneNumber; // Số điện thoại
    @Column(name = "avatar")
    private String avatar; // Ảnh đại diện
    @Column(name = "delivery_address")
    private String deliveryAddress; // Địa chỉ giao hàng
    @Column(name = "enabled")
    private boolean enabled; // Trạng thái kích hoạt
    @Column(name = "activation_code")
    private String activationCode; // Mã kích hoạt

    @ManyToMany(fetch = FetchType.EAGER,cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "id_user"), inverseJoinColumns = @JoinColumn(name = "id_role"))
    private List<Role> listRoles; // Danh tranh role của user

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Order> listOrders; // Danh tranh đơn hàng của user

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FavoriteArt> listFavoriteArts; // Danh tranh các bức tranh yêu thích

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CartItem> listCartItems; // Danh tranh hàng trong giỏ của user

    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Feedbacks> listFeedbacks;

    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", gender=" + gender +
                ", dateOfBirth=" + dateOfBirth +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", avatar='" + avatar + '\'' +
                ", enabled=" + enabled +
                ", activationCode='" + activationCode + '\'' +
                '}';
    }

}