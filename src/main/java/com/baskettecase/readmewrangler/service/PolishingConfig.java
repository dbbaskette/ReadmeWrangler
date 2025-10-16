package com.baskettecase.readmewrangler.service;

/**
 * Configuration for the polishing process.
 *
 * @param addTocThresholdHeadings Minimum heading count to trigger TOC generation (0 to disable)
 * @param headingStyle Heading style preference ("atx" for hashes, "setext" for underlines)
 * @param defaultCodeLanguage Default language for unlabeled code fences
 * @param badgesEnabled Whether to add/update badges
 * @param jdkVersion JDK version for badge
 * @param maxChangesPerFile Maximum number of line changes per file
 * @param requireMcpApproval Whether MCP approval is required before applying patches
 */
public record PolishingConfig(
    int addTocThresholdHeadings,
    String headingStyle,
    String defaultCodeLanguage,
    boolean badgesEnabled,
    String jdkVersion,
    int maxChangesPerFile,
    boolean requireMcpApproval
) {
    /**
     * Creates default configuration.
     */
    public static PolishingConfig defaults() {
        return new PolishingConfig(
            4,              // addTocThresholdHeadings
            "atx",          // headingStyle
            "bash",         // defaultCodeLanguage
            true,           // badgesEnabled
            "21",           // jdkVersion
            300,            // maxChangesPerFile
            true            // requireMcpApproval
        );
    }
}
