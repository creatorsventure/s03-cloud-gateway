package com.cv.s03cloudgateway.service.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebClientComponent {

    private final WebClient.Builder webClientBuilder;

    private WebClient buildClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(4));

        return webClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    public <T> Mono<T> get(String baseUrl, String uri, Class<T> responseType) {
        return buildClient(baseUrl)
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .onErrorResume(error -> {
                    logWebClientError("GET", uri, error);
                    return Mono.error(error);
                });
    }

    public <T, R> Mono<R> post(String baseUrl, String uri, T requestBody, Class<R> responseType) {
        return buildClient(baseUrl)
                .post()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .onErrorResume(error -> {
                    logWebClientError("POST", uri, error);
                    return Mono.error(error);
                });
    }

    public <T, R> Mono<R> put(String baseUrl, String uri, T requestBody, Class<R> responseType) {
        return buildClient(baseUrl)
                .put()
                .uri(uri)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .onErrorResume(error -> {
                    logWebClientError("PUT", uri, error);
                    return Mono.error(error);
                });
    }

    public Mono<Void> delete(String baseUrl, String uri) {
        return buildClient(baseUrl)
                .delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .onErrorResume(error -> {
                    logWebClientError("DELETE", uri, error);
                    return Mono.error(error);
                });
    }

    private void logWebClientError(String method, String uri, Throwable error) {
        if (error instanceof WebClientResponseException e) {
            log.error("WebClient {} {} failed: status={}, body={}",
                    method, uri, e.getStatusCode(), e.getResponseBodyAsString());
        } else if (error instanceof WebClientRequestException e) {
            log.error("WebClient {} {} failed: connection error: {}", method, uri, e.getMessage());
        } else {
            log.error("WebClient {} {} failed: unexpected error: {}", method, uri, error.getMessage(), error);
        }
    }
}
