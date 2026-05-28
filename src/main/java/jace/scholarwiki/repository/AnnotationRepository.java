package jace.scholarwiki.repository;

import jace.scholarwiki.model.Annotation;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class AnnotationRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnnotationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long insert(Annotation annotation) {
        String sql = "INSERT INTO annotations (arxiv_id, user_id, content, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, annotation.getArxivId());
            ps.setLong(2, annotation.getUserId());
            ps.setString(3, annotation.getContent());
            ps.setObject(4, annotation.getCreatedAt());
            ps.setObject(5, annotation.getUpdatedAt());
            return ps;
        }, keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        annotation.setId(id);
        return id;
    }

    public Optional<Annotation> findById(Long id) {
        String sql = "SELECT * FROM annotations WHERE id = ?";
        List<Annotation> results = jdbcTemplate.query(sql,
                new BeanPropertyRowMapper<>(Annotation.class), id);
        return results.stream().findFirst();
    }

    public List<Annotation> findByArxivId(String arxivId, Long userId) {
        String sql = "SELECT * FROM annotations WHERE arxiv_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Annotation.class), arxivId);
    }
}
