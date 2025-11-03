# CORS Configuration

## Global CORS Configuration (Recommended)

```java
package com.blog.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Allow Angular frontend
        corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));

        // Allow all HTTP methods
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers
        corsConfiguration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        corsConfiguration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}
```

## Alternative: application.yml Configuration

```yaml
spring:
  web:
    cors:
      allowed-origins: "http://localhost:4200"
      allowed-methods: "*"
      allowed-headers: "*"
      allow-credentials: true
      max-age: 3600
```

## Notes

- Global CORS configuration applies to all endpoints
- Allows requests from Angular frontend (localhost:4200)
- Supports all HTTP methods and headers
- Enables credentials for authentication
- Preflight requests are cached for 1 hour
- Remove `@CrossOrigin` annotations from controllers if using global config

## Production Configuration

For production, update allowed origins:

```java
// Development
corsConfiguration.setAllowedOrigins(List.of("http://localhost:4200"));

// Production
corsConfiguration.setAllowedOrigins(List.of(
    "http://localhost:4200",  // Local development
    "https://blog.kubevpro.i-consulting.shop"  // Production frontend
));
```

Or use environment variables:

```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;

corsConfiguration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
```

```yaml
# application.yml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200}
```

## TODO

- **TODO:** Add proper authentication/authorization with Spring Security
- **TODO:** Configure CORS for production environment
- **TODO:** Consider using environment-specific configuration profiles
