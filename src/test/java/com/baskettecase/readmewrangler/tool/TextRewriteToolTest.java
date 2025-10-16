package com.baskettecase.readmewrangler.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TextRewriteTool.
 */
class TextRewriteToolTest {

    private final TextRewriteTool rewriter = new TextRewriteTool();

    @Test
    void shouldAddLanguageTagToCodeFence() {
        String content = """
            # Test

            ```
            mvn clean install
            ```
            """;

        String result = rewriter.fixCodeFences(content, "bash");

        assertTrue(result.contains("```bash"));
    }

    @Test
    void shouldInferBashFromCommand() {
        String content = """
            ```
            ./mvnw test
            ```
            """;

        String result = rewriter.fixCodeFences(content, "text");

        assertTrue(result.contains("```bash"));
    }

    @Test
    void shouldNormalizeSetextHeadings() {
        String content = """
            Title
            ===

            Subtitle
            ---
            """;

        String result = rewriter.normalizeHeadings(content);

        assertTrue(result.contains("# Title"));
        assertTrue(result.contains("## Subtitle"));
    }

    @Test
    void shouldRemoveTrailingWhitespace() {
        String content = "Line with trailing spaces   \nAnother line  ";

        String result = rewriter.removeTrailingWhitespace(content);

        assertEquals("Line with trailing spaces\nAnother line", result);
    }
}
