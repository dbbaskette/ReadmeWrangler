package com.baskettecase.readmewrangler.service;

import com.baskettecase.readmewrangler.agent.ReadmePolisherAgent;
import com.baskettecase.readmewrangler.domain.*;
import com.baskettecase.readmewrangler.tool.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Core service that orchestrates the README polishing process.
 * Now delegates to Embabel agent for GOAP-based planning.
 */
@Service
public class PolishingService {

    private static final Logger log = LoggerFactory.getLogger(PolishingService.class);

    private final ReadmePolisherAgent agent;
    private final RepoScannerTool repoScanner;
    private final MarkdownLinterTool linter;
    private final TextRewriteTool rewriter;
    private final TocTool tocTool;
    private final BadgeTool badgeTool;
    private final PatchBuilderTool patchBuilder;

    public PolishingService(
        ReadmePolisherAgent agent,
        RepoScannerTool repoScanner,
        MarkdownLinterTool linter,
        TextRewriteTool rewriter,
        TocTool tocTool,
        BadgeTool badgeTool,
        PatchBuilderTool patchBuilder
    ) {
        this.agent = agent;
        this.repoScanner = repoScanner;
        this.linter = linter;
        this.rewriter = rewriter;
        this.tocTool = tocTool;
        this.badgeTool = badgeTool;
        this.patchBuilder = patchBuilder;
    }

    /**
     * Polishes a repository's documentation and generates a patch.
     * Delegates to Embabel agent for goal-oriented planning.
     *
     * @param repoPath Path to repository root
     * @param config Polishing configuration
     * @return PatchBundle with proposed changes
     * @throws IOException if file operations fail
     */
    public PatchBundle polishRepository(Path repoPath, PolishingConfig config) throws IOException {
        log.info("Delegating polish process to Embabel agent for: {}", repoPath);

        // Create polishing context
        ReadmePolisherAgent.PolishingContext context =
            new ReadmePolisherAgent.PolishingContext(repoPath, config);

        // Execute agent goal - uses GOAP planning
        return agent.polishReadmeGoal(context);
    }

    /**
     * Polishes content by applying all transformations.
     */
    private String polishContent(
        String content,
        RepoSnapshot snapshot,
        PolishingConfig config,
        List<PolishingFinding> findings,
        Path file
    ) {
        String result = content;

        // Fix code fences
        result = rewriter.fixCodeFences(result, config.defaultCodeLanguage());

        // Normalize headings
        result = rewriter.normalizeHeadings(result);

        // Remove trailing whitespace
        result = rewriter.removeTrailingWhitespace(result);

        // Add test section if missing
        if (repoScanner.needsTestSection(result) && snapshot.build() != BuildSystem.OTHER) {
            result = addTestSection(result, snapshot.build());
            findings.add(PolishingFinding.forLine(
                "added-test-section",
                "Added 'How to Run Tests' section",
                Severity.INFO,
                file,
                0
            ));
        }

        // Add TOC if needed
        if (config.addTocThresholdHeadings() > 0) {
            int headingCount = linter.countHeadings(result);
            if (headingCount >= config.addTocThresholdHeadings() && !tocTool.hasToc(result)) {
                String toc = tocTool.generateToc(result);
                result = tocTool.insertToc(result, toc);
                findings.add(PolishingFinding.forLine(
                    "added-toc",
                    "Added table of contents",
                    Severity.INFO,
                    file,
                    0
                ));
            }
        }

        // Add badges if enabled
        if (config.badgesEnabled() && !badgeTool.hasBadges(result)) {
            String badges = badgeTool.generateBadgeSection(
                snapshot.root().getFileName().toString(),
                snapshot.build(),
                config.jdkVersion(),
                snapshot.root()
            );
            result = badges + result;
        }

        return result;
    }

    /**
     * Adds a test section to the README.
     */
    private String addTestSection(String content, BuildSystem buildSystem) {
        String testSection = repoScanner.generateTestCommandSnippet(buildSystem);

        // Try to insert after "Installation" or "Usage" section
        String[] sections = {"## Installation", "## Usage", "## Getting Started"};
        for (String section : sections) {
            if (content.contains(section)) {
                int idx = content.indexOf(section);
                int nextSection = content.indexOf("\n## ", idx + section.length());
                if (nextSection > 0) {
                    return content.substring(0, nextSection) + "\n" + testSection + "\n" + content.substring(nextSection);
                }
            }
        }

        // Append at end
        return content + "\n\n" + testSection;
    }

    /**
     * Finds the README file in the snapshot.
     */
    private Path findReadme(RepoSnapshot snapshot) {
        return snapshot.markdownFiles().stream()
            .filter(p -> p.getFileName().toString().equalsIgnoreCase("README.md"))
            .findFirst()
            .orElse(null);
    }

    /**
     * Builds the improvements summary.
     */
    private ReadmeImprovements buildImprovements(String original, String polished, List<PolishingFinding> findings) {
        boolean addedTestSection = findings.stream()
            .anyMatch(f -> f.id().equals("added-test-section"));

        boolean fixedCodeBlocks = !original.contains("```bash") && polished.contains("```bash") ||
                                  !original.contains("```java") && polished.contains("```java");

        boolean normalizedHeadings = findings.stream()
            .anyMatch(f -> f.id().equals("heading-skip-level"));

        boolean addedToc = findings.stream()
            .anyMatch(f -> f.id().equals("added-toc"));

        boolean enhancedVisuals = findings.stream()
            .anyMatch(f -> f.id().equals("enhanced-visuals"));

        return new ReadmeImprovements(
            addedTestSection,
            fixedCodeBlocks,
            normalizedHeadings,
            addedToc,
            enhancedVisuals,
            findings
        );
    }
}
