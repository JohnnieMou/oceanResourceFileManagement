package com.example.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "orfm.jwt")
@Data
public class JwtProperties {

    private String secretKey;

    private Long ttl;

    private String tokenName;
}
