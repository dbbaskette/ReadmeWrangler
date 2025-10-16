package com.baskettecase.readmewrangler.tool;

import com.baskettecase.readmewrangler.domain.PolishingFinding;
import com.baskettecase.readmewrangler.domain.Severity;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MarkdownLinterTool.
 */
class MarkdownLinterToolTest {

    private final MarkdownLinterTool linter = new MarkdownLinterTool();
    private final Path testFile = Paths.get("test.md");

    @Test
    void shouldDetectMissingCodeFenceLanguage() {
        String content = """
            # Test

            ```
            code here
            ```
            """;

        List<PolishingFinding> findings = linter.lint(testFile, content);

        assertFalse(findings.isEmpty());
        assertTrue(findings.stream()
            .anyMatch(f -> f.id().equals("missing-code-fence-language")));
    }

    @Test
    void shouldNotFlagCodeFenceWithLanguage() {
        String content = """
            # Test

            ## Sub-heading

            ```java
            code here
            ```
            """;

        List<PolishingFinding> findings = linter.lint(testFile, content);

        // Should not have missing-code-fence-language finding
        assertFalse(findings.stream()
            .anyMatch(f -> f.id().equals("missing-code-fence-language")));
    }

    @Test
    void shouldDetectHeadingSkipLevel() {
        String content = """
            # Title

            ### Skipped level
            """;

        List<PolishingFinding> findings = linter.lint(testFile, content);

        assertTrue(findings.stream()
            .anyMatch(f -> f.id().equals("heading-skip-level")));
    }

    @Test
    void shouldCountHeadings() {
        String content = """
            # H1
            ## H2
            ### H3
            ## H2 Again
            """;

        int count = linter.countHeadings(content);

        assertEquals(4, count);
    }
}
