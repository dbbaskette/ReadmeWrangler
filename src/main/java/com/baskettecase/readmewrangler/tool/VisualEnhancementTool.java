package com.baskettecase.readmewrangler.tool;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhances README visual appeal with icons, emojis, and varied text sizes.
 * Adds visual hierarchy to improve readability.
 */
@Component
public class VisualEnhancementTool {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)$", Pattern.MULTILINE);

    // Common section icons mapping
    private static final Map<String, String> SECTION_ICONS = new HashMap<>();

    static {
        // Features & Capabilities
        SECTION_ICONS.put("features", "🎯");
        SECTION_ICONS.put("capabilities", "⚡");
        SECTION_ICONS.put("highlights", "✨");

        // Getting Started & Installation
        SECTION_ICONS.put("quick start", "🚀");
        SECTION_ICONS.put("getting started", "🏁");
        SECTION_ICONS.put("installation", "📦");
        SECTION_ICONS.put("setup", "⚙️");

        // Usage & Examples
        SECTION_ICONS.put("usage", "💻");
        SECTION_ICONS.put("examples", "📚");
        SECTION_ICONS.put("tutorial", "📖");
        SECTION_ICONS.put("guide", "📝");

        // Configuration & Settings
        SECTION_ICONS.put("configuration", "⚙️");
        SECTION_ICONS.put("settings", "🔧");
        SECTION_ICONS.put("options", "🎛️");

        // Testing & Development
        SECTION_ICONS.put("testing", "🧪");
        SECTION_ICONS.put("tests", "✅");
        SECTION_ICONS.put("development", "🛠️");
        SECTION_ICONS.put("building", "🔨");

        // Architecture & Design
        SECTION_ICONS.put("architecture", "🏗️");
        SECTION_ICONS.put("design", "🎨");
        SECTION_ICONS.put("structure", "📐");

        // Documentation & Help
        SECTION_ICONS.put("documentation", "📄");
        SECTION_ICONS.put("api", "🔌");
        SECTION_ICONS.put("reference", "📚");

        // Contributing & Community
        SECTION_ICONS.put("contributing", "🤝");
        SECTION_ICONS.put("community", "👥");
        SECTION_ICONS.put("support", "💬");

        // Deployment & Operations
        SECTION_ICONS.put("deployment", "🚢");
        SECTION_ICONS.put("production", "🏭");
        SECTION_ICONS.put("monitoring", "📊");

        // Security & License
        SECTION_ICONS.put("security", "🔒");
        SECTION_ICONS.put("license", "📄");
        SECTION_ICONS.put("legal", "⚖️");

        // Performance & Optimization
        SECTION_ICONS.put("performance", "⚡");
        SECTION_ICONS.put("optimization", "🚄");

        // Troubleshooting & FAQ
        SECTION_ICONS.put("troubleshooting", "🐛");
        SECTION_ICONS.put("faq", "❓");
        SECTION_ICONS.put("known issues", "⚠️");

        // Changelog & Roadmap
        SECTION_ICONS.put("changelog", "📋");
        SECTION_ICONS.put("roadmap", "🗺️");
        SECTION_ICONS.put("releases", "🎉");

        // Table of Contents
        SECTION_ICONS.put("table of contents", "📋");
        SECTION_ICONS.put("contents", "📑");
    }

    /**
     * Enhances README with icons on headings.
     *
     * @param content Original README content
     * @return Enhanced content with icons
     */
    public String addIconsToHeadings(String content) {
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n", -1);

        for (String line : lines) {
            if (line.trim().startsWith("#")) {
                result.append(addIconToHeading(line));
            } else {
                result.append(line);
            }
            result.append("\n");
        }

        return result.toString().trim();
    }

    /**
     * Adds an appropriate icon to a heading if it doesn't already have one.
     */
    private String addIconToHeading(String heading) {
        Matcher matcher = HEADING_PATTERN.matcher(heading);

        if (!matcher.matches()) {
            return heading;
        }

        String hashes = matcher.group(1);
        String title = matcher.group(2).trim();

        // Skip if already has an emoji/icon
        if (hasEmoji(title)) {
            return heading;
        }

        // Find matching icon
        String icon = findIconForTitle(title);

        if (icon != null) {
            return hashes + " " + icon + " " + title;
        }

        return heading;
    }

    /**
     * Finds an appropriate icon for a heading title.
     */
    private String findIconForTitle(String title) {
        String lowerTitle = title.toLowerCase();

        // Direct match
        for (Map.Entry<String, String> entry : SECTION_ICONS.entrySet()) {
            if (lowerTitle.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Checks if text already contains an emoji/icon.
     */
    private boolean hasEmoji(String text) {
        // Check for emoji unicode ranges - using codepoint ranges
        for (int i = 0; i < text.length(); i++) {
            int codepoint = text.codePointAt(i);
            // Check various emoji ranges
            if ((codepoint >= 0x1F300 && codepoint <= 0x1F9FF) ||  // Emoji & Pictographs
                (codepoint >= 0x2600 && codepoint <= 0x27BF) ||    // Misc symbols
                (codepoint >= 0x1F600 && codepoint <= 0x1F64F)) {  // Emoticons
                return true;
            }
        }
        return false;
    }

    /**
     * Enhances section with visual formatting (bold, italic, varied sizes).
     *
     * @param content Original content
     * @return Enhanced content with better visual hierarchy
     */
    public String enhanceVisualHierarchy(String content) {
        // Add horizontal rules between major sections (H2 level)
        String result = addSectionDividers(content);

        // Emphasize key phrases
        result = emphasizeKeyPhrases(result);

        return result;
    }

    /**
     * Adds horizontal rules between major sections for visual separation.
     */
    private String addSectionDividers(String content) {
        StringBuilder result = new StringBuilder();
        String[] lines = content.split("\n", -1);

        boolean lastWasH1 = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Add divider before H2 headings (but not right after H1 or at the start)
            if (line.startsWith("## ") && i > 0 && !lastWasH1) {
                // Check if there isn't already a divider
                if (i > 0 && !lines[i-1].trim().equals("---")) {
                    result.append("\n---\n\n");
                }
            }

            result.append(lines[i]).append("\n");
            lastWasH1 = line.startsWith("# ") && !line.startsWith("## ");
        }

        return result.toString().trim();
    }

    /**
     * Emphasizes key phrases like "Important", "Note", "Warning", etc.
     */
    private String emphasizeKeyPhrases(String content) {
        // Bold important keywords at start of lines
        content = content.replaceAll("(?m)^(Important:|Note:|Warning:|Tip:|Info:)", "**$1**");

        // Add emoji to common callouts
        content = content.replaceAll("(?m)^\\*\\*Important:\\*\\*", "⚠️ **Important:**");
        content = content.replaceAll("(?m)^\\*\\*Note:\\*\\*", "📝 **Note:**");
        content = content.replaceAll("(?m)^\\*\\*Warning:\\*\\*", "⚠️ **Warning:**");
        content = content.replaceAll("(?m)^\\*\\*Tip:\\*\\*", "💡 **Tip:**");
        content = content.replaceAll("(?m)^\\*\\*Info:\\*\\*", "ℹ️ **Info:**");

        return content;
    }

    /**
     * Checks if content needs visual enhancements.
     */
    public boolean needsVisualEnhancement(String content) {
        // Check if headings lack icons (excluding H1)
        int h2OrLowerCount = 0;
        int h2WithIconCount = 0;

        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.trim().matches("^##+ .*")) { // H2 or lower
                h2OrLowerCount++;
                if (hasEmoji(line)) {
                    h2WithIconCount++;
                }
            }
        }

        // If less than 30% of headings have icons, enhance
        if (h2OrLowerCount > 0) {
            double iconPercentage = (double) h2WithIconCount / h2OrLowerCount;
            return iconPercentage < 0.3;
        }

        return false;
    }

    /**
     * Gets statistics about visual elements in content.
     */
    public VisualStats getVisualStats(String content) {
        int totalHeadings = 0;
        int headingsWithIcons = 0;
        int dividers = 0;
        int emphasisCount = 0;

        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("#")) {
                totalHeadings++;
                if (hasEmoji(trimmed)) {
                    headingsWithIcons++;
                }
            }

            if (trimmed.equals("---")) {
                dividers++;
            }

            if (trimmed.matches(".*\\*\\*(Important|Note|Warning|Tip|Info):.*")) {
                emphasisCount++;
            }
        }

        return new VisualStats(totalHeadings, headingsWithIcons, dividers, emphasisCount);
    }

    /**
     * Statistics about visual elements in a README.
     */
    public record VisualStats(
        int totalHeadings,
        int headingsWithIcons,
        int dividers,
        int emphasisElements
    ) {
        public double iconPercentage() {
            return totalHeadings > 0 ? (double) headingsWithIcons / totalHeadings * 100 : 0;
        }
    }
}
