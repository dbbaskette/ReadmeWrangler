package com.baskettecase.readmewrangler.tool;

import com.baskettecase.readmewrangler.domain.BuildSystem;
import com.baskettecase.readmewrangler.domain.RepoSnapshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RepoScannerTool.
 */
class RepoScannerToolTest {

    private final RepoScannerTool scanner = new RepoScannerTool();

    @Test
    void shouldDetectMavenBuildSystem(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("pom.xml"));

        RepoSnapshot snapshot = scanner.scanRepository(tempDir);

        assertEquals(BuildSystem.MAVEN, snapshot.build());
    }

    @Test
    void shouldDetectGradleBuildSystem(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("build.gradle"));

        RepoSnapshot snapshot = scanner.scanRepository(tempDir);

        assertEquals(BuildSystem.GRADLE, snapshot.build());
    }

    @Test
    void shouldFindMarkdownFiles(@TempDir Path tempDir) throws Exception {
        Files.createFile(tempDir.resolve("README.md"));
        Files.createFile(tempDir.resolve("CONTRIBUTING.md"));

        Path docsDir = tempDir.resolve("docs");
        Files.createDirectory(docsDir);
        Files.createFile(docsDir.resolve("API.md"));

        RepoSnapshot snapshot = scanner.scanRepository(tempDir);

        assertEquals(3, snapshot.markdownFiles().size());
    }

    @Test
    void shouldGenerateTestCommandForMaven() {
        String snippet = scanner.generateTestCommandSnippet(BuildSystem.MAVEN);

        assertTrue(snippet.contains("./mvnw test"));
        assertTrue(snippet.contains("How to Run Tests"));
    }

    @Test
    void shouldDetectMissingTestSection() {
        String content = "# My Project\n\nSome description";

        assertTrue(scanner.needsTestSection(content));
    }

    @Test
    void shouldDetectExistingTestSection() {
        String content = "# My Project\n\n## How to run tests\n\nRun `mvn test`";

        assertFalse(scanner.needsTestSection(content));
    }
}
