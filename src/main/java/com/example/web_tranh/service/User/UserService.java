package com.example.web_tranh.service.User;

import com.example.web_tranh.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
public interface UserService {
    public ResponseEntity<?> register(User user);
    public ResponseEntity<?> save(JsonNode userJson, String option);
    public ResponseEntity<?> delete(int id);
    public ResponseEntity<?> changePassword(JsonNode userJson);
    public ResponseEntity<?> changeAvatar(JsonNode userJson);
    public ResponseEntity<?> updateProfile(JsonNode userJson);
    public ResponseEntity<?> forgotPassword(JsonNode jsonNode);
    public User getUserById(int id);
}
