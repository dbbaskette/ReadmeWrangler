package com.baskettecase.readmewrangler.agent;

import com.baskettecase.readmewrangler.domain.*;
import com.baskettecase.readmewrangler.service.PolishingConfig;
import com.baskettecase.readmewrangler.tool.*;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Embabel agent for polishing repository README files.
 * Uses Goal-Oriented Action Planning (GOAP) to achieve documentation improvements.
 */
@Component
@Agent(description = "Polish repository README and documentation files")
public class ReadmePolisherAgent {

    private static final Logger log = LoggerFactory.getLogger(ReadmePolisherAgent.class);

    private final RepoScannerTool repoScanner;
    private final MarkdownLinterTool linter;
    private final TextRewriteTool rewriter;
    private final TocTool tocTool;
    private final BadgeTool badgeTool;
    private final PatchBuilderTool patchBuilder;
    private final VisualEnhancementTool visualEnhancer;
    private final DocConsolidationTool docConsolidation;

    public ReadmePolisherAgent(
        RepoScannerTool repoScanner,
        MarkdownLinterTool linter,
        TextRewriteTool rewriter,
        TocTool tocTool,
        BadgeTool badgeTool,
        PatchBuilderTool patchBuilder,
        VisualEnhancementTool visualEnhancer,
        DocConsolidationTool docConsolidation
    ) {
        this.repoScanner = repoScanner;
        this.linter = linter;
        this.rewriter = rewriter;
        this.tocTool = tocTool;
        this.badgeTool = badgeTool;
        this.patchBuilder = patchBuilder;
        this.visualEnhancer = visualEnhancer;
        this.docConsolidation = docConsolidation;
    }

    /**
     * Main goal: Polish the README and generate a patch bundle.
     */
    @Action
    @AchievesGoal(description = "Polish README and create patch for approval")
    public PatchBundle polishReadmeGoal(PolishingContext context) throws IOException {
        log.info("Starting README polishing goal for: {}", context.repoPath());

        // Execute sub-goals in sequence
        RepoSnapshot snapshot = scanRepositoryGoal(context);
        context = context.withSnapshot(snapshot);

        // Consolidate documentation files if needed
        if (shouldConsolidateDocs(context.repoPath())) {
            consolidateDocsGoal(context.repoPath());
        }

        Path readmeFile = findReadme(snapshot);
        if (readmeFile == null) {
            log.warn("No README.md found in repository");
            return new PatchBundle("", ReadmeImprovements.empty());
        }

        String original = Files.readString(readmeFile);
        List<PolishingFinding> findings = lintMarkdownGoal(readmeFile, original);

        String polished = rewriteMarkdownGoal(original, snapshot, context.config(), findings, readmeFile);

        ReadmeImprovements improvements = buildImprovements(original, polished, findings);
        String diff = patchBuilder.createUnifiedDiff(readmeFile, original, polished);

        PatchBundle bundle = new PatchBundle(diff, improvements);
        log.info("Polishing complete: {}", bundle.getSummaryLine());

        return bundle;
    }

    /**
     * Sub-goal: Scan repository for metadata.
     */
    @Action(description = "Scan repository to detect build system and files")
    public RepoSnapshot scanRepositoryGoal(PolishingContext context) throws IOException {
        log.info("Scanning repository: {}", context.repoPath());
        return repoScanner.scanRepository(context.repoPath());
    }

    /**
     * Sub-goal: Lint markdown content.
     */
    @Action(description = "Lint markdown for code fences and heading issues")
    public List<PolishingFinding> lintMarkdownGoal(Path file, String content) {
        log.info("Linting markdown file: {}", file);
        return linter.lint(file, content);
    }

    /**
     * Sub-goal: Rewrite and improve markdown content.
     */
    @Action(description = "Rewrite markdown with fixes and improvements")
    public String rewriteMarkdownGoal(
        String content,
        RepoSnapshot snapshot,
        PolishingConfig config,
        List<PolishingFinding> findings,
        Path file
    ) {
        log.info("Rewriting markdown with improvements");

        String result = content;

        // Fix code fences
        result = rewriter.fixCodeFences(result, config.defaultCodeLanguage());

        // Normalize headings
        result = rewriter.normalizeHeadings(result);

        // Remove trailing whitespace
        result = rewriter.removeTrailingWhitespace(result);

        // Add test section if needed
        if (shouldAddTestSection(result, snapshot.build())) {
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
        if (shouldAddToc(result, config.addTocThresholdHeadings())) {
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

        // Add visual enhancements if needed
        if (shouldEnhanceVisuals(result)) {
            result = enhanceVisualsGoal(result);
            findings.add(PolishingFinding.forLine(
                "enhanced-visuals",
                "Added icons and visual formatting to headings",
                Severity.INFO,
                file,
                0
            ));
        }

        return result;
    }

    /**
     * Condition: Should add test section?
     */
    @Condition
    public boolean shouldAddTestSection(String content, BuildSystem buildSystem) {
        return repoScanner.needsTestSection(content) && buildSystem != BuildSystem.OTHER;
    }

    /**
     * Condition: Should add table of contents?
     */
    @Condition
    public boolean shouldAddToc(String content, int threshold) {
        if (threshold <= 0) return false;
        int headingCount = linter.countHeadings(content);
        return headingCount >= threshold && !tocTool.hasToc(content);
    }

    /**
     * Condition: Should enhance visuals with icons and formatting?
     */
    @Condition
    public boolean shouldEnhanceVisuals(String content) {
        return visualEnhancer.needsVisualEnhancement(content);
    }

    /**
     * Action: Add test section to content.
     */
    @Action(description = "Add test section with appropriate command")
    public String addTestSection(String content, BuildSystem buildSystem) {
        String testSection = repoScanner.generateTestCommandSnippet(buildSystem);

        // Try to insert after common sections
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
     * Action: Enhance visual appeal with icons and formatting.
     */
    @Action(description = "Add icons to headings and enhance visual hierarchy")
    public String enhanceVisualsGoal(String content) {
        log.info("Enhancing visual appeal with icons and formatting");

        // Add icons to headings
        String result = visualEnhancer.addIconsToHeadings(content);

        // Enhance visual hierarchy
        result = visualEnhancer.enhanceVisualHierarchy(result);

        return result;
    }

    /**
     * Action: Consolidate documentation files into DEVELOPMENT.md.
     */
    @Action(description = "Consolidate multiple documentation files into DEVELOPMENT.md")
    public void consolidateDocsGoal(Path repoPath) throws IOException {
        log.info("Consolidating documentation files into DEVELOPMENT.md");

        DocConsolidationTool.ConsolidationResult result = docConsolidation.consolidateDocumentation(repoPath);

        if (!result.hasConsolidation()) {
            log.info("No documentation files to consolidate");
            return;
        }

        // Write DEVELOPMENT.md
        Path developmentFile = repoPath.resolve("DEVELOPMENT.md");
        Files.writeString(developmentFile, result.consolidatedContent());
        log.info("Created DEVELOPMENT.md with consolidated documentation");

        // Remove old files
        for (Path oldFile : result.filesToRemove()) {
            try {
                Files.delete(oldFile);
                log.info("Removed consolidated file: {}", oldFile.getFileName());
            } catch (IOException e) {
                log.warn("Failed to remove file {}: {}", oldFile, e.getMessage());
            }
        }

        log.info("Documentation consolidation complete: {} files merged", result.filesToRemove().size());
    }

    /**
     * Condition: Should consolidate documentation files?
     */
    @Condition
    public boolean shouldConsolidateDocs(Path repoPath) {
        try {
            // Check if there are multiple documentation files (excluding README and CLAUDE.md)
            long docCount = Files.list(repoPath)
                .filter(p -> p.getFileName().toString().endsWith(".md"))
                .filter(p -> !p.getFileName().toString().equals("README.md"))
                .filter(p -> !p.getFileName().toString().equals("CLAUDE.md"))
                .filter(p -> !p.getFileName().toString().equals("DEVELOPMENT.md"))
                .count();

            return docCount > 2; // Consolidate if more than 2 additional docs
        } catch (IOException e) {
            log.warn("Failed to check documentation files: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Helper: Find README file in snapshot.
     */
    private Path findReadme(RepoSnapshot snapshot) {
        return snapshot.markdownFiles().stream()
            .filter(p -> p.getFileName().toString().equalsIgnoreCase("README.md"))
            .findFirst()
            .orElse(null);
    }

    /**
     * Helper: Build improvements summary.
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

    /**
     * Context object for polishing operations.
     */
    public record PolishingContext(
        Path repoPath,
        PolishingConfig config,
        RepoSnapshot snapshot
    ) {
        public PolishingContext(Path repoPath, PolishingConfig config) {
            this(repoPath, config, null);
        }

        public PolishingContext withSnapshot(RepoSnapshot snapshot) {
            return new PolishingContext(repoPath, config, snapshot);
        }
    }
}
