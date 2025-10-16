package com.baskettecase.readmewrangler.domain;

/**
 * Bundle containing the unified diff patch and summary of improvements.
 * This is the primary output of the polishing process and requires human approval before application.
 *
 * @param unifiedDiff The unified diff format patch showing all proposed changes
 * @param summary Summary of improvements made during polishing
 */
public record PatchBundle(
    String unifiedDiff,
    ReadmeImprovements summary
) {
    /**
     * Creates a patch bundle with validation.
     */
    public PatchBundle {
        if (unifiedDiff == null) {
            unifiedDiff = "";
        }
        if (summary == null) {
            summary = ReadmeImprovements.empty();
        }
    }

    /**
     * Checks if the patch contains any changes.
     */
    public boolean hasChanges() {
        return !unifiedDiff.isBlank();
    }

    /**
     * Gets a summary line of changes.
     */
    public String getSummaryLine() {
        int changes = 0;
        if (summary.addedTestSection()) changes++;
        if (summary.fixedCodeBlocks()) changes++;
        if (summary.normalizedHeadings()) changes++;
        if (summary.addedToc()) changes++;

        return String.format("%d improvements, %d findings",
            changes,
            summary.notes().size());
    }
}
