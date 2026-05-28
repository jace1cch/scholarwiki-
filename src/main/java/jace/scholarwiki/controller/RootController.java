package jace.scholarwiki.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, Object> index() {
        return Map.of(
                "service", "ScholarWiki",
                "version", "2.0",
                "description", "Academic Paper Wiki Engine",
                "timestamp", LocalDateTime.now().toString()
        );
    }
}
