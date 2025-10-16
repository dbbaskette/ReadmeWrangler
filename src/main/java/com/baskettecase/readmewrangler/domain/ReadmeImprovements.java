package com.baskettecase.readmewrangler.domain;

import java.util.List;

/**
 * Summary of improvements made to README and documentation files.
 * Tracks what changes were applied during the polishing process.
 *
 * @param addedTestSection Whether a "How to run tests" section was added
 * @param fixedCodeBlocks Whether code blocks were fixed (language tags, fencing)
 * @param normalizedHeadings Whether heading hierarchy was normalized
 * @param addedToc Whether a table of contents was added
 * @param enhancedVisuals Whether icons and visual formatting were added
 * @param notes List of detailed findings and changes made
 */
public record ReadmeImprovements(
    boolean addedTestSection,
    boolean fixedCodeBlocks,
    boolean normalizedHeadings,
    boolean addedToc,
    boolean enhancedVisuals,
    List<PolishingFinding> notes
) {
    /**
     * Creates an improvements summary with validation.
     */
    public ReadmeImprovements {
        notes = notes == null ? List.of() : List.copyOf(notes);
    }

    /**
     * Creates an empty improvements summary.
     */
    public static ReadmeImprovements empty() {
        return new ReadmeImprovements(false, false, false, false, false, List.of());
    }

    /**
     * Counts findings by severity.
     */
    public long countBySeverity(Severity severity) {
        return notes.stream()
            .filter(f -> f.severity() == severity)
            .count();
    }
}
