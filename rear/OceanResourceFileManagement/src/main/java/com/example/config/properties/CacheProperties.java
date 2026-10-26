package com.example.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
   import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.cache.redis")
@Data
public class CacheProperties {

   private long timeToLive;

}
   