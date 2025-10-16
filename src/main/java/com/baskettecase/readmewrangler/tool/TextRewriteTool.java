package com.baskettecase.readmewrangler.tool;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rewrites markdown text to improve clarity and consistency.
 * Fixes code fences, tightens language, and normalizes formatting.
 */
@Component
public class TextRewriteTool {

    private static final Pattern CODE_FENCE_NO_LANG = Pattern.compile("^```\\s*$", Pattern.MULTILINE);

    /**
     * Fixes code fences by adding language tags where missing.
     *
     * @param content Markdown content
     * @param defaultLanguage Default language tag to use (e.g., "bash", "java")
     * @return Fixed content
     */
    public String fixCodeFences(String content, String defaultLanguage) {
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n", -1);

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (line.startsWith("```") && line.substring(3).trim().isEmpty()) {
                // Infer language from next line if possible
                String inferredLang = inferLanguage(lines, i + 1, defaultLanguage);
                result.append("```").append(inferredLang).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString().trim();
    }

    /**
     * Attempts to infer language from code content.
     */
    private String inferLanguage(String[] lines, int startIdx, String defaultLang) {
        if (startIdx >= lines.length) {
            return defaultLang;
        }

        String nextLine = lines[startIdx].trim();

        if (nextLine.startsWith("mvn ") || nextLine.startsWith("./mvnw") ||
            nextLine.startsWith("gradle") || nextLine.startsWith("./gradlew") ||
            nextLine.startsWith("cd ") || nextLine.startsWith("ls ") ||
            nextLine.startsWith("npm ") || nextLine.startsWith("make ")) {
            return "bash";
        }

        if (nextLine.startsWith("public class") || nextLine.startsWith("import ") ||
            nextLine.startsWith("package ") || nextLine.contains("@")) {
            return "java";
        }

        if (nextLine.startsWith("{") || nextLine.startsWith("---")) {
            return "yaml";
        }

        if (nextLine.startsWith("<") && nextLine.contains(">")) {
            return "xml";
        }

        return defaultLang;
    }

    /**
     * Normalizes heading styles to ATX format (using #).
     *
     * @param content Markdown content
     * @return Content with normalized headings
     */
    public String normalizeHeadings(String content) {
        // Convert setext headings (underlines) to ATX (hashes)
        String result = content;

        // H1: ===
        result = result.replaceAll("(?m)^(.+)\\n={2,}$", "# $1");

        // H2: ---
        result = result.replaceAll("(?m)^(.+)\\n-{2,}$", "## $1");

        return result;
    }

    /**
     * Removes trailing whitespace from lines.
     *
     * @param content Markdown content
     * @return Content with trailing whitespace removed
     */
    public String removeTrailingWhitespace(String content) {
        return content.replaceAll("(?m)[ \\t]+$", "");
    }

    /**
     * Ensures consistent line endings.
     *
     * @param content Markdown content
     * @return Content with consistent line endings
     */
    public String normalizeLineEndings(String content) {
        return content.replaceAll("\\r\\n", "\n");
    }
}
