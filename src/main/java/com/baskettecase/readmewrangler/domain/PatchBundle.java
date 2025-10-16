package com.baskettecase.readmewrangler.domain;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Bundle containing the unified diff patch and summary of improvements.
 * This is the primary output of the polishing process and requires human approval before application.
 *
 * @param unifiedDiff The unified diff format patch showing all proposed changes to README
 * @param summary Summary of improvements made during polishing
 * @param consolidationPatch Optional patch for documentation consolidation (DEVELOPMENT.md creation)
 * @param filesToDelete List of files to delete as part of consolidation
 */
public record PatchBundle(
    String unifiedDiff,
    ReadmeImprovements summary,
    String consolidationPatch,
    List<Path> filesToDelete
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
        if (consolidationPatch == null) {
            consolidationPatch = "";
        }
        if (filesToDelete == null) {
            filesToDelete = List.of();
        }
    }

    /**
     * Constructor for backward compatibility (no consolidation).
     */
    public PatchBundle(String unifiedDiff, ReadmeImprovements summary) {
        this(unifiedDiff, summary, "", List.of());
    }

    /**
     * Checks if the patch contains any changes.
     */
    public boolean hasChanges() {
        return !unifiedDiff.isBlank() || !consolidationPatch.isBlank() || !filesToDelete.isEmpty();
    }

    /**
     * Checks if this bundle includes consolidation changes.
     */
    public boolean hasConsolidation() {
        return !consolidationPatch.isBlank() || !filesToDelete.isEmpty();
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
        if (hasConsolidation()) changes++;

        String consolidationInfo = hasConsolidation()
            ? String.format(" (consolidated %d files)", filesToDelete.size())
            : "";

        return String.format("%d improvements, %d findings%s",
            changes,
            summary.notes().size(),
            consolidationInfo);
    }
}
