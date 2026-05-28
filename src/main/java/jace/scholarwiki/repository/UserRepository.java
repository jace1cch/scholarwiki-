package jace.scholarwiki.repository;

import jace.scholarwiki.model.ApiKey;
import jace.scholarwiki.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final BeanPropertyRowMapper<User> USER_MAPPER =
            BeanPropertyRowMapper.newInstance(User.class);

    private static final BeanPropertyRowMapper<ApiKey> API_KEY_MAPPER =
            BeanPropertyRowMapper.newInstance(ApiKey.class);

    public Optional<User> findById(Long id) {
        var users = jdbcTemplate.query(
                "SELECT * FROM users WHERE id = ?",
                USER_MAPPER,
                id
        );
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findByUsername(String username) {
        var users = jdbcTemplate.query(
                "SELECT * FROM users WHERE username = ?",
                USER_MAPPER,
                username
        );
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Long createUser(String username) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (username) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, username);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<ApiKey> findApiKey(String apiKey) {
        var keys = jdbcTemplate.query(
                "SELECT * FROM api_keys WHERE api_key = ?",
                API_KEY_MAPPER,
                apiKey
        );
        return keys.isEmpty() ? Optional.empty() : Optional.of(keys.get(0));
    }

    public Long createApiKey(Long userId, String apiKey, String name) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO api_keys (user_id, api_key, name) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, userId);
            ps.setString(2, apiKey);
            ps.setString(3, name);
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }
}
