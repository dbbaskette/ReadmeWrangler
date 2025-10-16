package com.baskettecase.readmewrangler.tool;

import com.baskettecase.readmewrangler.domain.BuildSystem;
import com.baskettecase.readmewrangler.domain.RepoSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Scans repositories to detect build systems, test commands, and relevant files.
 * Provides heuristics for detecting Maven, Gradle, Makefile-based builds.
 */
@Component
public class RepoScannerTool {

    private static final Logger log = LoggerFactory.getLogger(RepoScannerTool.class);

    /**
     * Scans a repository and creates a snapshot with detected metadata.
     *
     * @param rootPath Root directory of the repository
     * @return RepoSnapshot containing detected files and metadata
     * @throws IOException if scanning fails
     */
    public RepoSnapshot scanRepository(Path rootPath) throws IOException {
        if (!Files.isDirectory(rootPath)) {
            throw new IllegalArgumentException("Path must be a directory: " + rootPath);
        }

        BuildSystem buildSystem = detectBuildSystem(rootPath);
        List<Path> markdownFiles = findMarkdownFiles(rootPath);
        List<Path> scripts = findScripts(rootPath);

        log.info("Scanned repository at {}: {} markdown files, build system: {}",
            rootPath, markdownFiles.size(), buildSystem);

        return new RepoSnapshot(rootPath, markdownFiles, buildSystem, scripts);
    }

    /**
     * Detects the build system used by the repository.
     */
    private BuildSystem detectBuildSystem(Path root) {
        if (Files.exists(root.resolve("pom.xml")) || Files.exists(root.resolve("mvnw"))) {
            return BuildSystem.MAVEN;
        }
        if (Files.exists(root.resolve("build.gradle")) ||
            Files.exists(root.resolve("build.gradle.kts")) ||
            Files.exists(root.resolve("gradlew"))) {
            return BuildSystem.GRADLE;
        }
        if (Files.exists(root.resolve("Makefile"))) {
            return BuildSystem.MAKEFILE;
        }
        return BuildSystem.OTHER;
    }

    /**
     * Finds all markdown files in the repository.
     */
    private List<Path> findMarkdownFiles(Path root) throws IOException {
        List<Path> markdownFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(Files::isRegularFile)
                .filter(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    return name.endsWith(".md");
                })
                .filter(p -> !isIgnoredPath(root, p))
                .forEach(markdownFiles::add);
        }

        return markdownFiles;
    }

    /**
     * Finds script files (test runners, build scripts).
     */
    private List<Path> findScripts(Path root) throws IOException {
        List<Path> scripts = new ArrayList<>();

        // Check common script directories
        Path scriptsDir = root.resolve("scripts");
        if (Files.isDirectory(scriptsDir)) {
            try (Stream<Path> paths = Files.walk(scriptsDir, 2)) {
                paths.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.startsWith("test") || name.startsWith("build") ||
                               name.endsWith(".sh") || name.endsWith(".bat");
                    })
                    .forEach(scripts::add);
            }
        }

        // Check root for wrapper scripts
        List.of("mvnw", "gradlew", "test.sh", "build.sh").forEach(name -> {
            Path script = root.resolve(name);
            if (Files.exists(script)) {
                scripts.add(script);
            }
        });

        return scripts;
    }

    /**
     * Determines if a path should be ignored (e.g., node_modules, .git, target).
     */
    private boolean isIgnoredPath(Path root, Path file) {
        Path relative = root.relativize(file);
        String pathStr = relative.toString();

        return pathStr.contains("node_modules") ||
               pathStr.contains(".git") ||
               pathStr.contains("target") ||
               pathStr.contains("build") ||
               pathStr.contains(".idea") ||
               pathStr.contains(".vscode");
    }

    /**
     * Generates a recommended test command snippet based on detected build system.
     *
     * @param buildSystem The detected build system
     * @return Markdown snippet with test command
     */
    public String generateTestCommandSnippet(BuildSystem buildSystem) {
        return switch (buildSystem) {
            case MAVEN -> """
                ## How to Run Tests

                ```bash
                # Using Maven wrapper (recommended)
                ./mvnw test

                # Or with installed Maven
                mvn test
                ```
                """;
            case GRADLE -> """
                ## How to Run Tests

                ```bash
                # Using Gradle wrapper (recommended)
                ./gradlew test

                # Or with installed Gradle
                gradle test
                ```
                """;
            case MAKEFILE -> """
                ## How to Run Tests

                ```bash
                make test
                ```
                """;
            case OTHER -> """
                ## How to Run Tests

                ```bash
                # Add your test command here
                ```
                """;
        };
    }

    /**
     * Determines if a README needs a test section.
     *
     * @param content README content
     * @return true if test section is missing
     */
    public boolean needsTestSection(String content) {
        String lower = content.toLowerCase();
        return !lower.contains("run test") &&
               !lower.contains("running test") &&
               !lower.contains("test command");
    }
}
