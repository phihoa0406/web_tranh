package com.example.web_tranh.service.Order;

import com.example.web_tranh.dao.*;
import com.example.web_tranh.entity.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImp implements OrderService{
    private final ObjectMapper objectMapper;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArtRepository artRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    public OrderServiceImp(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional

    public ResponseEntity<?> save(JsonNode jsonData)
    {
        try {
            // Chuyển đổi jsonData thành đối tượng Order
            Order orderData = objectMapper.treeToValue(jsonData, Order.class);
            orderData.setTotalPrice(orderData.getTotalPrice());
            orderData.setDateCreated(Date.valueOf(LocalDate.now()));
            orderData.setStatus("Đang xử lý");


            // Lấy thông tin người dùng và thanh toán
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idUser"))));
            User user = userRepository.findById(idUser)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            orderData.setUser(user);

            int idDelivery = Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idDelivery"))));
            Delivery delivery = deliveryRepository.findById(idDelivery)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin vận chuyển"));
            orderData.setDelivery(delivery);

            int idPayment = Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idPayment"))));
            Payment payment = paymentRepository.findById(idPayment)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán"));
            orderData.setPayment(payment);

            // Lưu đơn hàng
            Order newOrder = orderRepository.save(orderData);

            // Lấy thông tin sản phẩm (tranh) trong đơn hàng
            JsonNode artNode = jsonData.get("art");

            // Kiểm tra artNode có tồn tại và có chứa mảng các sản phẩm tranh
            if (artNode != null && artNode.isArray())
            {
                for (JsonNode node : artNode)
                {
                    int quantity = Integer.parseInt(formatStringByJson(String.valueOf(node.get("quantity"))));
                    Art artResponse = objectMapper.treeToValue(node.get("art"), Art.class);


                    // Lấy tranh từ cơ sở dữ liệu
                    Optional<Art> art = artRepository.findById(artResponse.getIdArt());
                    if (art.isPresent()) {
                        Art artEntity = art.get();

                        // Kiểm tra nếu tranh có trong kho (quantity > 0)
                        if (artEntity.getQuantity() >= quantity) {
                            // Cập nhật số lượng tranh sau khi bán
                            artEntity.setQuantity(artEntity.getQuantity() - quantity);  // Giảm số lượng tranh còn lại

                            // Kiểm tra nếu tranh đã bán hết, cập nhật reviewStatus
                            if (artEntity.getQuantity() == 0) {
                                artEntity.setReviewStatus("Bán hết");  // Cập nhật trạng thái "Sold Out"
                            }
                            // Tạo chi tiết đơn hàng
                            OrderDetail orderDetail = new OrderDetail();
                            orderDetail.setArt(artEntity);  // Gán tranh cho chi tiết đơn hàng
                            orderDetail.setOrder(newOrder);  // Gán đơn hàng cho chi tiết
                            orderDetail.setPrice(quantity * artEntity.getFinalPrice());  // Tính tổng giá trị cho số lượng tranh
                            orderDetailRepository.save(orderDetail);  // Lưu chi tiết đơn hàng
                            // Lưu lại tranh đã cập nhật
                            artRepository.save(artEntity);
                        } else {
                            return ResponseEntity.badRequest().body("Sản phẩm " + artEntity.getNameArt() + " không đủ số lượng.");
                        }
                    } else {
                        return ResponseEntity.badRequest().body("Không tìm thấy tranh với ID: " + artResponse.getIdArt());
                    }

                }
            }
            else {
                return ResponseEntity.badRequest().body("Không tìm thấy thông tin tranh.");
            }
            // Xóa các mục trong giỏ hàng của người dùng
            cartItemRepository.deleteCartItemsByIdUser(user.getIdUser());
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();

    }


    @Override
    @Transactional
    public ResponseEntity<?> update(JsonNode jsonData)
    {
        try {
            int idOrder = Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idOrder"))));
            String status = formatStringByJson(String.valueOf(jsonData.get("status")));
            // Lấy đơn hàng từ cơ sở dữ liệu
            Optional<Order> order = orderRepository.findById(idOrder);
            if (order.isPresent()) {
                Order existingOrder = order.get();
                existingOrder.setStatus(status);
                // Nếu đơn hàng bị hủy, hoàn trả số lượng tranh về kho
                if (status.equals("Đã huỷ")) {
                    List<OrderDetail> orderDetailList = orderDetailRepository.findOrderDetailsByOrder(existingOrder);

                    for (OrderDetail orderDetail : orderDetailList) {
                        Art artOrderDetail = orderDetail.getArt();

                        // Kiểm tra xem tranh có bị bán hết không (quantity == 0)
                        if (artOrderDetail.getQuantity() == 0) {
                            artOrderDetail.setQuantity(1); // Hoàn trả tranh về kho nếu đơn hàng bị hủy
                            artRepository.save(artOrderDetail);
                        }
                    }
                }
                // Lưu lại trạng thái đơn hàng
                orderRepository.save(existingOrder);
            } else {
                return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }


    @Override
    @Transactional
    public ResponseEntity<?> cancel(JsonNode jsonData)
    {
        try {
            // Lấy idUser từ JSON và tìm thông tin User
            int idUser = Integer.parseInt(formatStringByJson(String.valueOf(jsonData.get("idUser"))));
            User user = userRepository.findById(idUser)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

            // Lấy đơn hàng gần nhất của người dùng
            Order order = orderRepository.findFirstByUserOrderByIdOrderDesc(user);
            if (order == null) {
                return ResponseEntity.badRequest().body("Không tìm thấy đơn hàng.");
            }

            // Cập nhật trạng thái đơn hàng thành "Bị huỷ"
            order.setStatus("Đã huỷ");

            // Lấy danh sách OrderDetail liên quan đến đơn hàng này
            List<OrderDetail> orderDetailList = orderDetailRepository.findOrderDetailsByOrder(order);

            for (OrderDetail orderDetail : orderDetailList) {
                Art artOrderDetail = orderDetail.getArt();

                // Nếu tranh đã được bán (quantity == 0), hoàn trả lại và cập nhật review_status
                if (artOrderDetail.getQuantity() == 0) {
                    artOrderDetail.setQuantity(1); // Hoàn trả lại tranh về kho
                    artOrderDetail.setReviewStatus("Đã duyệt"); // Cập nhật review_status thành APPROVED
                    artRepository.save(artOrderDetail);
                }
            }

            // Lưu lại trạng thái mới của đơn hàng
            orderRepository.save(order);
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi trong quá trình hủy đơn.");
        }

        return ResponseEntity.ok("Đơn hàng đã được hủy và các thay đổi đã được áp dụng.");
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}
