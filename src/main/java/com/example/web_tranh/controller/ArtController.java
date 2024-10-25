package com.example.web_tranh.controller;


import com.example.web_tranh.service.Art.ArtService;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/art")
public class ArtController {
    @Autowired
    private ArtService artService;

    @PostMapping(path = "/add-art")
    public ResponseEntity<?> save(@RequestBody JsonNode jsonData) {
        try {
            return artService.save(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi");
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(path = "/update-art")
    public ResponseEntity<?> update(@RequestBody JsonNode jsonData) {
        try{
            return artService.update(jsonData);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Lỗi");
            return ResponseEntity.badRequest().build();
        }
    }
    @PutMapping("/browse-art")
    public ResponseEntity<?> updateArtReviewStatus(@RequestBody JsonNode jsonData) {
        try {
            // Lấy các giá trị từ JsonNode
            int idArt = jsonData.get("idArt").asInt();
            String reviewStatus = jsonData.get("reviewStatus").asText();

            // Gọi service để xử lý
            artService.updateArtReviewStatus(idArt, reviewStatus);

            return ResponseEntity.ok("DUyệt tranh thành công");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi duyệt tranh");
        }
    }


    @GetMapping(path = "/get-total")
    public long getTotal() {
        return artService.getTotalArt();
    }
}
