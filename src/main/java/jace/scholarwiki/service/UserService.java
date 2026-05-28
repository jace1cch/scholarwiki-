package jace.scholarwiki.service;

import jace.scholarwiki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Validate an API key and return the associated user ID if valid.
     * Returns empty if the key does not exist or is not active.
     */
    public Optional<Long> validateApiKey(String apiKey) {
        return userRepository.findApiKey(apiKey)
                .filter(k -> k.isActive())
                .map(k -> k.getUserId());
    }

    /**
     * Create a new user (if not already exists) and generate an API key for them.
     *
     * @param username the username to create
     * @param keyName  a human-readable label for the API key
     * @return the generated API key string in format "psk-<uuid>"
     */
    public String createUserWithApiKey(String username, String keyName) {
        Long userId = userRepository.findByUsername(username)
                .map(user -> user.getId())
                .orElseGet(() -> userRepository.createUser(username));

        String apiKey = "psk-" + UUID.randomUUID().toString().replace("-", "");
        userRepository.createApiKey(userId, apiKey, keyName);
        return apiKey;
    }
}
