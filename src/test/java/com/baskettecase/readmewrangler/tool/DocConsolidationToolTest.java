package com.baskettecase.readmewrangler.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DocConsolidationTool.
 */
class DocConsolidationToolTest {

    private DocConsolidationTool tool;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        tool = new DocConsolidationTool();
    }

    @Test
    void testConsolidateMultipleFiles() throws IOException {
        // Create multiple documentation files
        Files.writeString(tempDir.resolve("GETTING_STARTED.md"),
            "# Getting Started\n\nThis is the getting started guide.\n\n## Installation\n\nRun `mvn install`.");

        Files.writeString(tempDir.resolve("SETUP.md"),
            "# Setup\n\nSetup instructions here.\n\n## Prerequisites\n\n- Java 21\n- Maven");

        Files.writeString(tempDir.resolve("AGENTS.md"),
            "# Agents\n\nInformation about agents.\n\n## Embabel\n\nEmbabel is used for GOAP.");

        // Create README and CLAUDE.md which should be excluded
        Files.writeString(tempDir.resolve("README.md"), "# Main README");
        Files.writeString(tempDir.resolve("CLAUDE.md"), "# Claude Config");

        // Execute consolidation
        DocConsolidationTool.ConsolidationResult result = tool.consolidateDocumentation(tempDir);

        // Verify result
        assertTrue(result.hasConsolidation());
        assertEquals(3, result.filesToRemove().size());

        String consolidated = result.consolidatedContent();
        assertNotNull(consolidated);
        assertTrue(consolidated.contains("# Development Guide"));
        assertTrue(consolidated.contains("## Table of Contents"));
        assertTrue(consolidated.contains("## Getting Started"));
        assertTrue(consolidated.contains("## Setup"));
        assertTrue(consolidated.contains("## Agents"));

        // Verify original top-level headings are removed (not in content sections)
        // Check that "# Getting Started" appears as "## Getting Started" instead
        int h1Count = consolidated.split("# Getting Started", -1).length - 1;
        int h2Count = consolidated.split("## Getting Started", -1).length - 1;
        assertTrue(h2Count > 0, "Should have ## Getting Started");
        assertEquals(0, h1Count - h2Count, "Should not have standalone # Getting Started");

        // Verify content is present
        assertTrue(consolidated.contains("getting started guide"));
        assertTrue(consolidated.contains("Java 21"));
        assertTrue(consolidated.contains("Embabel is used for GOAP"));
    }

    @Test
    void testExcludesClaudeAndReadme() throws IOException {
        Files.writeString(tempDir.resolve("README.md"), "# README");
        Files.writeString(tempDir.resolve("CLAUDE.md"), "# CLAUDE");
        Files.writeString(tempDir.resolve("SETUP.md"), "# Setup");

        DocConsolidationTool.ConsolidationResult result = tool.consolidateDocumentation(tempDir);

        // Should only consolidate SETUP.md
        assertEquals(1, result.filesToRemove().size());
        assertEquals("SETUP.md", result.filesToRemove().get(0).getFileName().toString());
    }

    @Test
    void testNoFilesToConsolidate() throws IOException {
        Files.writeString(tempDir.resolve("README.md"), "# README");
        Files.writeString(tempDir.resolve("CLAUDE.md"), "# CLAUDE");

        DocConsolidationTool.ConsolidationResult result = tool.consolidateDocumentation(tempDir);

        assertFalse(result.hasConsolidation());
        assertTrue(result.filesToRemove().isEmpty());
        assertEquals("", result.consolidatedContent());
    }

    @Test
    void testFileNameToSectionName() throws IOException {
        Files.writeString(tempDir.resolve("GETTING_STARTED.md"), "# Test");
        Files.writeString(tempDir.resolve("quick-reference.md"), "# Test");
        Files.writeString(tempDir.resolve("MCP_EMBABEL_INTEGRATION.md"), "# Test");

        DocConsolidationTool.ConsolidationResult result = tool.consolidateDocumentation(tempDir);

        String consolidated = result.consolidatedContent();
        assertTrue(consolidated.contains("## Getting Started"));
        assertTrue(consolidated.contains("## Quick Reference"));
        assertTrue(consolidated.contains("## Mcp Embabel Integration"));
    }

    @Test
    void testDeduplication() throws IOException {
        String duplicateContent = "# Doc\n\nSame paragraph.\n\nSame paragraph.\n\nDifferent paragraph.";
        Files.writeString(tempDir.resolve("DOC.md"), duplicateContent);

        DocConsolidationTool.ConsolidationResult result = tool.consolidateDocumentation(tempDir);

        String consolidated = result.consolidatedContent();

        // Should only have one instance of "Same paragraph"
        int firstIndex = consolidated.indexOf("Same paragraph");
        int lastIndex = consolidated.lastIndexOf("Same paragraph");
        assertEquals(firstIndex, lastIndex, "Duplicate paragraphs should be removed");

        assertTrue(consolidated.contains("Different paragraph"));
    }

    @Test
    void testTableOfContentsGeneration() throws IOException {
        Files.writeString(tempDir.resolve("SETUP.md"), "# Setup");
        Files.writeString(tempDir.resolve("USAGE.md"), "# Usage");

        DocConsolidationTool.ConsolidationResult result = tool.consolidateDocumentation(tempDir);

        String consolidated = result.consolidatedContent();
        assertTrue(consolidated.contains("## Table of Contents"));
        assertTrue(consolidated.contains("[Setup](#setup)"));
        assertTrue(consolidated.contains("[Usage](#usage)"));
    }
}
