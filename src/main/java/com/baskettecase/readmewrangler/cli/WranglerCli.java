package com.baskettecase.readmewrangler.cli;

import com.baskettecase.readmewrangler.domain.PatchBundle;
import com.baskettecase.readmewrangler.service.PolishingConfig;
import com.baskettecase.readmewrangler.service.PolishingService;
import com.baskettecase.readmewrangler.tool.PatchBuilderTool;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

/**
 * Command-line interface for README Wrangler.
 * Provides polish and apply commands.
 */
@Component
@Command(
    name = "readme-wrangler",
    description = "Polish repository documentation",
    subcommands = {
        WranglerCli.PolishCommand.class,
        WranglerCli.ApplyCommand.class
    }
)
public class WranglerCli implements Callable<Integer> {

    @Override
    public Integer call() {
        CommandLine.usage(this, System.out);
        return 0;
    }

    /**
     * Polish command - analyzes and generates patches.
     */
    @Command(name = "polish", description = "Polish repository documentation and generate a patch")
    static class PolishCommand implements Callable<Integer> {

        @Parameters(index = "0", description = "Path to repository root", defaultValue = ".")
        private String repoPath;

        @Option(names = {"--write-patch"}, description = "Output file for patch")
        private String patchFile;

        @Option(names = {"--toc-threshold"}, description = "Minimum headings to add TOC", defaultValue = "4")
        private int tocThreshold;

        @Option(names = {"--badges"}, description = "Enable badges", defaultValue = "true")
        private boolean badges;

        @Option(names = {"--jdk"}, description = "JDK version for badge", defaultValue = "21")
        private String jdkVersion;

        private final PolishingService polishingService;
        private final PatchBuilderTool patchBuilder;

        PolishCommand(PolishingService polishingService, PatchBuilderTool patchBuilder) {
            this.polishingService = polishingService;
            this.patchBuilder = patchBuilder;
        }

        @Override
        public Integer call() {
            try {
                System.out.println("🔧 Polishing repository at: " + repoPath);

                Path repo = Paths.get(repoPath);
                PolishingConfig config = new PolishingConfig(
                    tocThreshold,
                    "atx",
                    "bash",
                    badges,
                    jdkVersion,
                    300,
                    false
                );

                PatchBundle bundle = polishingService.polishRepository(repo, config);

                if (bundle.hasChanges()) {
                    System.out.println("\n✨ " + bundle.getSummaryLine());
                    System.out.println("\n" + bundle.unifiedDiff());

                    if (patchFile != null) {
                        Path outPath = Paths.get(patchFile);
                        patchBuilder.writePatchToFile(bundle.unifiedDiff(), outPath);
                        System.out.println("\n📄 Patch written to: " + outPath);
                    }

                    return 0;
                } else {
                    System.out.println("\n✅ No changes needed - documentation looks good!");
                    return 0;
                }

            } catch (IOException e) {
                System.err.println("❌ Error: " + e.getMessage());
                return 1;
            }
        }
    }

    /**
     * Apply command - applies a patch file.
     */
    @Command(name = "apply", description = "Apply a patch to documentation files")
    static class ApplyCommand implements Callable<Integer> {

        @Option(names = {"--patch"}, required = true, description = "Patch file to apply")
        private String patchFile;

        @Option(names = {"--branch"}, description = "Git branch to create (optional)")
        private String branch;

        @Override
        public Integer call() {
            try {
                Path patch = Paths.get(patchFile);

                if (!Files.exists(patch)) {
                    System.err.println("❌ Patch file not found: " + patchFile);
                    return 1;
                }

                System.out.println("⚠️  Apply functionality requires manual review and approval.");
                System.out.println("📄 Patch file: " + patchFile);

                if (branch != null) {
                    System.out.println("🌿 Suggested branch: " + branch);
                    System.out.println("\nTo apply:");
                    System.out.println("  git checkout -b " + branch);
                    System.out.println("  git apply " + patchFile);
                }

                return 0;

            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                return 1;
            }
        }
    }
}
