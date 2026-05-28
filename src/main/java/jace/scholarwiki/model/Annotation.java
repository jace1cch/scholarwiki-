package jace.scholarwiki.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Annotation {
    private Long id;
    private String arxivId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
