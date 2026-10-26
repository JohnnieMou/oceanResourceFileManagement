package com.example.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "orfm.autolog")
@Data
public class LogProperties {
    private boolean enable;
}
