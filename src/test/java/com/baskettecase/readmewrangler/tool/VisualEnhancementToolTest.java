package com.baskettecase.readmewrangler.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for VisualEnhancementTool.
 */
class VisualEnhancementToolTest {

    private VisualEnhancementTool tool;

    @BeforeEach
    void setUp() {
        tool = new VisualEnhancementTool();
    }

    @Test
    void shouldAddIconsToHeadingsWithoutIcons() {
        String content = """
            # My Project

            ## Features

            Some features here.

            ## Installation

            Install instructions.
            """;

        String result = tool.addIconsToHeadings(content);

        assertTrue(result.contains("🎯"), "Should add features icon");
        assertTrue(result.contains("📦"), "Should add installation icon");
    }

    @Test
    void shouldNotAddIconsToHeadingsWithExistingIcons() {
        String content = """
            # 🚀 My Project

            ## 🎯 Features

            Some features here.
            """;

        String result = tool.addIconsToHeadings(content);

        // Should not add more icons when they already exist
        assertTrue(result.contains("🚀 My Project"), "Should preserve existing rocket icon");
        assertTrue(result.contains("🎯 Features"), "Should preserve existing target icon");

        // Verify no duplicate heading structure
        assertEquals(content.trim(), result.trim(), "Should not modify content with existing icons");
    }

    @Test
    void shouldEnhanceVisualHierarchy() {
        String content = """
            # Title

            ## Section One

            Content here.

            ## Section Two

            More content.
            """;

        String result = tool.enhanceVisualHierarchy(content);

        assertTrue(result.contains("---"), "Should add section dividers");
    }

    @Test
    void shouldEmphasizeKeyPhrases() {
        String content = """
            # Title

            Important: This is critical.

            Note: Please read this.

            Warning: Be careful.
            """;

        String result = tool.enhanceVisualHierarchy(content);

        assertTrue(result.contains("⚠️ **Important:**"), "Should emphasize Important");
        assertTrue(result.contains("📝 **Note:**"), "Should emphasize Note");
        assertTrue(result.contains("⚠️ **Warning:**"), "Should emphasize Warning");
    }

    @Test
    void shouldDetectNeedForVisualEnhancement() {
        String contentWithoutIcons = """
            # Title

            ## Features

            ## Installation

            ## Usage
            """;

        assertTrue(tool.needsVisualEnhancement(contentWithoutIcons),
            "Should detect content needs enhancement when few icons present");

        String contentWithIcons = """
            # 🚀 Title

            ## 🎯 Features

            ## 📦 Installation

            ## 💻 Usage
            """;

        assertFalse(tool.needsVisualEnhancement(contentWithIcons),
            "Should not need enhancement when icons already present");
    }

    @Test
    void shouldGetVisualStats() {
        String content = """
            # Title

            ## 🎯 Features

            ---

            ## Installation

            Important: Read this.
            """;

        VisualEnhancementTool.VisualStats stats = tool.getVisualStats(content);

        assertEquals(3, stats.totalHeadings(), "Should count all headings");
        assertEquals(1, stats.headingsWithIcons(), "Should count headings with icons");
        assertEquals(1, stats.dividers(), "Should count dividers");
    }

    @Test
    void shouldCalculateIconPercentage() {
        String content = """
            # Title

            ## 🎯 Features

            ## Installation

            ## Usage

            ## Testing
            """;

        VisualEnhancementTool.VisualStats stats = tool.getVisualStats(content);

        assertEquals(5, stats.totalHeadings());
        assertEquals(1, stats.headingsWithIcons());
        assertEquals(20.0, stats.iconPercentage(), 0.01, "Should calculate 20% icon coverage");
    }
}
