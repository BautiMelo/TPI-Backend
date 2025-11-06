package com.backend.tpi.api_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class GatewayTokenService {

    private final RestClient restClient;
    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    // Cached token
    private volatile String accessToken;
    private volatile Instant expiresAt = Instant.EPOCH;
    private final ReentrantLock lock = new ReentrantLock();

    public GatewayTokenService(RestClient restClient,
                               @Value("${app.oauth2.token-uri}") String tokenUri,
                               @Value("${app.oauth2.client-id}") String clientId,
                               @Value("${app.oauth2.client-secret}") String clientSecret,
                               @Value("${app.oauth2.scope:}") String scope) {
        this.restClient = restClient;
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope == null ? "" : scope;
    }

    /**
     * Return a valid access token. If cached token expired (with 10s buffer), fetch a new one.
     */
    public String getAccessToken() {
        Instant now = Instant.now();
        if (accessToken != null && expiresAt.minusSeconds(10).isAfter(now)) {
            return accessToken;
        }

        lock.lock();
        try {
            // re-check after lock
            now = Instant.now();
            if (accessToken != null && expiresAt.minusSeconds(10).isAfter(now)) {
                return accessToken;
            }

            // request a new token using client_credentials (form-encoded)
            try {
                ResponseEntity<Map<String, Object>> respEntity = restClient.post()
                        .uri(tokenUri)
                        .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                                .with("client_id", clientId)
                                .with("client_secret", clientSecret)
                                .with("scope", scope))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

                Map<String, Object> response = respEntity != null ? respEntity.getBody() : null;
                if (response == null || !response.containsKey("access_token")) {
                    throw new IllegalStateException("Token endpoint returned no access_token");
                }
                this.accessToken = (String) response.get("access_token");
                Number expiresIn = (Number) response.getOrDefault("expires_in", 300);
                this.expiresAt = Instant.now().plusSeconds(expiresIn.longValue());
                return accessToken;
            } catch (HttpClientErrorException e) {
                throw new RuntimeException("Failed to get token from token endpoint: " + e.getResponseBodyAsString(), e);
            }
        } finally {
            lock.unlock();
        }
    }
}
