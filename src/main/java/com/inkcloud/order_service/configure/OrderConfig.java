package com.inkcloud.order_service.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class OrderConfig {
    @Value("${SPRING_API_GATEWAY_CORS}")
    private String apiGateway;

    // @Bean
    // public WebMvcConfigurer corsConfigurer() {
    // return new WebMvcConfigurer() {
    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    // registry.addMapping("/api/**") // 적용할 API 경로
    // .allowedOrigins(apiGateway) // 리액트 주소 -> api 게이트웨이 주소
    // .allowedMethods("GET", "POST", "PUT", "DELETE")
    // .allowedHeaders("*")
    // .allowCredentials(true);
    // }
    // };
    // }

    @Bean
    public JPAQueryFactory queryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {

        return builder
                .baseUrl(apiGateway)
                .defaultHeaders(header -> {
                    header.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                    // header.add("Authorization", "Bearer " + portOneSecret);
                })
                .filter((request, next) -> {
                    // 요청 전처리
                    log.info("Order-Service Request: " + request.method() + " " + request.url());
                    return next.exchange(request);
                })
                .filter(ExchangeFilterFunction.ofResponseProcessor(response -> {
                    // 응답 로깅
                    log.info("Order-Service Response status code: " + response.statusCode());

                    if (response.statusCode().isError()) {
                        log.error("Error response received: " + response.statusCode());
                    }

                    return Mono.just(response);
                }))
                .build();
    }
}
