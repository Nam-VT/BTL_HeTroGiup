package it4341.HeTroGiup.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeocodingService {
    private final WebClient client = WebClient.create("https://nominatim.openstreetmap.org");

    public double[] getCoordinates(String query) {
        String cleanQuery = query != null ? query.trim() : "";

        List<Map<String, Object>> resp = client.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("q", cleanQuery)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .path("/search")
                        .build()
                )
                .header("User-Agent", "demo-app")
                .retrieve()
                .bodyToMono(List.class)
                .block();

        if (resp == null || resp.isEmpty()) {
            throw new RuntimeException("Location not found: " + query);
        }

        Map<String, Object> place = resp.get(0);
        double lat = Double.parseDouble((String) place.get("lat"));
        double lon = Double.parseDouble((String) place.get("lon"));

        return new double[]{lat, lon};
    }
}
