package com.example.web_tranh.service.Art;


import com.example.web_tranh.dao.ArtRepository;
import com.example.web_tranh.dao.CartItemRepository;
import com.example.web_tranh.dao.FavoriteArtRepository;
import com.example.web_tranh.dao.OrderDetailRepository;
import com.example.web_tranh.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArtRecommendationService {
    @Autowired
    private ArtRepository artRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private FavoriteArtRepository favoriteArtRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // Các trọng số cho các hành vi
    private static final double FAVORITE_WEIGHT = 1.0;
    private static final double CART_WEIGHT = 0.7;
    private static final double PURCHASED_WEIGHT = 0.5;

    private boolean isFavorite(Art art, int userId) {
        return favoriteArtRepository.existsByArtIdAndUserId(art.getIdArt(), userId);
    }

    private boolean isInCart(Art art, int userId) {
        return cartItemRepository.existsByArtIdAndUserId(art.getIdArt(), userId);
    }

    private boolean isPurchased(Art art, int userId) {
        return orderDetailRepository.existsByArtIdAndUserId(art.getIdArt(), userId);
    }

    public Art generateUserProfile(int userId)
    {
        // Lấy tất cả các tranh mà người dùng đã tương tác
        List<Art> userInteractedArts = getUserInteractedArts(userId);
        // Các biến để tính toán trọng số
        double totalWeight = 0.0;
        double weightedPrice = 0.0;
        Map<String, Double> weightedGenres = new HashMap<>();
        Map<String, Double> weightedAuthors = new HashMap<>();
        for (Art art : userInteractedArts) {
            double weight = 0.0;

            if (isFavorite(art, userId)) {
                weight = FAVORITE_WEIGHT;
            } else if (isInCart(art, userId)) {
                weight = CART_WEIGHT;
            } else if (isPurchased(art, userId)) {
                weight = PURCHASED_WEIGHT;
            }
            if (weight > 0) {
                // Tính trọng số cho giá
                weightedPrice += art.getPrice() * weight;
                // Tính trọng số cho thể loại
                for (Genre genre : art.getListGenres()) {
                    weightedGenres.put(
                            genre.getNameGenre(),
                            weightedGenres.getOrDefault(genre.getNameGenre(), 0.0) + weight
                    );
                }
                // Tính trọng số cho tác giả
                weightedAuthors.put(
                        art.getAuthor(),
                        weightedAuthors.getOrDefault(art.getAuthor(), 0.0) + weight
                );
                totalWeight += weight;
            }
        }
        // Tính toán giá trị trung bình có trọng số cho các thuộc tính
        double avgPrice = weightedPrice / totalWeight;

        // Tính trọng số trung bình cho thể loại
        Map<String, Double> avgGenres = new HashMap<>();
        for (Map.Entry<String, Double> entry : weightedGenres.entrySet()) {
            avgGenres.put(entry.getKey(), entry.getValue() / totalWeight);
        }

        // Tính trọng số trung bình cho tác giả
        Map<String, Double> avgAuthors = new HashMap<>();
        for (Map.Entry<String, Double> entry : weightedAuthors.entrySet()) {
            avgAuthors.put(entry.getKey(), entry.getValue() / totalWeight);
        }

        // Tạo ra tranh đại diện (profile) của người dùng
        Art userProfile = new Art();
        userProfile.setPrice(avgPrice);

        // Chọn 3 thể loại phổ biến nhất
        List<Genre> genres = avgGenres.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))
                .limit(3)
                .map(entry -> {
                    Genre genre = new Genre();
                    genre.setNameGenre(entry.getKey());
                    return genre;
                })
                .collect(Collectors.toList());

        // Gán danh sách thể loại vào userProfile
        userProfile.setListGenres(genres);

        // Chọn tác giả phổ biến nhất
        userProfile.setAuthor(avgAuthors.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get()
                .getKey());
        return userProfile;
    }


    private static class WeightData {
        private final double weightedPrice;
        private final Map<String, Double> weightedGenres;
        private final Map<String, Double> weightedAuthors;
        private final double totalWeight;

        public WeightData(double weightedPrice, Map<String, Double> weightedGenres, Map<String, Double> weightedAuthors, double totalWeight) {
            this.weightedPrice = weightedPrice;
            this.weightedGenres = weightedGenres;
            this.weightedAuthors = weightedAuthors;
            this.totalWeight = totalWeight;
        }

        public double getWeightedPrice() {
            return weightedPrice;
        }

        public Map<String, Double> getWeightedGenres() {
            return weightedGenres;
        }

        public Map<String, Double> getWeightedAuthors() {
            return weightedAuthors;
        }

        public double getTotalWeight() {
            return totalWeight;
        }
    }

    // Phương thức để lấy tất cả các bức tranh mà người dùng đã tương tác (yêu thích, giỏ hàng, đã mua)
    public List<Art> getUserInteractedArts(int userId)
    {
        Set<Art> interactedArts = new HashSet<>();

        // Lấy các tranh yêu thích của người dùng
        List<FavoriteArt> favoriteArts = favoriteArtRepository.findFavoriteArtsByUserId(userId);
        for (FavoriteArt fav : favoriteArts) {
            interactedArts.add(fav.getArt());
        }

        // Lấy các tranh trong giỏ hàng của người dùng
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        for (CartItem cart : cartItems) {
            interactedArts.add(cart.getArt());
        }

        // Lấy các tranh đã mua của người dùng
        List<OrderDetail> orderDetails = orderDetailRepository.findByUserId(userId);
        for (OrderDetail order : orderDetails) {
            interactedArts.add(order.getArt());
        }
        // Chuyển Set thành List để trả về
        return new ArrayList<>(interactedArts);
    }

    // Phương thức tính độ tương đồng Cosine
    private double cosineSimilarity(Art art1, Art art2) {
        // Tính toán độ tương đồng cosine giữa hai tác phẩm nghệ thuật
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        // So sánh thể loại (genres)
        Set<String> allGenres = new HashSet<>();
        for (Genre genre : art1.getListGenres()) allGenres.add(genre.getNameGenre());
        for (Genre genre : art2.getListGenres()) allGenres.add(genre.getNameGenre());
        for (String genre : allGenres) {
            // Trọng số của thể loại trong mỗi tác phẩm
            double genreWeight1 = art1.getListGenres().stream().filter(g -> g.getNameGenre().equals(genre)).count();
            double genreWeight2 = art2.getListGenres().stream().filter(g -> g.getNameGenre().equals(genre)).count();
            // Cập nhật dot product và các norm
            dotProduct += genreWeight1 * genreWeight2;
            normA += genreWeight1 * genreWeight1;
            normB += genreWeight2 * genreWeight2;
        }
        // So sánh tác giả (author)
        double authorWeight = art1.getAuthor().equals(art2.getAuthor()) ? 1.0 : 0.0;
        dotProduct += authorWeight;
        normA += authorWeight * authorWeight;
        normB += authorWeight * authorWeight;
        // So sánh giá bán (price)
        double priceDifference = Math.abs(art1.getPrice() - art2.getPrice());
        dotProduct -= priceDifference; // Trừ đi độ chênh lệch giá (càng khác biệt, độ tương đồng giảm)
        normA += priceDifference * priceDifference;
        normB += priceDifference * priceDifference;
        // Tránh chia cho 0 trong trường hợp tác phẩm có giá trị giá bằng 0
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        // Tính toán độ tương đồng cosine
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Phương thức gợi ý tranh dựa trên userProfile
    public List<Art> getTopSimilarArts(int userId) {
        // Lấy tất cả các bức tranh có trạng thái "Đã duyệt"
        List<Art> approvedArts = artRepository.findArtByReviewStatus("Đã duyệt");
        // Lọc ra các bức tranh người dùng đã tương tác (yêu thích, giỏ hàng, đã mua)
        List<Integer> interactedArtIds = new ArrayList<>();
        interactedArtIds.addAll(favoriteArtRepository.findFavoriteArtsByUserId(userId).stream().map(fav -> fav.getArt().getIdArt()).collect(Collectors.toList()));
        interactedArtIds.addAll(cartItemRepository.findByUserId(userId).stream().map(cart -> cart.getArt().getIdArt()).collect(Collectors.toList()));
        interactedArtIds.addAll(orderDetailRepository.findByUserId(userId).stream().map(order -> order.getArt().getIdArt()).collect(Collectors.toList()));
        // Lấy userProfile của người dùng
        Art userProfile = generateUserProfile(userId);
        // Tạo danh sách lưu trữ độ tương đồng
        List<AbstractMap.SimpleEntry<Art, Double>> similarityList = new ArrayList<>();
        // Tính toán độ tương đồng giữa userProfile và từng bức tranh
        for (Art art : approvedArts) {
            // Kiểm tra nếu tranh này đã được người dùng tương tác thì bỏ qua
            if (interactedArtIds.contains(art.getIdArt())) continue;

            // Tính độ tương đồng giữa userProfile và bức tranh hiện tại
            double similarity = cosineSimilarity(userProfile, art);

            // Thêm vào danh sách độ tương đồng (không cần lưu vào entity Art)
            similarityList.add(new AbstractMap.SimpleEntry<>(art, similarity));
        }

        // Sắp xếp các bức tranh theo độ tương đồng giảm dần
        similarityList.sort((pair1, pair2) -> Double.compare(pair2.getValue(), pair1.getValue()));

        // Lấy 3 bức tranh có độ tương đồng cao nhất
        return similarityList.stream()
                .limit(4)
                .map(AbstractMap.SimpleEntry::getKey)
                .collect(Collectors.toList());
    }
}
