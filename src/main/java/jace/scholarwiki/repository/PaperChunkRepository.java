package jace.scholarwiki.repository;

import jace.scholarwiki.model.PaperChunk;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PaperChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public PaperChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insert(PaperChunk chunk) {
        String sql = "REPLACE INTO paper_chunk (arxiv_id, chunk_id, content, created_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, chunk.getArxivId(), chunk.getChunkId(), chunk.getContent(), chunk.getCreatedAt());
    }

    public void deleteByArxivId(String arxivId) {
        String sql = "DELETE FROM paper_chunk WHERE arxiv_id = ?";
        jdbcTemplate.update(sql, arxivId);
    }

    public List<PaperChunk> findByArxivId(String arxivId) {
        String sql = "SELECT * FROM paper_chunk WHERE arxiv_id = ? ORDER BY id ASC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(PaperChunk.class), arxivId);
    }

    public void updateEmbedding(String arxivId, String chunkId, String vectorJson) {
        String sql = "UPDATE paper_chunk SET embedding = ? WHERE arxiv_id = ? AND chunk_id = ?";
        jdbcTemplate.update(sql, vectorJson, arxivId, chunkId);
    }
}
