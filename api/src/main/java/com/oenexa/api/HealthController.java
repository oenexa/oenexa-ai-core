package com.oenexa.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health & Diagnostics REST Controller.
 * Why: Pure REST controller separated from main application runner.
 * How: Spring MVC GetMapping returning health metrics record/map.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "oenexa-ai-engineer-backend",
                "javaVersion", System.getProperty("java.version")
        ));
    }
}
