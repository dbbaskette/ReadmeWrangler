package com.baskettecase.readmewrangler.tool;

import com.baskettecase.readmewrangler.domain.BuildSystem;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates and manages badges for README files.
 * Creates badges for build status, license, JDK version, etc.
 */
@Component
public class BadgeTool {

    /**
     * Generates a badge section for a README.
     *
     * @param repoName Repository name
     * @param buildSystem Build system
     * @param jdkVersion JDK version
     * @param repoPath Repository path for license detection
     * @return Markdown badge section
     */
    public String generateBadgeSection(String repoName, BuildSystem buildSystem, String jdkVersion, Path repoPath) {
        List<String> badges = new ArrayList<>();

        // Build badge
        if (buildSystem == BuildSystem.MAVEN) {
            badges.add("![Maven Build](https://img.shields.io/badge/build-maven-blue)");
        } else if (buildSystem == BuildSystem.GRADLE) {
            badges.add("![Gradle Build](https://img.shields.io/badge/build-gradle-blue)");
        }

        // JDK badge
        if (jdkVersion != null && !jdkVersion.isBlank()) {
            badges.add(String.format("![JDK %s](https://img.shields.io/badge/JDK-%s-orange)", jdkVersion, jdkVersion));
        }

        // License badge
        String license = detectLicense(repoPath);
        if (license != null) {
            badges.add(String.format("![License](https://img.shields.io/badge/license-%s-green)", license));
        }

        if (badges.isEmpty()) {
            return "";
        }

        return String.join(" ", badges) + "\n\n";
    }

    /**
     * Detects the license type from LICENSE file.
     */
    private String detectLicense(Path repoPath) {
        if (repoPath == null) {
            return null;
        }

        Path licenseFile = null;
        for (String name : List.of("LICENSE", "LICENSE.txt", "LICENSE.md", "COPYING")) {
            Path candidate = repoPath.resolve(name);
            if (Files.exists(candidate)) {
                licenseFile = candidate;
                break;
            }
        }

        if (licenseFile == null) {
            return null;
        }

        try {
            String content = Files.readString(licenseFile);
            String lower = content.toLowerCase();

            if (lower.contains("apache license")) {
                return "Apache-2.0";
            } else if (lower.contains("mit license")) {
                return "MIT";
            } else if (lower.contains("gnu general public license v3")) {
                return "GPL-3.0";
            } else if (lower.contains("gnu lesser general public license")) {
                return "LGPL";
            } else if (lower.contains("bsd")) {
                return "BSD";
            }
        } catch (Exception e) {
            // Ignore errors
        }

        return "Custom";
    }

    /**
     * Checks if content already has badges.
     */
    public boolean hasBadges(String content) {
        return content.contains("![") && content.contains("shields.io");
    }
}
