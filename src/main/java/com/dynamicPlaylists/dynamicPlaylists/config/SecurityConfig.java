package com.dynamicPlaylists.dynamicPlaylists.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Custom HttpSessionRequestCache to avoid `?continue` parameter
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setMatchingRequestParameterName(null);  // Disable the `?continue` parameter

        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/login-url", "/callback", "/save-user-playlists", "/user/updateUserCooldownDays", "/user/updateUserSkipThreshold").permitAll()  // Allow access to login and callback
                        .anyRequest().authenticated()  // Protect other endpoints
                )
                .requestCache((cache) -> cache
                        .requestCache(requestCache)
                )
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
