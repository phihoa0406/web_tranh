package com.example.web_tranh.dao;

import com.example.web_tranh.entity.Art;
import com.example.web_tranh.entity.CartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "cart-items")
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    // Tìm tất cả CartItem của người dùng dựa trên userId
    @Query("SELECT c FROM CartItem c WHERE c.user.idUser = :userId")  // Chỉnh sửa tham số từ idUser thành userId
    List<CartItem> findByUserId(@Param("userId") int userId);  // Đảm bảo tham số trong phương thức cũng là userId
    @Query("SELECT COUNT(ci) > 0 FROM CartItem ci " +
            "WHERE ci.art.idArt = :artId AND ci.user.idUser = :userId")
    boolean existsByArtIdAndUserId(@Param("artId") int artId, @Param("userId") int userId);
    // Xóa tất cả các CartItem của người dùng
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.idUser = :userId")  // Chỉnh sửa tham số từ idUser thành userId
    void deleteCartItemsByIdUser(@Param("userId") int userId);  // Đảm bảo tham số trong phương thức cũng là userId

    //    @Query("SELECT DISTINCT c.art FROM CartItem c WHERE c.user.id = :userId")
//    List<Art> findDistinctArtsByUserId(@Param("userId") int userId);
    @Query("SELECT DISTINCT c.art FROM CartItem c WHERE c.user.id = :userId")
    List<Art> findDistinctArtsByUserId(@Param("userId") int userId);


    List<CartItem> findByUserIdUser(int idUser);

}
