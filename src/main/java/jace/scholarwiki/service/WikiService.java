package jace.scholarwiki.service;

import jace.scholarwiki.client.EmbeddingClient;
import jace.scholarwiki.model.ChunkSearchResult;
import jace.scholarwiki.model.PaperChunk;
import jace.scholarwiki.model.SearchResult;
import jace.scholarwiki.model.WikiPage;
import jace.scholarwiki.repository.PaperChunkRepository;
import jace.scholarwiki.repository.WikiPageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class WikiService {

    private static final Pattern ANCHOR_PATTERN = Pattern.compile("\\{#([\\w-]+)\\}");

    private final WikiPageRepository wikiPageRepository;
    private final PaperChunkRepository paperChunkRepository;
    private final EmbeddingClient embeddingClient;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Parse wiki_md into chunks by {#id} anchors.
     * Each anchor starts a new chunk; content between anchors belongs to the preceding anchor.
     */
    public List<PaperChunk> extractChunks(String arxivId, String wikiMd) {
        List<PaperChunk> chunks = new ArrayList<>();
        if (wikiMd == null || wikiMd.isBlank()) {
            return chunks;
        }

        Matcher matcher = ANCHOR_PATTERN.matcher(wikiMd);
        int lastEnd = 0;
        String lastChunkId = null;
        int lastAnchorEnd = 0;

        while (matcher.find()) {
            // Save previous chunk if we have one
            if (lastChunkId != null) {
                String content = wikiMd.substring(lastAnchorEnd, matcher.start()).trim();
                if (!content.isEmpty()) {
                    PaperChunk chunk = new PaperChunk();
                    chunk.setArxivId(arxivId);
                    chunk.setChunkId(lastChunkId);
                    chunk.setContent(content);
                    chunk.setCreatedAt(LocalDateTime.now());
                    chunks.add(chunk);
                }
            }
            lastChunkId = matcher.group(1);
            lastAnchorEnd = matcher.end();
        }

        // Last chunk (content after last anchor to end)
        if (lastChunkId != null && lastAnchorEnd < wikiMd.length()) {
            String content = wikiMd.substring(lastAnchorEnd).trim();
            if (!content.isEmpty()) {
                PaperChunk chunk = new PaperChunk();
                chunk.setArxivId(arxivId);
                chunk.setChunkId(lastChunkId);
                chunk.setContent(content);
                chunk.setCreatedAt(LocalDateTime.now());
                chunks.add(chunk);
            }
        }

        return chunks;
    }

    /**
     * Save chunks and vectorize each one.
     */
    public void processChunks(String arxivId, String wikiMd) {
        // Delete old chunks
        paperChunkRepository.deleteByArxivId(arxivId);

        // Parse and save new chunks
        List<PaperChunk> chunks = extractChunks(arxivId, wikiMd);
        for (PaperChunk chunk : chunks) {
            paperChunkRepository.insert(chunk);
        }

        // Vectorize each chunk
        for (PaperChunk chunk : chunks) {
            try {
                float[] embedding = embeddingClient.embedOne(chunk.getContent());
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < embedding.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(embedding[i]);
                }
                sb.append("]");
                paperChunkRepository.updateEmbedding(arxivId, chunk.getChunkId(), sb.toString());
            } catch (Exception e) {
                // Log and continue — one chunk failure shouldn't block others
                System.err.println("Failed to vectorize chunk " + chunk.getChunkId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Vector search across paper chunks.
     */
    public SearchResult<ChunkSearchResult> vectorSearch(String query, int page, int size) {
        // Embed the query
        float[] queryEmbedding = embeddingClient.embedOne(query);

        // Convert to JSON vector string
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < queryEmbedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(queryEmbedding[i]);
        }
        sb.append("]");
        String vectorJson = sb.toString();

        // Search chunks by cosine distance
        String sql = "SELECT c.arxiv_id, p.title AS paper_title, c.chunk_id, c.content, p.tags, " +
                "cosine_distance(c.embedding, ?) AS score " +
                "FROM paper_chunk c " +
                "JOIN wiki_pages p ON c.arxiv_id = p.arxiv_id " +
                "ORDER BY score ASC " +
                "LIMIT ? OFFSET ?";
        int offset = page * size;

        List<ChunkSearchResult> items = jdbcTemplate.query(sql,
                (rs, rowNum) -> new ChunkSearchResult(
                        rs.getString("arxiv_id"),
                        rs.getString("paper_title"),
                        rs.getString("chunk_id"),
                        rs.getString("content"),
                        rs.getString("tags"),
                        rs.getDouble("score")
                ),
                vectorJson, size, offset);

        return new SearchResult<>(items, items.size(), page, size);
    }

    /**
     * Save a WikiPage to the database.
     */
    public void saveWikiPage(WikiPage page) {
        wikiPageRepository.insert(page);
    }

    /**
     * Retrieve a paper by its arXiv ID.
     */
    public Optional<WikiPage> getPaper(String arxivId) {
        return wikiPageRepository.findByArxivId(arxivId);
    }
}
