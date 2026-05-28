package jace.scholarwiki.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChunkSearchResult {
    private String arxivId;
    private String paperTitle;
    private String chunkId;
    private String content;
    private String tags;
    private double score;
}
