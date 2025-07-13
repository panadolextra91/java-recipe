package com.javarecipe.backend.common.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        // Use CLOUDINARY_URL environment variable like in Node.js
        String cloudinaryUrl = "cloudinary://352188284543682:dcED4bZAlKhDr1trgPze_9Z1DK8@duy8dombh";

        return new Cloudinary(cloudinaryUrl);
    }
}
