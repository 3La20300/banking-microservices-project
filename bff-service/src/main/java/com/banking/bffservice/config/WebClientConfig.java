package com.banking.bffservice.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Value("${external-services.user-service.base-url}")
    private String userServiceBaseUrl;

    @Value("${external-services.account-service.base-url}")
    private String accountServiceBaseUrl;

    @Value("${external-services.transaction-service.base-url}")
    private String transactionServiceBaseUrl;

    @Bean
    public WebClient userServiceWebClient() {
        return createWebClient(userServiceBaseUrl);
    }

    @Bean
    public WebClient accountServiceWebClient() {
        return createWebClient(accountServiceBaseUrl);
    }

    @Bean
    public WebClient transactionServiceWebClient() {
        return createWebClient(transactionServiceBaseUrl);
    }

    private WebClient createWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}