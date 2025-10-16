package com.baskettecase.readmewrangler.tool;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates and manages table of contents for markdown documents.
 */
@Component
public class TocTool {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.*)$", Pattern.MULTILINE);
    private static final Pattern TOC_MARKER = Pattern.compile("(?i)<!--\\s*toc\\s*-->|##\\s*Table of Contents");

    /**
     * Generates a table of contents from markdown content.
     *
     * @param content Markdown content
     * @return Generated TOC as markdown
     */
    public String generateToc(String content) {
        List<TocEntry> entries = new ArrayList<>();
        Matcher matcher = HEADING_PATTERN.matcher(content);

        while (matcher.find()) {
            int level = matcher.group(1).length();
            String title = matcher.group(2).trim();

            // Skip the TOC heading itself
            if (!title.equalsIgnoreCase("Table of Contents")) {
                String anchor = createAnchor(title);
                entries.add(new TocEntry(level, title, anchor));
            }
        }

        return buildTocMarkdown(entries);
    }

    /**
     * Builds the TOC markdown from entries.
     */
    private String buildTocMarkdown(List<TocEntry> entries) {
        if (entries.isEmpty()) {
            return "";
        }

        StringBuilder toc = new StringBuilder("## Table of Contents\n\n");

        for (TocEntry entry : entries) {
            int indent = (entry.level - 1) * 2;
            String spaces = " ".repeat(indent);

            toc.append(spaces)
               .append("- [")
               .append(entry.title)
               .append("](#")
               .append(entry.anchor)
               .append(")\n");
        }

        return toc.toString();
    }

    /**
     * Creates a GitHub-style anchor from a heading.
     */
    private String createAnchor(String heading) {
        return heading.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }

    /**
     * Checks if content already has a TOC.
     */
    public boolean hasToc(String content) {
        return TOC_MARKER.matcher(content).find();
    }

    /**
     * Inserts TOC after the first heading or at the beginning.
     */
    public String insertToc(String content, String toc) {
        String[] lines = content.split("\n", -1);
        StringBuilder result = new StringBuilder();

        boolean inserted = false;
        for (int i = 0; i < lines.length; i++) {
            result.append(lines[i]).append("\n");

            // Insert after first heading
            if (!inserted && lines[i].startsWith("#")) {
                result.append("\n").append(toc).append("\n");
                inserted = true;
            }
        }

        // If no heading found, prepend
        if (!inserted) {
            return toc + "\n\n" + content;
        }

        return result.toString().trim();
    }

    /**
     * Represents a TOC entry.
     */
    private record TocEntry(int level, String title, String anchor) {
    }
}
