package com.example.web_tranh.service.UserSecurity;

import com.example.web_tranh.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;
public interface UserSecurityService extends UserDetailsService {
    public User findByUsername(String username);
}