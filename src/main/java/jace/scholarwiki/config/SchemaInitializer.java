package jace.scholarwiki.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchemaInitializer {

    private final DataSource dataSource;
    private final Environment environment;

    @EventListener(ApplicationReadyEvent.class)
    public void initSchema() {
        var schemaFile = "schema.sql";
        var profiles = environment.getActiveProfiles();
        for (var p : profiles) {
            if ("local".equals(p)) {
                schemaFile = "schema-local.sql";
                break;
            }
        }
        try {
            var conn = dataSource.getConnection();
            ScriptUtils.executeSqlScript(conn, new ClassPathResource(schemaFile));
            conn.close();
            log.info("Database schema initialized successfully from {}", schemaFile);
        } catch (Exception e) {
            log.error("Failed to initialize database schema from " + schemaFile, e);
        }
    }
}
