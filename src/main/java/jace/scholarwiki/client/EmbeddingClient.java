package jace.scholarwiki.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public EmbeddingClient(
            WebClient.Builder webClientBuilder,
            @Value("${embedding.api.url}") String apiUrl,
            @Value("${embedding.api.key}") String apiKey,
            @Value("${embedding.api.model}") String model,
            @Value("${embedding.api.dimension}") int dimension) {
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public List<float[]> embed(List<String> texts) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "input", texts
            ));

            String response = webClient.post()
                    .uri("/embeddings")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode data = root.get("data");
            List<float[]> embeddings = new ArrayList<>();
            for (JsonNode item : data) {
                JsonNode embeddingNode = item.get("embedding");
                float[] embedding = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    embedding[i] = (float) embeddingNode.get(i).asDouble();
                }
                embeddings.add(embedding);
            }
            return embeddings;
        } catch (Exception e) {
            throw new RuntimeException("Embedding API call failed", e);
        }
    }

    public float[] embedOne(String text) {
        List<float[]> result = embed(List.of(text));
        if (result.isEmpty()) {
            throw new RuntimeException("Embedding API returned empty result");
        }
        return result.get(0);
    }
}
