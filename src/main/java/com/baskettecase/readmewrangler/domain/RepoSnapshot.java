package com.baskettecase.readmewrangler.domain;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents a snapshot of a repository for analysis.
 * Captures all relevant files and metadata needed for polishing documentation.
 *
 * @param root Root path of the repository
 * @param markdownFiles List of markdown files found in the repository
 * @param build Detected build system (Maven, Gradle, etc.)
 * @param scripts List of script files (test runners, build scripts, etc.)
 */
public record RepoSnapshot(
    Path root,
    List<Path> markdownFiles,
    BuildSystem build,
    List<Path> scripts
) {
    /**
     * Creates a repository snapshot with validation.
     */
    public RepoSnapshot {
        if (root == null) {
            throw new IllegalArgumentException("Repository root cannot be null");
        }
        markdownFiles = markdownFiles == null ? List.of() : List.copyOf(markdownFiles);
        scripts = scripts == null ? List.of() : List.copyOf(scripts);
        if (build == null) {
            build = BuildSystem.OTHER;
        }
    }
}
