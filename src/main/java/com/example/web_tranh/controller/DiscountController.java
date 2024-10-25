package com.example.web_tranh.controller;

import java.util.List;
import java.util.Date;

import com.example.web_tranh.service.Discount.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/discount")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @PostMapping("/add-discount")
    public ResponseEntity<Void> applyDiscount(
            @RequestParam List<Integer> artIds,
            @RequestParam double discountPercentage,
            @RequestParam Date startDate,
            @RequestParam Date endDate
    ) {
        try {
            discountService.applyDiscountForMultipleArtworks(
                    artIds,
                    discountPercentage,
                    startDate,
                    endDate
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
