package jace.scholarwiki.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaperChunk {
    private Long id;
    private String arxivId;
    private String chunkId;
    private String content;
    private LocalDateTime createdAt;
}
