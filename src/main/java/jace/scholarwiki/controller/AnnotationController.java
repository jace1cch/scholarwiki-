package jace.scholarwiki.controller;

import jace.scholarwiki.model.Annotation;
import jace.scholarwiki.repository.AnnotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/annotations")
@RequiredArgsConstructor
public class AnnotationController {

    private final AnnotationRepository annotationRepository;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addAnnotation(
            @RequestBody Map<String, String> body,
            @RequestAttribute("userId") Long userId) {

        String arxivId = body.get("arxivId");
        String content = body.get("content");

        if (arxivId == null || arxivId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "arxivId must not be blank"));
        }
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "content must not be blank"));
        }

        Annotation annotation = new Annotation();
        annotation.setArxivId(arxivId);
        annotation.setContent(content);
        annotation.setUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        annotation.setCreatedAt(now);
        annotation.setUpdatedAt(now);

        annotationRepository.insert(annotation);

        return ResponseEntity.ok(Map.of(
                "id", annotation.getId(),
                "status", "CREATED"
        ));
    }

    @GetMapping
    public ResponseEntity<List<Annotation>> getAnnotations(
            @RequestParam String arxivId,
            @RequestAttribute("userId") Long userId) {

        List<Annotation> annotations = annotationRepository.findByArxivId(arxivId, userId);
        return ResponseEntity.ok(annotations);
    }
}
