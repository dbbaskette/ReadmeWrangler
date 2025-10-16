package com.baskettecase.readmewrangler.tool;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Builds unified diff patches from original and modified content.
 */
@Component
public class PatchBuilderTool {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
            .withZone(ZoneId.systemDefault());

    /**
     * Creates a unified diff between original and modified content.
     *
     * @param filePath Path to the file being patched
     * @param original Original content
     * @param modified Modified content
     * @return Unified diff string
     */
    public String createUnifiedDiff(Path filePath, String original, String modified) {
        if (original.equals(modified)) {
            return "";
        }

        StringBuilder diff = new StringBuilder();
        String fileName = filePath != null ? filePath.toString() : "unknown";
        String timestamp = TIMESTAMP_FORMAT.format(Instant.now());

        diff.append("--- a/").append(fileName).append("\t").append(timestamp).append("\n");
        diff.append("+++ b/").append(fileName).append("\t").append(timestamp).append("\n");

        String[] originalLines = original.split("\n", -1);
        String[] modifiedLines = modified.split("\n", -1);

        // Simple line-by-line diff (for production, use a proper diff library)
        diff.append(createSimpleDiff(originalLines, modifiedLines));

        return diff.toString();
    }

    /**
     * Creates a simple line-by-line diff.
     * For production use, integrate with a proper diff library like java-diff-utils.
     */
    private String createSimpleDiff(String[] original, String[] modified) {
        StringBuilder result = new StringBuilder();

        result.append("@@ -1,").append(original.length)
              .append(" +1,").append(modified.length)
              .append(" @@\n");

        // Show removed lines
        for (String line : original) {
            if (!contains(modified, line)) {
                result.append("-").append(line).append("\n");
            }
        }

        // Show added lines
        for (String line : modified) {
            if (!contains(original, line)) {
                result.append("+").append(line).append("\n");
            }
        }

        // Show common lines with context
        int contextLines = 3;
        for (int i = 0; i < Math.min(contextLines, Math.min(original.length, modified.length)); i++) {
            if (i < original.length && i < modified.length && original[i].equals(modified[i])) {
                result.append(" ").append(original[i]).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Helper to check if array contains a line.
     */
    private boolean contains(String[] lines, String target) {
        for (String line : lines) {
            if (line.equals(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Writes patch to a file.
     *
     * @param patch Patch content
     * @param outputPath Output file path
     * @throws IOException if writing fails
     */
    public void writePatchToFile(String patch, Path outputPath) throws IOException {
        Files.writeString(outputPath, patch);
    }
}
