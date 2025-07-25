package com.example.web_tranh.security;

import com.example.web_tranh.service.Jwt.JwtFilter;
import com.example.web_tranh.service.UserSecurity.UserSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // Khi đăng nhâp thì sẽ vào hàm này đâu tiên để kiểm tra
    @Bean
    public DaoAuthenticationProvider authenticationProvider (UserSecurityService userSecurityService) {
        DaoAuthenticationProvider dap = new DaoAuthenticationProvider();
        dap.setUserDetailsService(userSecurityService);
        dap.setPasswordEncoder(passwordEncoder());
        return dap;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cấu hình phân quyền cho endpoint
        http.authorizeHttpRequests(
                config->config
                        .requestMatchers(HttpMethod.GET, Endpoints.PUBLIC_GET).permitAll()
                        .requestMatchers(HttpMethod.POST, Endpoints.PUBLIC_POST).permitAll()
                        .requestMatchers(HttpMethod.PUT, Endpoints.PUBLIC_PUT).permitAll()
                        .requestMatchers(HttpMethod.DELETE, Endpoints.PUBLIC_DELETE).permitAll()
                        .requestMatchers(HttpMethod.GET, Endpoints.ADMIN_ENDPOINT).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, Endpoints.ADMIN_ENDPOINT).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, Endpoints.ADMIN_ENDPOINT).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, Endpoints.ADMIN_ENDPOINT).hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, Endpoints.ARTIST_ENDPOINT).hasAuthority("ARTIST")
                        .requestMatchers(HttpMethod.POST, Endpoints.ARTIST_ENDPOINT).hasAuthority("ARTIST")
                        .requestMatchers(HttpMethod.PUT, Endpoints.ARTIST_ENDPOINT).hasAuthority("ARTIST")
                        .requestMatchers(HttpMethod.DELETE, Endpoints.ARTIST_ENDPOINT).hasAuthority("ARTIST")
        );
        // Cấu hình cors
        http.cors(cors -> {
            cors.configurationSource(request -> {
                CorsConfiguration corsConfig = new CorsConfiguration();
                corsConfig.addAllowedOrigin("*");
                corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                corsConfig.addAllowedHeader("*");
                return corsConfig;
            });
        });

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.httpBasic(Customizer.withDefaults());
        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}