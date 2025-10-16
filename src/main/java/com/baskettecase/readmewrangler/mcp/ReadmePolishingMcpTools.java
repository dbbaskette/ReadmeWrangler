package com.baskettecase.readmewrangler.mcp;

import com.baskettecase.readmewrangler.domain.PatchBundle;
import com.baskettecase.readmewrangler.domain.RepoSnapshot;
import com.baskettecase.readmewrangler.service.PolishingConfig;
import com.baskettecase.readmewrangler.service.PolishingService;
import com.baskettecase.readmewrangler.tool.PatchBuilderTool;
import com.baskettecase.readmewrangler.tool.RepoScannerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP Tools for README polishing operations.
 * Exposes tools via Model Context Protocol for Claude and other AI clients.
 */
@Component
public class ReadmePolishingMcpTools {

    private static final Logger log = LoggerFactory.getLogger(ReadmePolishingMcpTools.class);

    private final PolishingService polishingService;
    private final RepoScannerTool repoScanner;
    private final PatchBuilderTool patchBuilder;

    // Store patches awaiting approval (in-memory for now)
    private final Map<String, PatchBundle> pendingPatches = new ConcurrentHashMap<>();

    public ReadmePolishingMcpTools(
        PolishingService polishingService,
        RepoScannerTool repoScanner,
        PatchBuilderTool patchBuilder
    ) {
        this.polishingService = polishingService;
        this.repoScanner = repoScanner;
        this.patchBuilder = patchBuilder;
    }

    /**
     * Scans a repository and analyzes its documentation.
     *
     * @param repoPath Absolute path to the repository
     * @return Repository analysis summary
     */
    @McpTool(
        name = "scan_repository",
        description = "Scans a repository to detect build system, markdown files, and test scripts"
    )
    public Map<String, Object> scanRepository(
        @McpToolParam(description = "Absolute path to the repository root", required = true)
        String repoPath
    ) {
        try {
            log.info("MCP Tool: Scanning repository at {}", repoPath);

            Path path = Paths.get(repoPath);
            RepoSnapshot snapshot = repoScanner.scanRepository(path);

            Map<String, Object> result = new HashMap<>();
            result.put("repoPath", repoPath);
            result.put("buildSystem", snapshot.build().name());
            result.put("markdownFilesCount", snapshot.markdownFiles().size());
            result.put("scriptsCount", snapshot.scripts().size());
            result.put("markdownFiles", snapshot.markdownFiles().stream()
                .map(Path::toString)
                .toList());

            return result;

        } catch (IOException e) {
            log.error("Failed to scan repository", e);
            return Map.of("error", "Failed to scan repository: " + e.getMessage());
        }
    }

    /**
     * Polishes a repository's documentation and generates a patch.
     *
     * @param repoPath Absolute path to the repository
     * @param patchId Unique identifier for this patch
     * @return Patch bundle with proposed changes
     */
    @McpTool(
        name = "polish_readme",
        description = "Analyzes and polishes README and documentation files, generating a patch for review"
    )
    public Map<String, Object> polishReadme(
        @McpToolParam(description = "Absolute path to the repository root", required = true)
        String repoPath,
        @McpToolParam(description = "Unique patch identifier for approval workflow", required = false)
        String patchId
    ) {
        try {
            log.info("MCP Tool: Polishing README at {} with patchId={}", repoPath, patchId);

            Path path = Paths.get(repoPath);
            PolishingConfig config = PolishingConfig.defaults();

            PatchBundle bundle = polishingService.polishRepository(path, config);

            // Generate patch ID if not provided
            String actualPatchId = patchId != null ? patchId : generatePatchId(repoPath);

            // Store patch for later approval
            pendingPatches.put(actualPatchId, bundle);

            Map<String, Object> result = new HashMap<>();
            result.put("patchId", actualPatchId);
            result.put("hasChanges", bundle.hasChanges());
            result.put("summary", bundle.getSummaryLine());
            result.put("addedTestSection", bundle.summary().addedTestSection());
            result.put("fixedCodeBlocks", bundle.summary().fixedCodeBlocks());
            result.put("normalizedHeadings", bundle.summary().normalizedHeadings());
            result.put("addedToc", bundle.summary().addedToc());
            result.put("findingsCount", bundle.summary().notes().size());
            result.put("diff", bundle.unifiedDiff());
            result.put("hasConsolidation", bundle.hasConsolidation());
            result.put("consolidationPatch", bundle.consolidationPatch());
            result.put("filesToDelete", bundle.filesToDelete().stream()
                .map(p -> p.getFileName().toString())
                .toList());

            log.info("Generated patch {}: {}", actualPatchId, bundle.getSummaryLine());

            return result;

        } catch (IOException e) {
            log.error("Failed to polish README", e);
            return Map.of("error", "Failed to polish README: " + e.getMessage());
        }
    }

    /**
     * Approves and applies a pending patch to the repository.
     * This is a HITL (Human-In-The-Loop) approval tool.
     *
     * @param patchId Patch identifier from polish_readme
     * @param outputPath Path where to write the patch file
     * @return Application result
     */
    @McpTool(
        name = "approve_patch",
        description = "Approves a pending patch and writes it to a file for manual application (HITL)"
    )
    public Map<String, Object> approvePatch(
        @McpToolParam(description = "Patch identifier to approve", required = true)
        String patchId,
        @McpToolParam(description = "Path where to save the approved patch", required = true)
        String outputPath
    ) {
        try {
            log.info("MCP Tool: Approving patch {} to {}", patchId, outputPath);

            PatchBundle bundle = pendingPatches.get(patchId);
            if (bundle == null) {
                return Map.of("error", "Patch not found: " + patchId);
            }

            if (!bundle.hasChanges()) {
                return Map.of("error", "No changes in patch " + patchId);
            }

            // Write README patch to file
            Path outPath = Paths.get(outputPath);
            StringBuilder combinedPatch = new StringBuilder();
            combinedPatch.append(bundle.unifiedDiff());

            // Add consolidation patch if present
            if (bundle.hasConsolidation()) {
                combinedPatch.append("\n").append(bundle.consolidationPatch());
            }

            patchBuilder.writePatchToFile(combinedPatch.toString(), outPath);

            // Remove from pending
            pendingPatches.remove(patchId);

            Map<String, Object> result = new HashMap<>();
            result.put("patchId", patchId);
            result.put("patchFile", outputPath);
            result.put("summary", bundle.getSummaryLine());
            result.put("hasConsolidation", bundle.hasConsolidation());

            StringBuilder instructions = new StringBuilder();
            instructions.append("Apply with: git apply ").append(outputPath);
            if (bundle.hasConsolidation()) {
                instructions.append("\nThen delete files: ");
                instructions.append(String.join(", ", bundle.filesToDelete().stream()
                    .map(p -> p.getFileName().toString())
                    .toList()));
            }
            result.put("instructions", instructions.toString());

            log.info("Approved and wrote patch {} to {}", patchId, outputPath);

            return result;

        } catch (IOException e) {
            log.error("Failed to write patch", e);
            return Map.of("error", "Failed to write patch: " + e.getMessage());
        }
    }

    /**
     * Lists all pending patches awaiting approval.
     *
     * @return List of pending patch IDs with summaries
     */
    @McpTool(
        name = "list_pending_patches",
        description = "Lists all patches awaiting HITL approval"
    )
    public Map<String, Object> listPendingPatches() {
        log.info("MCP Tool: Listing {} pending patches", pendingPatches.size());

        Map<String, String> patches = new HashMap<>();
        pendingPatches.forEach((id, bundle) ->
            patches.put(id, bundle.getSummaryLine())
        );

        return Map.of(
            "count", pendingPatches.size(),
            "patches", patches
        );
    }

    /**
     * Rejects a pending patch.
     *
     * @param patchId Patch identifier to reject
     * @return Rejection result
     */
    @McpTool(
        name = "reject_patch",
        description = "Rejects a pending patch and removes it from the approval queue"
    )
    public Map<String, Object> rejectPatch(
        @McpToolParam(description = "Patch identifier to reject", required = true)
        String patchId
    ) {
        log.info("MCP Tool: Rejecting patch {}", patchId);

        PatchBundle removed = pendingPatches.remove(patchId);
        if (removed == null) {
            return Map.of("error", "Patch not found: " + patchId);
        }

        return Map.of(
            "patchId", patchId,
            "status", "rejected",
            "message", "Patch has been removed from approval queue"
        );
    }

    /**
     * Generates a test command snippet for a repository.
     *
     * @param repoPath Absolute path to the repository
     * @return Test command snippet
     */
    @McpTool(
        name = "generate_test_command",
        description = "Generates appropriate test command snippet based on detected build system"
    )
    public Map<String, Object> generateTestCommand(
        @McpToolParam(description = "Absolute path to the repository root", required = true)
        String repoPath
    ) {
        try {
            log.info("MCP Tool: Generating test command for {}", repoPath);

            Path path = Paths.get(repoPath);
            RepoSnapshot snapshot = repoScanner.scanRepository(path);

            String testSnippet = repoScanner.generateTestCommandSnippet(snapshot.build());

            return Map.of(
                "buildSystem", snapshot.build().name(),
                "testCommand", testSnippet
            );

        } catch (IOException e) {
            log.error("Failed to generate test command", e);
            return Map.of("error", "Failed to generate test command: " + e.getMessage());
        }
    }

    /**
     * Generates a unique patch ID.
     */
    private String generatePatchId(String repoPath) {
        Path path = Paths.get(repoPath);
        String repoName = path.getFileName().toString();
        long timestamp = System.currentTimeMillis();
        return String.format("%s-%d", repoName, timestamp);
    }
}
