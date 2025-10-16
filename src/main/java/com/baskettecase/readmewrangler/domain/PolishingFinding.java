package com.baskettecase.readmewrangler.domain;

import java.nio.file.Path;

/**
 * Represents a single finding or issue discovered during documentation polishing.
 * Includes location information and severity for precise reporting.
 *
 * @param id Unique identifier for this finding type (e.g., "missing-test-section")
 * @param message Human-readable description of the finding
 * @param severity Severity level of the finding
 * @param file File where the finding was detected
 * @param lineStart Starting line number (1-indexed)
 * @param lineEnd Ending line number (1-indexed)
 */
public record PolishingFinding(
    String id,
    String message,
    Severity severity,
    Path file,
    int lineStart,
    int lineEnd
) {
    /**
     * Creates a polishing finding with validation.
     */
    public PolishingFinding {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Finding ID cannot be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Finding message cannot be null or blank");
        }
        if (severity == null) {
            severity = Severity.INFO;
        }
        if (lineStart < 0 || lineEnd < 0 || lineEnd < lineStart) {
            throw new IllegalArgumentException("Invalid line range: " + lineStart + "-" + lineEnd);
        }
    }

    /**
     * Creates a finding for a single line.
     */
    public static PolishingFinding forLine(String id, String message, Severity severity, Path file, int line) {
        return new PolishingFinding(id, message, severity, file, line, line);
    }
}
