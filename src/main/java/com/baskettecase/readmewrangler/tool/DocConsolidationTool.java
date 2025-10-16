package com.baskettecase.readmewrangler.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

/**
 * Tool for consolidating multiple documentation markdown files into a single DEVELOPMENT.md.
 * Excludes Code Assistant-specific files (CLAUDE.md) and README.md.
 */
@Component
public class DocConsolidationTool {

    private static final Logger log = LoggerFactory.getLogger(DocConsolidationTool.class);

    // Files to exclude from consolidation
    private static final Set<String> EXCLUDED_FILES = Set.of(
        "CLAUDE.md",
        "README.md",
        "DEVELOPMENT.md"
    );

    /**
     * Consolidates all documentation markdown files into DEVELOPMENT.md.
     *
     * @param repoRoot Repository root path
     * @return ConsolidationResult with consolidated content and files to remove
     * @throws IOException if file operations fail
     */
    public ConsolidationResult consolidateDocumentation(Path repoRoot) throws IOException {
        log.info("Consolidating documentation files in {}", repoRoot);

        List<Path> docsToConsolidate = findDocumentationFiles(repoRoot);

        if (docsToConsolidate.isEmpty()) {
            log.info("No documentation files to consolidate");
            return new ConsolidationResult("", List.of(), false);
        }

        StringBuilder consolidated = new StringBuilder();
        consolidated.append("# Development Guide\n\n");
        consolidated.append("This document consolidates all development and process documentation for README Wrangler.\n\n");
        consolidated.append("---\n\n");

        // Add table of contents
        consolidated.append("## Table of Contents\n\n");
        for (Path doc : docsToConsolidate) {
            String fileName = doc.getFileName().toString();
            String sectionName = fileNameToSectionName(fileName);
            String anchor = sectionName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
            consolidated.append("- [").append(sectionName).append("](#").append(anchor).append(")\n");
        }
        consolidated.append("\n---\n\n");

        // Consolidate content from each file
        for (Path doc : docsToConsolidate) {
            String fileName = doc.getFileName().toString();
            String sectionName = fileNameToSectionName(fileName);
            String content = Files.readString(doc);

            log.info("  Consolidating: {}", fileName);

            consolidated.append("## ").append(sectionName).append("\n\n");

            // Remove existing top-level heading if present
            String processedContent = removeTopLevelHeading(content);

            // Deduplicate and clean content
            processedContent = deduplicateSections(processedContent);

            consolidated.append(processedContent);
            consolidated.append("\n\n---\n\n");
        }

        log.info("Consolidated {} files into DEVELOPMENT.md", docsToConsolidate.size());

        return new ConsolidationResult(
            consolidated.toString(),
            docsToConsolidate,
            true
        );
    }

    /**
     * Finds all documentation markdown files to consolidate.
     */
    private List<Path> findDocumentationFiles(Path repoRoot) throws IOException {
        List<Path> docs = new ArrayList<>();

        try (Stream<Path> stream = Files.list(repoRoot)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".md"))
                .filter(p -> !EXCLUDED_FILES.contains(p.getFileName().toString()))
                .filter(Files::isRegularFile)
                .sorted()
                .forEach(docs::add);
        }

        return docs;
    }

    /**
     * Converts filename to section name.
     * Example: GETTING_STARTED.md -> Getting Started
     */
    private String fileNameToSectionName(String fileName) {
        String name = fileName.replace(".md", "")
                             .replace(".MD", "")
                             .replace("_", " ")
                             .replace("-", " ");

        // Title case
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Removes the first top-level heading (# Heading) from content.
     */
    private String removeTopLevelHeading(String content) {
        String[] lines = content.split("\n");
        if (lines.length > 0 && lines[0].trim().startsWith("# ")) {
            // Skip first line and any following blank lines
            int startIdx = 1;
            while (startIdx < lines.length && lines[startIdx].trim().isEmpty()) {
                startIdx++;
            }
            return String.join("\n", Arrays.copyOfRange(lines, startIdx, lines.length));
        }
        return content;
    }

    /**
     * Basic deduplication: removes consecutive duplicate paragraphs.
     */
    private String deduplicateSections(String content) {
        String[] paragraphs = content.split("\n\n");
        List<String> deduplicated = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String para : paragraphs) {
            String normalized = para.trim().toLowerCase();
            if (!normalized.isEmpty() && !seen.contains(normalized)) {
                deduplicated.add(para);
                seen.add(normalized);
            }
        }

        return String.join("\n\n", deduplicated);
    }

    /**
     * Result of documentation consolidation.
     */
    public record ConsolidationResult(
        String consolidatedContent,
        List<Path> filesToRemove,
        boolean hasConsolidation
    ) {}
}
