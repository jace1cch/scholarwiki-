package jace.scholarwiki.controller;

import jace.scholarwiki.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * POST /api/v1/admin/keys
     * Create a new API key for a user (no auth required).
     */
    @PostMapping("/keys")
    public ResponseEntity<Map<String, Object>> createApiKey(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String keyName = body.get("name");

        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username must not be blank"));
        }

        String apiKey = userService.createUserWithApiKey(username, keyName);

        return ResponseEntity.ok(Map.of(
                "apiKey", apiKey,
                "username", username,
                "warning", "Store this API key securely. It will not be shown again."
        ));
    }
}
