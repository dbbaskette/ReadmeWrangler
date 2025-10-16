# README Wrangler: Embabel + Spring AI MCP Integration

This document describes the integration of Embabel agent framework and Spring AI Model Context Protocol (MCP) server into README Wrangler.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                    README Wrangler                          │
├─────────────────────────────────────────────────────────────┤
│  REST API         CLI (Picocli)        MCP Server (HTTP)    │
│  /api/v1/polish   polish --path .      /mcp (SSE Streaming) │
├─────────────────────────────────────────────────────────────┤
│                   PolishingService                          │
│              Delegates to Embabel Agent                     │
├─────────────────────────────────────────────────────────────┤
│               Embabel Agent Framework                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ ReadmePolisherAgent (@Agent)                         │  │
│  │                                                       │  │
│  │ • polishReadmeGoal (@AchievesGoal)                  │  │
│  │   - scanRepositoryGoal (@Action)                    │  │
│  │   - lintMarkdownGoal (@Action)                      │  │
│  │   - rewriteMarkdownGoal (@Action)                   │  │
│  │                                                       │  │
│  │ Conditions: shouldAddTestSection, shouldAddToc      │  │
│  │ Uses GOAP (Goal-Oriented Action Planning)          │  │
│  └──────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│              Spring AI MCP Server (Streaming)               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ ReadmePolishingMcpTools (@Component)                 │  │
│  │                                                       │  │
│  │ MCP Tools (exposed via Server-Sent Events):         │  │
│  │ • scan_repository                                   │  │
│  │ • polish_readme                                     │  │
│  │ • approve_patch (HITL)                              │  │
│  │ • reject_patch (HITL)                               │  │
│  │ • list_pending_patches                              │  │
│  │ • generate_test_command                             │  │
│  └──────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────┤
│                     Tool Layer                              │
│  RepoScanner • MarkdownLinter • TextRewriter • TocTool      │
│  BadgeTool • PatchBuilder                                   │
└─────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. Embabel Agent: ReadmePolisherAgent

**Location:** `src/main/java/com/baskettecase/readmewrangler/agent/ReadmePolisherAgent.java`

**Purpose:** Uses Goal-Oriented Action Planning (GOAP) to intelligently polish documentation.

**Key Annotations:**
- `@Agent` - Declares the agent
- `@Action` - Marks actions the agent can perform
- `@AchievesGoal` - Indicates the primary goal-achieving action
- `@Condition` - Defines preconditions for actions

**Main Goal:**
```java
@Action
@AchievesGoal(description = "Polish README and create patch for approval")
public PatchBundle polishReadmeGoal(PolishingContext context)
```

**Sub-Actions:**
- `scanRepositoryGoal` - Detects build system, finds markdown files
- `lintMarkdownGoal` - Validates markdown syntax
- `rewriteMarkdownGoal` - Applies all fixes and improvements

**Conditions:**
- `shouldAddTestSection` - Determines if test section is needed
- `shouldAddToc` - Determines if TOC should be added

### 2. Spring AI MCP Server

**Endpoint:** `POST /mcp` (Server-Sent Events streaming)

**Protocol:** MCP Streamable HTTP
**Keep-Alive:** 30 seconds

**Configuration:**
```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE
        name: readme-wrangler-mcp
        type: SYNC
        streamable-http:
          mcp-endpoint: /mcp
          keep-alive-interval: 30s
```

### 3. MCP Tools

**Location:** `src/main/java/com/baskettecase/readmewrangler/mcp/ReadmePolishingMcpTools.java`

All tools use the `@McpTool` annotation and are automatically exposed via MCP.

#### scan_repository

Scans a repository to detect build system, markdown files, and scripts.

**Parameters:**
- `repoPath` (required) - Absolute path to repository

**Returns:**
```json
{
  "repoPath": "/path/to/repo",
  "buildSystem": "MAVEN",
  "markdownFilesCount": 3,
  "scriptsCount": 2,
  "markdownFiles": ["README.md", "CONTRIBUTING.md"]
}
```

#### polish_readme

Analyzes and polishes README, generating a patch for approval.

**Parameters:**
- `repoPath` (required) - Absolute path to repository
- `patchId` (optional) - Unique identifier for patch

**Returns:**
```json
{
  "patchId": "my-repo-1729095621000",
  "hasChanges": true,
  "summary": "4 improvements, 12 findings",
  "addedTestSection": true,
  "fixedCodeBlocks": true,
  "normalizedHeadings": false,
  "addedToc": true,
  "findingsCount": 12,
  "diff": "--- a/README.md\n+++ b/README.md\n..."
}
```

#### approve_patch (HITL)

Approves a pending patch and writes it to a file.

**Parameters:**
- `patchId` (required) - Patch identifier from polish_readme
- `outputPath` (required) - Where to save the patch file

**Returns:**
```json
{
  "patchId": "my-repo-1729095621000",
  "patchFile": "/path/to/output.diff",
  "summary": "4 improvements, 12 findings",
  "instructions": "Apply with: git apply /path/to/output.diff"
}
```

#### reject_patch

Rejects a pending patch.

**Parameters:**
- `patchId` (required) - Patch identifier to reject

#### list_pending_patches

Lists all patches awaiting approval.

**Returns:**
```json
{
  "count": 2,
  "patches": {
    "repo1-123": "3 improvements, 8 findings",
    "repo2-456": "2 improvements, 5 findings"
  }
}
```

#### generate_test_command

Generates test command snippet based on detected build system.

**Parameters:**
- `repoPath` (required) - Absolute path to repository

**Returns:**
```json
{
  "buildSystem": "MAVEN",
  "testCommand": "## How to Run Tests\n\n```bash\n./mvnw test\n```"
}
```

## HITL (Human-In-The-Loop) Workflow

README Wrangler implements a complete HITL approval workflow:

1. **Polish** - Agent analyzes repository and generates patch
   ```
   MCP Tool: polish_readme(repoPath="/path/to/repo")
   Returns: {patchId, diff, summary}
   ```

2. **Review** - Human reviews the proposed changes
   - Examine the unified diff
   - Check the summary of improvements
   - List all pending patches if needed

3. **Approve or Reject**
   - **Approve:** `approve_patch(patchId, outputPath)`
   - **Reject:** `reject_patch(patchId)`

4. **Apply** (Manual)
   ```bash
   git apply /path/to/patch.diff
   ```

## Usage Examples

### Via Claude Desktop (MCP Client)

Add to `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "readme-wrangler": {
      "url": "http://localhost:8080/mcp",
      "type": "streamable-http"
    }
  }
}
```

Then in Claude:

```
Please use the readme-wrangler MCP server to:
1. Scan the repository at /Users/me/my-project
2. Polish the README
3. Show me the proposed changes
4. If they look good, approve the patch
```

### Via REST API

```bash
# Polish repository
curl -X POST http://localhost:8080/api/v1/polish \
  -H "Content-Type: application/json" \
  -d '{"repoPath": "/path/to/repo"}' | jq
```

### Via CLI

```bash
# Polish and write patch
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish \
  --path /path/to/repo \
  --write-patch output.diff
```

## Configuration

### Required Environment Variables

```bash
export OPENAI_API_KEY="sk-..."  # For Embabel agent LLM interactions
```

### application.yml

```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE
        name: readme-wrangler-mcp
        streamable-http:
          mcp-endpoint: /mcp
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7
```

## Dependencies

**Spring AI (1.1.0-M3):**
- `spring-ai-starter-mcp-server-webmvc` - MCP server with SSE streaming
- `spring-ai-starter-model-openai` - OpenAI chat model

**Embabel (0.1.3):**
- `embabel-agent-starter` - Core agent framework
- `embabel-agent-starter-openai` - OpenAI integration for agents

## Key Differences from Original

### Before (Direct Orchestration)

```java
@Service
public class PolishingService {
    public PatchBundle polishRepository(Path path, PolishingConfig config) {
        // Direct sequential execution
        RepoSnapshot snapshot = repoScanner.scanRepository(path);
        Path readme = findReadme(snapshot);
        String content = Files.readString(readme);
        // ... more steps
    }
}
```

### After (Agent-Based with GOAP)

```java
@Agent(description = "Polish repository README")
public class ReadmePolisherAgent {

    @Action
    @AchievesGoal(description = "Polish README")
    public PatchBundle polishReadmeGoal(PolishingContext context) {
        // Embabel uses GOAP to find optimal path to goal
        // Can dynamically adjust based on conditions
    }

    @Condition
    public boolean shouldAddTestSection(String content, BuildSystem build) {
        // Dynamic conditions guide planning
    }
}
```

**Benefits:**
- **Dynamic Planning** - GOAP finds optimal action sequences
- **Conditional Logic** - Conditions guide agent behavior
- **Extensibility** - Easy to add new actions without modifying existing code
- **LLM Integration** - Agent can use LLMs for decisions when needed

## Testing

All existing tests pass with the new architecture:

```bash
mvn test
# Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
```

The agent layer is transparent to the REST API and CLI - they continue to work as before but now benefit from GOAP planning.

## Future Enhancements

1. **LLM-Powered Decisions** - Use OpenAI to make content improvement decisions
2. **Multi-File Polishing** - Extend beyond README to all documentation
3. **Custom Actions** - Allow users to define custom polishing actions
4. **Observability** - Add metrics for agent planning and execution
5. **MCP Resources** - Expose repository metadata as MCP resources
6. **MCP Prompts** - Provide templates for common documentation tasks

## Learn More

- **Embabel:** https://github.com/embabel/embabel-agent
- **Spring AI MCP:** https://docs.spring.io/spring-ai/reference/1.1/api/mcp/
- **Model Context Protocol:** https://modelcontextprotocol.io/

---

**Built with 🌱 Embabel, 🤖 Spring AI, and ☕ Java**
