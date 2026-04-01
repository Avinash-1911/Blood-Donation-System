package com.blooddonation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${geocode.osm.url}")
    private String nominatimUrl;

    @Value("${geocode.osm.user-agent}")
    private String userAgent;

    @Value("${geocode.osm.email}")
    private String email;

    @Value("${geocode.osm.timeout.ms}")
    private int timeoutMs;

    public GeocodingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().build();
    }

    public Optional<GeoJsonPoint> geocode(String query) {
        if (query == null || query.isBlank()) {
            return Optional.empty();
        }

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String url = String.format("%s?q=%s&format=json&limit=1&addressdetails=0&email=%s",
                    nominatimUrl, encodedQuery, encodedEmail);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("User-Agent", userAgent)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.warn("Geocoding request failed. status={} query={}", response.statusCode(), query);
                return Optional.empty();
            }

            JsonNode arr = objectMapper.readTree(response.body());
            if (!arr.isArray() || arr.isEmpty()) {
                return Optional.empty();
            }

            JsonNode first = arr.get(0);
            double lat = first.path("lat").asDouble(Double.NaN);
            double lon = first.path("lon").asDouble(Double.NaN);
            if (Double.isNaN(lat) || Double.isNaN(lon)) {
                return Optional.empty();
            }

            return Optional.of(new GeoJsonPoint(lon, lat));
        } catch (Exception e) {
            logger.warn("Geocoding failed for query={}: {}", query, e.getMessage());
            return Optional.empty();
        }
    }
}
