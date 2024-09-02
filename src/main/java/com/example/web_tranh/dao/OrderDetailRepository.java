package com.example.web_tranh.dao;

import com.example.web_tranh.entity.Order;
import com.example.web_tranh.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(path = "order-detail")
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    public List<OrderDetail> findOrderDetailsByOrder(Order order);
    //    List<OrderDetail> findByUserId(int userId);
    @Query("SELECT od FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE o.user.idUser = :userId AND o.status = 'COMPLETE'")
    List<OrderDetail> findByUserId(@Param("userId") int userId);
    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od " +
            "JOIN od.order o " +
            "WHERE od.art.idArt = :artId AND o.user.idUser = :userId AND o.status = 'COMPLETE'")
    boolean existsByArtIdAndUserId(@Param("artId") int artId, @Param("userId") int userId);
}