package com.example.gateway.config;

import com.example.gateway.filter.OidcAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway 路由配置
 */
@Configuration
public class GatewayConfig {

    @Autowired
    private OidcAuthenticationFilter oidcAuthenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // user-service
                .route("user-service", r -> r
                        .path("/user/**")
                        .uri("http://localhost:8081"))
                // config-service
                .route("config-service", r -> r
                        .path("/config/**")
                        .uri("http://localhost:8082"))
                // ops-service
                .route("ops-service", r -> r
                        .path("/ops/**")
                        .uri("http://localhost:8083"))
                .build();
    }
}
