package jace.scholarwiki.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiKey {
    private Long id;
    private Long userId;
    private String apiKey;
    private String name;
    private boolean active;
    private LocalDateTime createdAt;
}
