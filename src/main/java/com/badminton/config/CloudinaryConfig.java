package com.badminton.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary(
            @Value("${app.cloudinary.cloud-name:}") String cloudName,
            @Value("${app.cloudinary.api-key:}") String apiKey,
            @Value("${app.cloudinary.api-secret:}") String apiSecret) {

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", StringUtils.hasText(cloudName) ? cloudName : "demo");
        config.put("api_key", StringUtils.hasText(apiKey) ? apiKey : "demo");
        config.put("api_secret", StringUtils.hasText(apiSecret) ? apiSecret : "demo");
        config.put("secure", "true");
        return new Cloudinary(config);
    }
}
