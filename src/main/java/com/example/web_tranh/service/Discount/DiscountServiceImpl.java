package com.example.web_tranh.service.Discount;

import com.example.web_tranh.dao.ArtRepository;
import com.example.web_tranh.dao.DiscountRepository;
import com.example.web_tranh.entity.Art;
import com.example.web_tranh.entity.Discount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class DiscountServiceImpl implements DiscountService {
    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private ArtRepository artRepository;

    @Override
    public void applyDiscountForMultipleArtworks(List<Integer> artIds, double discountPercentage, Date startDate, Date endDate) {
        // Lấy tất cả các tranh từ ID
        List<Art> arts = artRepository.findAllByIdArtIn(artIds);

        for (Art art : arts) {
            Discount discount = new Discount();
            discount.setDiscountPercentage(discountPercentage);
            discount.setStartDate(startDate);
            discount.setEndDate(endDate);
            discount.setArt(art);  // Gán tranh vào đối tượng giảm giá
            discountRepository.save(discount);  // Lưu giảm giá vào cơ sở dữ liệu
        }
    }
}