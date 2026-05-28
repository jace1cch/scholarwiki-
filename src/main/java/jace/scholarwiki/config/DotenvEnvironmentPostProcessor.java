package jace.scholarwiki.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment env, SpringApplication app) {
        Path dotenvPath = Path.of(System.getProperty("user.dir"), ".env");
        if (!dotenvPath.toFile().exists()) return;
        Properties props = new Properties();
        try (InputStream is = new FileInputStream(dotenvPath.toFile())) {
            props.load(is);
            env.getPropertySources().addLast(new PropertiesPropertySource("dotenv", props));
        } catch (Exception e) {
            // Silently ignore — .env is optional
        }
    }
}
