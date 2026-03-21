package com.example.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 前端路由过滤器 - 将根路径 / 重定向到静态页面
 */
@Component
public class FrontendRouteFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 只处理根路径
        if ("/".equals(path)) {
            // 让 Spring WebFlux 处理静态资源
            return chain.filter(exchange);
        }
        
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        // 确保在网关路由之后执行
        return Ordered.LOWEST_PRECEDENCE;
    }
}
