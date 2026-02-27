package org.example.care.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
@Component
public class JwtProperties {
    private String secret;
    private long expirationMs;
    private String cookieName;
    private boolean cookieSecure;
}
