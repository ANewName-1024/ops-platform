package com.example.gateway.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 前端页面控制器
 */
@RestController
public class FrontendController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> index() throws IOException {
        return loadHtml("static/index.html");
    }

    private Mono<String> loadHtml(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        if (resource.exists()) {
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return Mono.just(content);
        }
        return Mono.just("<!DOCTYPE html><html><head><title>OPS Platform</title></head><body><h1>Loading...</h1></body></html>");
    }
}
