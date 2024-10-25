package com.example.web_tranh.controller;

import com.example.web_tranh.dao.ArtRepository;
import com.example.web_tranh.dao.FavoriteArtRepository;
import com.example.web_tranh.dao.UserRepository;
import com.example.web_tranh.entity.Art;
import com.example.web_tranh.entity.FavoriteArt;
import com.example.web_tranh.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/favorite-art")
public class FavoriteArtController {

    @Autowired
    private ArtRepository artRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FavoriteArtRepository favoriteArtRepository;

    @GetMapping("/get-favorite-art/{idUser}")
    public ResponseEntity<?> getAllFavoriteArtByIdUser(@PathVariable Integer idUser) {
        try{
            User user = userRepository.findById(idUser).get();
            List<FavoriteArt> favoriteArtList = favoriteArtRepository.findFavoriteArtsByUser(user);
            List<Integer> idArtListOfFavoriteArt = new ArrayList<>();
            for (FavoriteArt favoriteArt : favoriteArtList) {
                idArtListOfFavoriteArt.add(favoriteArt.getArt().getIdArt());
            }
            return ResponseEntity.ok().body(idArtListOfFavoriteArt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping("/add-art")
    public ResponseEntity<?> save(@RequestBody JsonNode jsonNode) {
        try{
            int idArt = Integer.parseInt(formatStringByJson(jsonNode.get("idArt").toString()));
            int idUser = Integer.parseInt(formatStringByJson(jsonNode.get("idUser").toString()));

            Art art = artRepository.findById(idArt).get();
            User user = userRepository.findById(idUser).get();

            FavoriteArt favoriteArt = FavoriteArt.builder().art(art).user(user).build();

            favoriteArtRepository.save(favoriteArt);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-art")
    public ResponseEntity<?> remove(@RequestBody JsonNode jsonNode) {
        try{
            int idArt = Integer.parseInt(formatStringByJson(jsonNode.get("idArt").toString()));
            int idUser = Integer.parseInt(formatStringByJson(jsonNode.get("idUser").toString()));

            Art art = artRepository.findById(idArt).get();
            User user = userRepository.findById(idUser).get();

            FavoriteArt favoriteArt = favoriteArtRepository.findFavoriteArtByArtAndUser(art, user);

            favoriteArtRepository.delete(favoriteArt);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    private String formatStringByJson(String json) {
        return json.replaceAll("\"", "");
    }
}