package com.example.web_tranh.controller;

import com.example.web_tranh.dao.ArtRepository;
import com.example.web_tranh.entity.Art;
import com.example.web_tranh.service.Art.ArtRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recommendations")
public class ArtRecommendationController {

    @Autowired
    private ArtRecommendationService artRecommendationService;
    @Autowired
    private ArtRepository artRepository ;

    // API để lấy 3 bức tranh gợi ý cho người dùng
    @GetMapping("/top-similar/{userId}")
    public List<Art> getUserInteractedArts(@PathVariable int userId) {
        return artRecommendationService.getTopSimilarArts(userId);
    }
    @GetMapping("/top_similar/{userId}")
    public List<Art> getTopSimilarArt(@PathVariable int userId) {
        List<Art> allArts = artRepository.findAll();
        Collections.shuffle(allArts);
        return allArts.stream().limit(4).collect(Collectors.toList());
    }
}
