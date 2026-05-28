package jace.scholarwiki.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jace.scholarwiki.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Bypass auth for non-/api/ paths and admin key creation
        if (!path.startsWith("/api/") || path.equals("/api/v1/admin/keys")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader(API_KEY_HEADER);

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Missing API key for request: {}", path);
            sendUnauthorized(response, "Missing API key");
            return;
        }

        var userIdOpt = userService.validateApiKey(apiKey);

        if (userIdOpt.isEmpty()) {
            log.warn("Invalid API key for request: {}", path);
            sendUnauthorized(response, "Invalid API key");
            return;
        }

        Long userId = userIdOpt.get();

        // Set authentication in security context
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList()
                );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Set userId as request attribute for downstream use
        request.setAttribute("userId", userId);

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Map.of("error", message));
    }
}
