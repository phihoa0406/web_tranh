package com.example.web_tranh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "art")

public class Art {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_art")
    private Integer idArt; // Mã tranh
    @Column(name = "name_art")
    private String nameArt; // Tên tranh
    @Column(name = "author")
    private String author; // Tên tác giả
    @Column(name = "author_id")
    private int authorId; // Tên tác giả
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description; // Mô tả
    @Column(name = "price")
    private double price; // Giá niêm yết
    @Column(name = "quantity")
    private int quantity = 1; // Số lượng
    @Column(name = "review_status")
    private String reviewStatus = "Chờ duyệt"; // Giá trị mặc định



    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "art_genre", joinColumns = @JoinColumn(name = "id_art"), inverseJoinColumns = @JoinColumn(name = "id_genre"))
    private List<Genre> listGenres; // Danh tranh thể loại
    @OneToMany(mappedBy = "art",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Image> listImages; // Danh tranh ảnh
    @OneToMany(mappedBy = "art", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails; // Một tranh có nhiều chi tiết đơn
    @OneToMany(mappedBy = "art",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<FavoriteArt> listFavoriteArts; // Danh sách tranh yêu thích
    @OneToMany(mappedBy = "art",fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Feedbacks> listFeedbacks; // Danh sách feedbacks
    @OneToMany(mappedBy = "art",fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<CartItem> listCartItems;


    @OneToMany(mappedBy = "art", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Discount> discounts; // Danh tranh giảm giá
    // Phương thức tính giá sau khi giảm giá
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Art art = (Art) o;
        return idArt == art.idArt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idArt);
    }

    public double getDiscountPercentage() {
        // Lọc các giảm giá hiệu lực tại thời điểm hiện tại
        Optional<Discount> currentDiscount = discounts.stream()
                .filter(discount -> !discount.getStartDate().after(new Date()) &&
                        !discount.getEndDate().before(new Date()))
                .findFirst();

        // Nếu có giảm giá hợp lệ, trả về phần trăm giảm giá
        return currentDiscount.map(Discount::getDiscountPercentage).orElse(0.0);
    }

    public double getFinalPrice() {
        double finalPrice = price; // Giá gốc

        // Lấy phần trăm giảm giá
        double discountPercentage = getDiscountPercentage();

        // Nếu có giảm giá hợp lệ, tính toán giá cuối cùng
        if (discountPercentage > 0) {
            finalPrice = finalPrice * (1 - discountPercentage / 100); // Áp dụng giảm giá
        }

        return finalPrice;
    }


}
