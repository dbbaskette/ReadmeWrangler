package com.baskettecase.readmewrangler.controller;

import com.baskettecase.readmewrangler.domain.PatchBundle;
import com.baskettecase.readmewrangler.service.PolishingConfig;
import com.baskettecase.readmewrangler.service.PolishingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * REST controller for documentation polishing endpoints.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Polish", description = "Documentation polishing operations")
public class PolishController {

    private static final Logger log = LoggerFactory.getLogger(PolishController.class);

    private final PolishingService polishingService;

    public PolishController(PolishingService polishingService) {
        this.polishingService = polishingService;
    }

    /**
     * Polishes a repository's documentation and returns a patch.
     *
     * @param request Polish request with repository path
     * @return PatchBundle with proposed changes
     */
    @PostMapping("/polish")
    @Operation(summary = "Polish repository documentation", description = "Analyzes and improves README and documentation files, returning a patch for review")
    public ResponseEntity<PatchBundle> polish(@RequestBody PolishRequest request) {
        try {
            log.info("Received polish request for: {}", request.repoPath());

            Path repoPath = Paths.get(request.repoPath());
            PolishingConfig config = request.config() != null ? request.config() : PolishingConfig.defaults();

            PatchBundle bundle = polishingService.polishRepository(repoPath, config);

            return ResponseEntity.ok(bundle);

        } catch (IOException e) {
            log.error("Failed to polish repository", e);
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns OK if service is running")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("OK", "README Wrangler is running"));
    }

    /**
     * Request model for polish endpoint.
     */
    public record PolishRequest(
        String repoPath,
        PolishingConfig config
    ) {
    }

    /**
     * Response model for health endpoint.
     */
    public record HealthResponse(
        String status,
        String message
    ) {
    }
}
