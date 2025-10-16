package com.baskettecase.readmewrangler.tool;

import com.baskettecase.readmewrangler.domain.PolishingFinding;
import com.baskettecase.readmewrangler.domain.Severity;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lints markdown files for common issues.
 * Checks code blocks, headings, and formatting.
 */
@Component
public class MarkdownLinterTool {

    private static final Pattern CODE_FENCE_PATTERN = Pattern.compile("^```(.*)$", Pattern.MULTILINE);
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)$", Pattern.MULTILINE);

    /**
     * Lints a markdown file and returns findings.
     *
     * @param file Path to the markdown file
     * @param content Content of the markdown file
     * @return List of polishing findings
     */
    public List<PolishingFinding> lint(Path file, String content) {
        List<PolishingFinding> findings = new ArrayList<>();

        findings.addAll(checkCodeFences(file, content));
        findings.addAll(checkHeadings(file, content));

        return findings;
    }

    /**
     * Checks code fences for language tags.
     */
    private List<PolishingFinding> checkCodeFences(Path file, String content) {
        List<PolishingFinding> findings = new ArrayList<>();
        String[] lines = content.split("\n");

        boolean inCodeBlock = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith("```")) {
                if (!inCodeBlock) {
                    // Opening fence - check for language tag
                    String language = line.substring(3).trim();
                    if (language.isEmpty()) {
                        findings.add(PolishingFinding.forLine(
                            "missing-code-fence-language",
                            "Code fence missing language tag",
                            Severity.WARN,
                            file,
                            i + 1
                        ));
                    }
                    inCodeBlock = true;
                } else {
                    // Closing fence - no check needed
                    inCodeBlock = false;
                }
            }
        }

        return findings;
    }

    /**
     * Checks heading hierarchy for consistency.
     */
    private List<PolishingFinding> checkHeadings(Path file, String content) {
        List<PolishingFinding> findings = new ArrayList<>();
        String[] lines = content.split("\n");

        int previousLevel = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher matcher = HEADING_PATTERN.matcher(line);

            if (matcher.matches()) {
                int level = matcher.group(1).length();

                if (previousLevel > 0 && level > previousLevel + 1) {
                    findings.add(PolishingFinding.forLine(
                        "heading-skip-level",
                        String.format("Heading jumps from level %d to %d", previousLevel, level),
                        Severity.INFO,
                        file,
                        i + 1
                    ));
                }

                previousLevel = level;
            }
        }

        return findings;
    }

    /**
     * Counts headings in content.
     */
    public int countHeadings(String content) {
        Matcher matcher = HEADING_PATTERN.matcher(content);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
