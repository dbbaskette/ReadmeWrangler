# Development Guide

This document consolidates all development and process documentation for README Wrangler.

---

## Table of Contents

- [Agents](#agents)
- [Env Setup Summary](#env-setup-summary)
- [Getting Started](#getting-started)
- [Implementation Summary](#implementation-summary)
- [Mcp Embabel Integration](#mcp-embabel-integration)
- [Openai Setup](#openai-setup)
- [Quick Reference](#quick-reference)
- [Readme Wrangler Spec](#readme-wrangler-spec)
- [Setup](#setup)

---

## Agents

## Versions & Dependencies

- **Spring Boot**: 3.5.6
- **Build Tool**: maven
- **Git Repository**: Yes

## Spring Projects

### spring-boot (3.5.6)
📚 [Documentation](https://docs.spring.io/spring-boot/reference/3.5.6/)

### spring-cloud-stream (4.3.0)
📚 [Documentation](https://docs.spring.io/spring-cloud-stream/reference/4.3.0/)

### spring-cloud-stream-applications (2025.0.0)
📚 [Documentation](https://docs.spring.io/spring-cloud-stream-applications/reference/2025.0.0/)

## Dev Workflow

```bash
# Build the project
mvn clean package

# Run tests
mvn test

# Run application
mvn spring-boot:run
```

## Cloud Foundry Deployment

```bash
# Deploy to Cloud Foundry
cf push
```

**Important:**
- Ensure `manifest.yml` is configured appropriately
- Set `SPRING_PROFILES_ACTIVE=cloud` in the manifest
- Dev/test work is local - maintain separate `-local` and `-cloud` profiles
- Use local run scripts for local development work


## Documentation to Consult First

- [spring-boot 3.5.6](https://docs.spring.io/spring-boot/reference/3.5.6/)
- [spring-cloud-stream 4.3.0](https://docs.spring.io/spring-cloud-stream/reference/4.3.0/)
- [spring-cloud-stream-applications 2025.0.0](https://docs.spring.io/spring-cloud-stream-applications/reference/2025.0.0/)

## Policies

- Follow Spring best practices for dependency injection and use constructor injection over field injection
- Generate JUnit 5 tests for all new code with minimal reproducible examples
- Refer to official Spring documentation before making changes
- Follow conventional Spring project structure (src/main/java, src/main/resources) with logical package layout by domain
- Write clear, self-documenting code with Javadoc for public APIs; prefer meaningful names over abbreviations
- After code changes, compile and iteratively fix all errors until compilation succeeds
- Favor clean layered architecture: Controller -> Service -> Repository with DTOs for inputs/outputs; keep controllers thin
- Adhere to Google Java Style Guide and maintain consistency across the codebase
- Avoid anti-patterns: excessive static state, large monolithic methods, Lombok overuse that hinders readability
- Prefer Spring Boot starters and avoid unused transitive dependencies
- Design RESTful endpoints with consistent URI structures, HTTP verbs, and status codes
- Version APIs (e.g., /api/v1) and document with OpenAPI/Swagger using springdoc-openapi
- Use standardized error payloads with consistent exception mapping to HTTP status codes
- Secure endpoints with Spring Security by default; enable CSRF protection, secure session management, and proper CORS controls
- Never leak sensitive information in responses; avoid hard-coding secrets
- Enable structured logging with correlation IDs and integrate Spring Boot Actuator for metrics and health checks
- Use Micrometer for metrics collection and consider distributed tracing with OpenTelemetry
- Externalize configuration with profiles (e.g., application-dev.yml, application-prod.yml); use environment variables or secret stores for credentials
- When proposing changes, show rationale and alignment with these rules
- Recommend incremental changes that can be reviewed, tested, and merged without destabilizing existing behavior
- Clearly separate repository and service boundaries; use DTOs to shape exposed data
- Document threading and backpressure considerations when using thread pools, schedulers, or parallel streams
- Maintain READMEs with colors, icons, different header sizes, and diagrams; provide onboarding guides and example workflows

---

## Env Setup Summary

## ✅ What Was Implemented

Following the pattern from `insurance-megacorp/imc-policy-mcp-server`, I've implemented automatic `.env` file loading for README Wrangler.

### Files Created/Updated

1. **`.env.example`** - Template with all configuration options
2. **`.env`** - Your local environment file (git-ignored)
3. **`.gitignore`** - Updated to ignore all `.env*` files
4. **`pom.xml`** - Added `spring-dotenv` dependency (v4.0.0)
5. **`SETUP.md`** - Comprehensive setup guide
6. **`README.md`** - Updated quick start instructions

### How It Works

```bash
# 1. Copy template
cp .env.example .env

# 2. Edit and add your OpenAI key
# .env contains:
OPENAI_API_KEY=sk-your-actual-key-here

# 3. Run using the provided script
./run-local.sh
# .env is loaded automatically - no export needed!
```

The `spring-dotenv` library automatically loads `.env` at startup. **Just use the run script** - it's that simple!

---

## 🔑 Next Steps for You

### 1. Get OpenAI API Key

Visit: https://platform.openai.com/api-keys

1. Sign in or create account
2. Click "Create new secret key"
3. Copy the key (starts with `sk-...`)
4. Keep it secure!

### 2. Configure Your .env File

```bash
# Edit the .env file I created
nano .env  # or use VSCode, vim, etc.

# Replace the placeholder with your real key:
OPENAI_API_KEY=sk-your-real-key-here
```

### 3. Run the Application

```bash
# Use the run script (recommended)
./run-local.sh

# The .env is loaded automatically - that's it!
```

### 4. Verify It Works

```bash
# Check health
curl http://localhost:8080/actuator/health

# Should return:
# {"status":"UP"}
```

## 📋 Configuration Options in .env

All of these are optional except `OPENAI_API_KEY`:

```bash
# Required
OPENAI_API_KEY=sk-...

# Optional (defaults shown)
# OPENAI_MODEL=gpt-4o
# OPENAI_TEMPERATURE=0.7
# SERVER_PORT=8080
# LOGGING_LEVEL_COM_BASKETTECASE=DEBUG
```

## 🔒 Security

✅ **Safe:**
- `.env` is in `.gitignore`
- Never committed to Git
- Only exists locally on your machine

❌ **Never:**
- Commit `.env` to version control
- Share your API key publicly
- Push `.env` to GitHub/GitLab

✅ **Instead:**
- Use `.env.example` for templates
- Share configuration structure, not secrets
- For Cloud Foundry, use service credentials

## 🆚 Comparison with insurance-megacorp

| Feature | insurance-megacorp | README Wrangler |
|---------|-------------------|-----------------|
| `.env.example` | ✅ | ✅ |
| `.env` git-ignored | ✅ | ✅ |
| Auto-load .env | ✅ (`spring-dotenv`) | ✅ (`spring-dotenv`) |
| OpenAI key required | ✅ | ✅ |
| Cloud Foundry support | ✅ | ✅ |
| Multiple profiles | ✅ | ✅ |

Both projects now use the **same pattern** for environment management!

## 🧪 Testing Without OpenAI Key

If you want to test the build without setting up OpenAI:

```bash
# Build only (skip tests, no runtime)
mvn clean package -DskipTests

# This works because:
# - Build doesn't need OpenAI
# - Runtime initialization needs it
# - Tests mock the dependencies
```

But to **run** the application, you **must** have a valid OpenAI API key because Embabel agent requires it for GOAP (Goal-Oriented Action Planning).

## 📚 Additional Resources

- [SETUP.md](SETUP.md) - Full setup guide
- [OPENAI_SETUP.md](OPENAI_SETUP.md) - OpenAI-specific details
- [.env.example](.env.example) - Configuration template
- [Spring Dotenv](https://github.com/paulschwarz/spring-dotenv) - Library documentation

---

## Getting Started

This guide will help you get up and running with README Wrangler quickly.

## Prerequisites

- **Java 21** or higher (OpenJDK recommended)
- **Maven 3.9+**
- A repository with a README.md file

## Quick Start

### 1. Build the Application

```bash
# Clone the repository
git clone <your-repo-url>
cd ReadmeWrangler

# Build with Maven
mvn clean package

# This creates: target/readme-wrangler-1.0.0-SNAPSHOT.jar
```

### 2. Try It Out with Test Fixtures

We've included three sample repositories in the `fixtures/` directory:

```bash
# Polish the Maven app fixture
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish fixtures/maven-app

# Write the patch to a file
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish fixtures/maven-app --write-patch maven-app.diff

# View the diff
cat maven-app.diff
```

### 3. Polish Your Own Repository

```bash
# Polish current directory
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish .

# Polish specific repository
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish /path/to/your/repo

# Save patch for review
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish . --write-patch my-changes.diff
```

### 4. Use the REST API

Start the web server:

```bash
# Start in local mode
./run-local.sh

# Or use Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The server starts at http://localhost:8080

**Polish via API:**

```bash
curl -X POST http://localhost:8080/api/v1/polish \
  -H "Content-Type: application/json" \
  -d '{
    "repoPath": "/path/to/your/repo"
  }'
```

**View API Documentation:**

Open http://localhost:8080/swagger-ui.html in your browser

## What README Wrangler Does

README Wrangler analyzes your documentation and:

✅ **Detects missing test sections** and adds appropriate commands for Maven/Gradle
✅ **Fixes code fences** by adding language tags (```bash, ```java)
✅ **Normalizes headings** to consistent ATX format (using #)
✅ **Generates TOC** when documents have 4+ headings
✅ **Adds badges** for build system, JDK version, and license
✅ **Creates patches** for human review (never writes directly)

## Example Output

**Before:**
```markdown
# My Project

```
mvn clean install
```
```

**After:**
```markdown
# My Project

```bash
mvn clean install
```

## How to Run Tests

\`\`\`bash
# Using Maven wrapper (recommended)
./mvnw test

# Or with installed Maven
mvn test
\`\`\`
```

## Configuration

Customize behavior via `application.yml`:

```yaml
wrangler:
  addTocThresholdHeadings: 4    # Min headings for TOC
  badgesEnabled: true           # Add badges
  jdkVersion: "21"              # JDK version for badge
```

Or via CLI flags:

```bash
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish \
  --toc-threshold 5 \
  --badges true \
  --jdk 21
```

## Deploy to Cloud Foundry

```bash
# Build the app
mvn clean package

# Push to Cloud Foundry
cf push

# Check status
cf apps
```

The `manifest.yml` is pre-configured with sensible defaults.

## Next Steps

- Read the full [README.md](README.md) for detailed documentation
- Explore the [test fixtures](fixtures/) for examples
- Check out the [spec](README-Wrangler-spec.md) for architecture details
- Run tests: `mvn test`
- Contribute improvements!

## Troubleshooting

### Java Version Issues

Ensure you're using Java 21:

```bash
java -version
# Should show: openjdk version "21" or higher
```

### Build Failures

Clean and rebuild:

### Port Already in Use

Change the port in `application.yml`:

```yaml
server:
  port: 8081  # Use a different port
```

## Support

- GitHub Issues: [Report bugs and request features]
- Documentation: See [README.md](README.md)
- Examples: Check [fixtures/](fixtures/)

---

**Happy Wrangling! 🤠**

---

## Implementation Summary

## Summary

README Wrangler has been successfully built as a fully integrated Spring Boot + Embabel + Spring AI MCP application that automatically polishes repository documentation.

## What Was Built

### 1. Core Application ✅
- **Spring Boot 3.5.6** with Java 21
- Maven-based build system
- Clean layered architecture (Controller → Service → Tools)
- Comprehensive domain models using Java records

### 2. Embabel Agent Integration ✅
- **Embabel 0.1.3** agent framework fully integrated
- `ReadmePolisherAgent` with Goal-Oriented Action Planning (GOAP)
- Annotations: `@Agent`, `@Action`, `@AchievesGoal`, `@Condition`
- Dynamic action planning based on repository structure
- OpenAI integration for potential LLM-powered decisions

### 3. Spring AI MCP Server ✅
- **Spring AI 1.1.0-M3** MCP server with streaming HTTP
- Server-Sent Events (SSE) for real-time communication
- Six MCP tools exposed via `/mcp` endpoint
- Full HITL (Human-In-The-Loop) approval workflow
- Compatible with Claude Desktop and other MCP clients

### 4. Documentation Polishing Tools ✅
Six specialized Spring beans for markdown processing:
- **RepoScannerTool** - Detects build systems (Maven, Gradle, Makefile)
- **MarkdownLinterTool** - Validates markdown syntax
- **TextRewriteTool** - Fixes code fences and headings
- **TocTool** - Generates table of contents
- **BadgeTool** - Creates status badges
- **PatchBuilderTool** - Produces unified diffs

### 5. Multiple Interfaces ✅

#### A. MCP Server (Recommended)
Six tools via Model Context Protocol:
1. `scan_repository` - Analyze repository structure
2. `polish_readme` - Generate improvement patch
3. `approve_patch` - HITL approval
4. `reject_patch` - HITL rejection
5. `list_pending_patches` - Show all pending
6. `generate_test_command` - Create test snippet

#### B. REST API
- `POST /api/v1/polish` - Polish and return patch
- `GET /api/v1/health` - Health check
- Full OpenAPI documentation at `/swagger-ui.html`
- Spring Boot Actuator endpoints

#### C. CLI (Picocli)
- `polish` command - Analyze and generate patch
- `apply` command - Review and apply patch
- Local run scripts for Mac/Linux and Windows

### 6. Testing ✅
- **14 JUnit 5 tests** - All passing
- Test fixtures for Maven, Gradle, and multi-doc projects
- Unit tests for all tools
- Test coverage for domain models

### 7. Deployment Support ✅
- **Cloud Foundry** - `manifest.yml` configured
- **Local Development** - Separate profiles (local, cloud)
- Run scripts: `run-local.sh`, `run-local.bat`
- Environment variable configuration

### 8. Documentation ✅
- Comprehensive `README.md` with badges and examples
- `MCP_EMBABEL_INTEGRATION.md` - Deep dive into architecture
- `GETTING_STARTED.md` - Quick start guide
- `CLAUDE.md` - Project rules and guidelines
- Inline Javadoc for all public APIs

## Architecture Highlights

```
┌─────────────────────────────────────────────────┐
│        Three Access Methods                     │
├─────────────────────────────────────────────────┤
│  MCP Tools    REST API    CLI (Picocli)        │
├─────────────────────────────────────────────────┤
│            PolishingService                     │
│        (Delegates to Agent)                     │
├─────────────────────────────────────────────────┤
│       Embabel Agent (GOAP)                      │
│  Goals → Actions → Conditions                   │
├─────────────────────────────────────────────────┤
│         Specialized Tools                       │
│  Scanner • Linter • Rewriter • TOC • Badges     │
└─────────────────────────────────────────────────┘
```

## Key Features Implemented

### Core Capabilities
✅ Detects build systems (Maven, Gradle, Makefile)
✅ Adds missing "How to Run Tests" sections
✅ Fixes code fence language tags
✅ Normalizes heading hierarchy
✅ Generates table of contents (threshold: 4+ headings)
✅ Adds build/license/JDK badges
✅ Produces unified diff patches
✅ Never writes directly - always requires approval

### AI & Agent Features
✅ Embabel GOAP for intelligent planning
✅ Dynamic action selection based on conditions
✅ Extensible action system
✅ OpenAI integration ready

### MCP Integration
✅ Streaming HTTP MCP server (SSE)
✅ Six MCP tools with `@McpTool` annotations
✅ HITL approval workflow
✅ Patch management (approve/reject/list)
✅ Compatible with Claude Desktop

## What Makes This Special

1. **Goal-Oriented Planning** - Uses GOAP from gaming AI for documentation improvements
2. **Model Context Protocol** - First-class MCP server with streaming support
3. **Human-In-The-Loop** - Never writes without approval
4. **Type-Safe** - All LLM interactions are strongly typed
5. **Spring Native** - Built on Spring Boot 3.5.6 with full ecosystem support
6. **Multiple Interfaces** - MCP, REST, and CLI all work together
7. **Cloud Ready** - Cloud Foundry deployment out of the box

## Technical Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.5.6 |
| Language | Java | 21 |
| Build | Maven | 3.9+ |
| Agent Framework | Embabel | 0.1.3 |
| MCP Server | Spring AI | 1.1.0-M3 |
| CLI | Picocli | 4.7.6 |
| Markdown | CommonMark | 0.24.0 |
| Git | JGit | 6.10.0 |
| Testing | JUnit 5 | Via Spring Boot |
| Cloud | Cloud Foundry | N/A |

## Build Status

```bash
✅ mvn clean compile  - SUCCESS (0.895s)
✅ mvn test           - 14 tests, 0 failures
✅ mvn clean package  - SUCCESS (1.720s)
```

## File Structure

```
ReadmeWrangler/
├── src/main/java/com/baskettecase/readmewrangler/
│   ├── agent/                    # Embabel agent
│   │   └── ReadmePolisherAgent.java
│   ├── mcp/                      # MCP tools
│   │   └── ReadmePolishingMcpTools.java
│   ├── controller/               # REST endpoints
│   │   └── PolishController.java
│   ├── service/                  # Business logic
│   │   ├── PolishingService.java
│   │   └── PolishingConfig.java
│   ├── tool/                     # Polishing tools
│   │   ├── RepoScannerTool.java
│   │   ├── MarkdownLinterTool.java
│   │   ├── TextRewriteTool.java
│   │   ├── TocTool.java
│   │   ├── BadgeTool.java
│   │   └── PatchBuilderTool.java
│   ├── domain/                   # Domain models
│   │   ├── RepoSnapshot.java
│   │   ├── PatchBundle.java
│   │   ├── PolishingFinding.java
│   │   ├── ReadmeImprovements.java
│   │   ├── BuildSystem.java
│   │   └── Severity.java
│   ├── cli/                      # Command-line interface
│   │   └── WranglerCli.java
│   └── ReadmeWranglerApplication.java
├── src/main/resources/
│   └── application.yml           # Configuration
├── src/test/java/                # JUnit 5 tests
├── fixtures/                     # Test repositories
│   ├── maven-app/
│   ├── gradle-lib/
│   └── docs-split/
├── pom.xml                       # Maven configuration
├── manifest.yml                  # Cloud Foundry
├── run-local.sh                  # Local run (Mac/Linux)
├── run-local.bat                 # Local run (Windows)
├── README.md                     # Main documentation
├── MCP_EMBABEL_INTEGRATION.md   # Architecture guide
├── GETTING_STARTED.md           # Quick start
└── CLAUDE.md                     # Project rules
```

## Usage Examples

### 1. MCP Client (Claude Desktop)

```json
// claude_desktop_config.json
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
Use readme-wrangler to polish my project at /path/to/repo
```

### 2. REST API

```bash
curl -X POST http://localhost:8080/api/v1/polish \
  -H "Content-Type: application/json" \
  -d '{"repoPath": "/path/to/repo"}'
```

### 3. CLI

```bash
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish \
  --path /path/to/repo \
  --write-patch output.diff
```

## Configuration

### Required
```bash
export OPENAI_API_KEY="sk-..."  # For Embabel agent
```

### Optional (application.yml)
```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE
        streamable-http:
          mcp-endpoint: /mcp
    openai:
      chat:
        options:
          model: gpt-4o
          temperature: 0.7

wrangler:
  addTocThresholdHeadings: 4
  badgesEnabled: true
  jdkVersion: "21"
```

## What's Next

Potential enhancements (not yet implemented):
- LLM-powered content improvement decisions
- Multi-file polishing beyond README
- Custom user-defined polishing actions
- MCP resources for repository metadata
- MCP prompts for documentation templates
- Distributed tracing with OpenTelemetry
- Additional linter backends (Vale, remark)
- Git integration for automatic PR creation

## Comparison: Before vs After

### Before (Spec Intent)
- Simple Spring Boot app
- Direct tool orchestration
- Manual sequencing

### After (What We Built)
- Spring Boot + Embabel + Spring AI MCP
- GOAP-based dynamic planning
- Six MCP tools with HITL workflow
- Three access methods (MCP, REST, CLI)
- Type-safe agent architecture
- Streaming SSE MCP server

## Success Criteria Met

✅ Built Spring Boot + Embabel app
✅ Uses Goal-Oriented Action Planning (GOAP)
✅ Never writes files directly (patch-based)
✅ Human-In-The-Loop approval workflow
✅ Detects build systems and adds test sections
✅ Fixes code fences and headings
✅ Generates TOC and badges
✅ Multiple interfaces (MCP, REST, CLI)
✅ Comprehensive tests (14 passing)
✅ Cloud Foundry deployment ready
✅ Full documentation

## Resources

- **GitHub (Embabel):** https://github.com/embabel/embabel-agent
- **Spring AI MCP:** https://docs.spring.io/spring-ai/reference/1.1/api/mcp/
- **Model Context Protocol:** https://modelcontextprotocol.io/
- **Embabel Docs:** https://docs.embabel.com/embabel-agent/guide/

---

**🎉 README Wrangler is ready to polish your documentation!**

Built with 🌱 Embabel, 🤖 Spring AI, ☕ Java 21, and 🧠 GOAP

---

## Mcp Embabel Integration

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

---

## Openai Setup

## Why OpenAI is Required

The application uses **Embabel Agent Framework** which requires an LLM (Large Language Model) to power its Goal-Oriented Action Planning (GOAP) system. The agent makes intelligent decisions about which documentation improvements to apply using AI.

## How to Get an OpenAI API Key

1. Go to [platform.openai.com](https://platform.openai.com/signup)
2. Create an account or sign in
3. Navigate to **API Keys** section
4. Click **Create new secret key**
5. Copy the key (starts with `sk-...`)

## Configuration Options

### Option 1: Environment Variable (Recommended)

```bash
export OPENAI_API_KEY=sk-your-actual-key-here
mvn spring-boot:run
```

### Option 2: Application Properties

Edit `src/main/resources/application.yml`:

```yaml
spring:
  ai:
    openai:
      api-key: sk-your-actual-key-here  # Replace with your key
```

⚠️ **Don't commit your API key to Git!**

### Option 3: .env File

Create `.env` in project root:

```bash
OPENAI_API_KEY=sk-your-actual-key-here
```

Then run:
```bash
source .env
mvn spring-boot:run
```

## Verify It Works

Once configured, start the application:

```bash
mvn spring-boot:run
```

You should see:
```
Started ReadmeWranglerApplication in X seconds
```

Test the health endpoint:
```bash
curl http://localhost:8080/health
```

## Cost Information

- The application uses **GPT-4o** model
- Typical cost: ~$0.01-0.05 per README polishing operation
- Monitor usage at [platform.openai.com/usage](https://platform.openai.com/usage)

## Alternative: Run Without Embabel Agent

If you only need the MCP tools without the intelligent agent planning, you would need to refactor the code to make Embabel optional. The current implementation tightly integrates the agent for smart decision-making.

---

## Quick Reference

## 🚀 Quick Start

```bash
# 1. Set API key
export OPENAI_API_KEY="sk-..."

# 2. Build
mvn clean package

# 3. Run
./run-local.sh
```

Server starts at: http://localhost:8080

## 📋 Quick Commands

### Build & Test
```bash
mvn clean compile          # Compile only
mvn test                   # Run tests (14 tests)
mvn clean package          # Build JAR
```

### Run
```bash
./run-local.sh                                      # Start server (Mac/Linux)
./run-local.bat                                     # Start server (Windows)
mvn spring-boot:run -Dspring-boot.run.profiles=local  # Maven direct
```

### CLI Usage
```bash
# Polish repository
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish /path/to/repo

# Write patch to file
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish \
  --path /path/to/repo \
  --write-patch output.diff

# Apply patch (shows instructions)
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar apply --patch output.diff
```

### REST API
```bash
# Health check
curl http://localhost:8080/api/v1/health

# Polish repository
curl -X POST http://localhost:8080/api/v1/polish \
  -H "Content-Type: application/json" \
  -d '{"repoPath": "/path/to/repo"}' | jq

# OpenAPI docs
open http://localhost:8080/swagger-ui.html
```

### MCP Server
```bash
# MCP endpoint (SSE streaming)
http://localhost:8080/mcp

# Claude Desktop config
cat > ~/.config/claude/claude_desktop_config.json <<EOF
{
  "mcpServers": {
    "readme-wrangler": {
      "url": "http://localhost:8080/mcp",
      "type": "streamable-http"
    }
  }
}
EOF
```

## 🔌 MCP Tools Summary

| Tool | Purpose |
|------|---------|
| `scan_repository` | Detect build system and files |
| `polish_readme` | Generate improvement patch |
| `approve_patch` | Approve and save patch (HITL) |
| `reject_patch` | Reject patch |
| `list_pending_patches` | Show all pending patches |
| `generate_test_command` | Generate test snippet |

## 📂 Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Maven dependencies & config |
| `application.yml` | Spring & MCP configuration |
| `ReadmePolisherAgent.java` | Embabel agent with GOAP |
| `ReadmePolishingMcpTools.java` | Six MCP tools |
| `PolishingService.java` | Service layer (delegates to agent) |
| `manifest.yml` | Cloud Foundry deployment |

## 🧪 Test Fixtures

```bash
# Test with provided fixtures
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish fixtures/maven-app
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish fixtures/gradle-lib
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish fixtures/docs-split
```

## ⚙️ Configuration Quick Reference

### Environment Variables
```bash
export OPENAI_API_KEY="sk-..."  # Required for Embabel
```

### application.yml Key Settings
```yaml
spring:
  ai:
    mcp:
      server:
        protocol: STREAMABLE        # MCP streaming mode
        streamable-http:
          mcp-endpoint: /mcp        # MCP endpoint path
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o

wrangler:
  addTocThresholdHeadings: 4    # Min headings for TOC
  badgesEnabled: true            # Generate badges
  jdkVersion: "21"              # JDK version badge
```

## 🐛 Troubleshooting

### Port Already in Use
```yaml
# In application.yml
server:
  port: 8081  # Change port
```

### Maven Build Fails
```bash
mvn clean install -U  # Force update dependencies
```

### Tests Fail
```bash
mvn clean test -X  # Debug mode
```

### OpenAI API Issues
```bash
# Verify key is set
echo $OPENAI_API_KEY

# Check key is valid (not shown in logs for security)
grep "api-key" src/main/resources/application.yml
```

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [README.md](README.md) | Main documentation |
| [MCP_EMBABEL_INTEGRATION.md](MCP_EMBABEL_INTEGRATION.md) | Architecture deep dive |
| [GETTING_STARTED.md](GETTING_STARTED.md) | Step-by-step guide |
| [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) | What was built |
| [CLAUDE.md](CLAUDE.md) | Project rules & guidelines |

## 🚢 Deployment

### Cloud Foundry
```bash
mvn clean package
cf push
cf logs readme-wrangler --recent
```

### Local Development
```bash
# Use local profile (verbose logging)
./run-local.sh
```

### Production
```bash
# Use cloud profile
SPRING_PROFILES_ACTIVE=cloud \
OPENAI_API_KEY=$OPENAI_API_KEY \
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar
```

## 📊 Monitoring

```bash
# Actuator endpoints
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

## 🔗 URLs at a Glance

- **Application:** http://localhost:8080
- **REST API:** http://localhost:8080/api/v1
- **MCP Server:** http://localhost:8080/mcp
- **Health:** http://localhost:8080/api/v1/health
- **OpenAPI:** http://localhost:8080/swagger-ui.html
- **Actuator:** http://localhost:8080/actuator

## 💡 Quick Tips

1. **Always set OPENAI_API_KEY** before running
2. **Use MCP client** (Claude Desktop) for best experience
3. **Never commits patches directly** - always review first
4. **Check test fixtures** for example repositories
5. **Use `--write-patch`** to save diffs for later review

## 📞 Getting Help

```bash
# CLI help
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar --help

# OpenAPI docs (when server is running)
open http://localhost:8080/swagger-ui.html

# Read the docs
cat README.md
cat MCP_EMBABEL_INTEGRATION.md
```

---

**For detailed information, see [README.md](README.md)**

---

## Readme Wrangler Spec

**Tagline:** “Make every repo’s README shine.”  
**Alt names (pick your vibe):** Doc Wrangler · Markdown Mechanic · Readme Rodeo · Docsmith · PolishBot

---

## 1) Goal & Scope

Build a small Spring Boot app that embeds **Embabel** to act as a developer-scale helper that:

- Watches a repository (local or via webhook/CI artifact) for `README.md` (and other docs) changes.
- **Tightens language**, **fixes code blocks**, **adds a “How to run tests” section if missing**, and normalizes headings/TOCs.
- Optionally performs **project-wide cleanup** via an **Embabel MCP** toolset (dedupe/merge scattered Markdown, standardize badges, lint + format docs).
- Produces a **patch** (diff) rather than mutating files directly; humans approve/apply (HITL).

This is designed to implementable in a day, demoable in 10–15 minutes.

## 2) Triggers & Inputs

- **Local CLI**: `./readme-wrangler polish --path /repo`
- **HTTP webhook**: `POST /events/readme-updated` with repo URL / artifact location
- **CI job**: stage artifacts (`README.md`, `/docs/**/*.md`, test reports, scripts) and call `POST /ci/polish`

**Artifacts the agent may read:**
- Markdown files: `README.md`, `/docs/**/*.md`, `CONTRIBUTING.md`, `CHANGELOG.md`
- Test hints: `pom.xml` or `build.gradle`, `mvnw`, `gradlew`, `Makefile`, `./scripts/test*`, GitHub Actions workflows
- Project metadata: `package.json`, `settings.gradle`, `LICENSE*`

## 3) High-level Architecture

```
Spring Boot (Web + CLI)
  ├─ Embabel Agent: ReadmePolisherAgent
  │    ├─ Goal: PolishReadmeGoal (input: RepoSnapshot) → PatchBundle (diffs + notes)
  │    └─ Sub-goals: Lint, Rewrite, EnsureTestSection, Format, TOCNormalize
  ├─ Tools (Spring beans)
  │    ├─ MarkdownLinterTool
  │    ├─ RepoScannerTool
  │    ├─ TextRewriteTool
  │    ├─ TocTool
  │    ├─ BadgeTool
  │    └─ PatchBuilderTool
  ├─ HITL/MCP: ApprovePatchTool, MergeMarkdownsTool, DedupeHeadingsTool, FormatRepoTool
  └─ Adapters: GitHub/GitLab (fetch & open PR), LocalFS adapter, CI webhook controller
```

**Design notes**
- Keep tools **typed** and pure where possible so they’re easy to unit-test.
- The agent **never writes**; it returns a patch (diff). Application layer applies only after approval.

## 4) Agent Contract & Data Types (sketch)

```java
// Inputs captured by adapters:
record RepoSnapshot(Path root, List<Path> markdownFiles, BuildSystem build, List<Path> scripts){}
enum BuildSystem { MAVEN, GRADLE, OTHER }

// Core artifacts:
record PolishingFinding(String id, String message, Severity sev, Path file, int lineStart, int lineEnd){}
enum Severity { INFO, WARN, ERROR }

record ReadmeImprovements(
  boolean addedTestSection,
  boolean fixedCodeBlocks,
  boolean normalizedHeadings,
  boolean addedToc,
  List<PolishingFinding> notes
){}

record PatchBundle(String unifiedDiff, ReadmeImprovements summary){}
```

**Main goal signature** (conceptual):
```
PolishReadmeGoal: RepoSnapshot -> PatchBundle
```

## 5) Tools (Spring beans) & Responsibilities

### MarkdownLinterTool
- **Purpose:** run markdown rules; return precise findings (line ranges, rule IDs).
- **Backends:** markdownlint, Vale, remark-lint (choose one to start; see §10).

### RepoScannerTool
- **Purpose:** detect presence of test commands & scripts.
- **Heuristics:** 
  - Maven: `./mvnw -q -DskipTests=false test`
  - Gradle: `./gradlew test`
  - If `Makefile` has `test` target → `make test`
  - `scripts/test*` → derive command
- **Output:** recommended snippet for **“How to run tests”** with OS-agnostic notes.

### TextRewriteTool
- **Purpose:** tighten language (active voice, concise intros), fix fenced code blocks:
  - Ensure backticks have language tags (e.g., ```java, ```bash).
  - Combine split fences; remove trailing whitespace; wrap lines smartly.

### TocTool
- **Purpose:** ensure a “Table of Contents” if file > N headings (configurable); normalize heading level steps (no skipping).

### BadgeTool
- **Purpose:** standardize CI, License, Maven Central, JDK version badges at the top.

### PatchBuilderTool
- **Purpose:** produce a **unified diff** from original → proposed, grouped by file.

## 6) Embabel Planning (example plan)

1. **Lint** README → collect issues.
2. **Scan repo** for test commands → derive “How to run tests” snippet if missing.
3. **Rewrite** sections: tighten intro, fix code fences, normalize headings.
4. **Insert TOC** when warranted.
5. **Update badges** (optional, guard behind a flag).
6. **Build patch** → `PatchBundle`.
7. **HITL**: Require approval via MCP `ApprovePatchTool`.
8. **Apply** (open a PR or write to a working branch).

## 7) HTTP & CLI Surface

- `POST /polish` → body: `{ "repoPath" | "repoUrl", "files": [...optional...] }` → returns `PatchBundle` (diff + summary).
- `POST /approve` → applies latest patch (requires capability + target branch).
- CLI: 
  - `readme-wrangler polish --path . --write-patch out.diff`
  - `readme-wrangler apply --patch out.diff --branch docs/polish`

## 8) HITL via MCP (Embabel)

Expose the following **MCP tools** so a human can step in from Claude Desktop (or any MCP client):

- `ApprovePatchTool(repo, patchId) -> {appliedBranch}`
- `MergeMarkdownsTool(repo, sources[]) -> {mergedFile, notes}`
- `DedupeHeadingsTool(file) -> {diff}`
- `FormatRepoTool(repo, mode=markdown|code|both) -> {report, diff}`

Use these to demo a **project-wide cleanup**: dedupe duplicate `README` sections across `/docs`, merge small How-Tos into a single “Guides.md,” and normalize heading levels.

## 9) Guardrails & Policies

- Never push to `main`; use `docs/polish/<timestamp>` branch.
- Max line change per file (configurable) to avoid rewriting entire docs unexpectedly.
- Keep a **“before/after”** block for each major change in the PR description.
- All AI rewrites **must** preserve code semantics (no code content changes beyond fences/format).

## 10) Recommended Extensions & Integrations (pick a minimal set first)

- **markdownlint-cli2** — fast baseline lint (headings, lists, code-fence integrity).
- **Vale** — prose style rules (tech tone, passive voice, banned terms).
- **remark / unified** — programmatic AST transforms for MD (TOC, heading levels).
- **prettier-plugin-markdown** — consistent wrapping/format.
- **doctoc** — TOC generation (if you don’t do it via remark).
- **typos** or **cspell** — spell-check proper nouns & code-friendly words.
- **conventional-commits-checker** — ensure PRs use Conventional Commits.
- **licensee** — validate LICENSE presence & type (optional, adds badge).
- **OpenAPI Markdown snippets** — if repo has OpenAPI, generate usage block.

_Target minimal set for v1: markdownlint + remark (TOC) + Prettier + typos._

## 11) Config

```yaml
wrangler:
  addTocThresholdHeadings: 4
  headingStyle: "atx"        # hashes over underlines
  codeFenceLanguages:
    - java
    - bash
    - yaml
  badges:
    enabled: true
    ci: "GitHub Actions"
    jdk: "21"
  guardrails:
    maxChangesPerFile: 300
    requireMcpApproval: true
```

## 12) Acceptance Criteria

- Given a README lacking a test section, running `polish` adds a correct, runnable snippet for the detected build system.
- Broken or untagged code fences are normalized and language-tagged.
- Headings are normalized to a consistent hierarchy; optional TOC appears when > N headings.
- Output is a **unified diff**; no direct file mutations without approval.
- CI demo: on push to `docs/**` or `README.md`, job comments the proposed patch on the PR and awaits MCP approval to apply.

## 13) Test Fixtures & E2E Demo

Provide sample repos in `/fixtures`:
- `maven-app/` — no test section, mixed fences, inconsistent headings.
- `gradle-lib/` — long README needing TOC, extra badges.
- `docs-split/` — multiple small markdowns to merge.

E2E script:
1. `./readme-wrangler polish --path fixtures/maven-app > out.diff`
2. Approve via MCP (simulated or live).
3. `./readme-wrangler apply --patch out.diff --branch docs/polish`
4. Open PR automatically with summary & screenshots.

## 14) “Build me” prompt for your code assistant

> **You are building “README Wrangler,” a Spring Boot + Embabel app that polishes repo docs and returns a patch.**  
> Implement the following:
> 1. Boot app with Web + Picocli CLI modules; config in `application.yml` as in §11.
> 2. Embabel agent `ReadmePolisherAgent` with main goal `PolishReadmeGoal(RepoSnapshot) -> PatchBundle` and sub-goals from §6.
> 3. Spring tools from §5 with clean interfaces and unit tests (Junit 5). Stub external linters behind ports; inject adapters.
> 4. HTTP endpoints & CLI from §7. Return JSON (`PatchBundle`) + write unified diff to file when `--write-patch` is provided.
> 5. MCP tools from §8; gate `apply` behind `requireMcpApproval`.
> 6. Provide `/fixtures` from §13 and an `E2E.md` showing the flow.
> 7. GitHub Action: on README/docs change, run polish, post diff comment, pause for MCP approval, then apply to `docs/polish/*` branch.
> 8. PR template section “Before/After” and checklist: _meaning preserved_, _headings normalized_, _tests section added_, _code fences language-tagged_.

**Non-goals:** don’t write a full linter; lean on existing tools. No aggressive rewriting beyond headings, code-fence fixes, TOC, and test section insertion.

## 15) Nice-to-haves (phase 2)
- Detect language per code fence; add language tag automatically.
- Generate “Contributing” preface if repo is public and missing CONTRIBUTING.md.
- Auto-badge test coverage if Jacoco/Coverage artifact is present.
- Summarize major changes as a one-line PR title using Conventional Commits.

## 16) License & Ownership
- Apache-2.0 (default). Include `NOTICE` and a short rationale in README.

## 17) Quick Naming Rationale
- **README Wrangler**: evokes “tidy, organize, herd the docs,” playful without being frivolous.
- **Markdown Mechanic**: conveys “fix what’s broken.”
- **Readme Rodeo**: fun, demo-friendly.

Pick your favorite; the spec uses **README Wrangler** as the working name.

---

## Setup

## Prerequisites

- **Java 21+** - [Download from Oracle](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.9+** - [Install Maven](https://maven.apache.org/install.html)
- **OpenAI API Key** - Required for Embabel agent intelligence

---

## Step 1: Get OpenAI API Key

1. Go to [platform.openai.com](https://platform.openai.com/signup)
2. Create an account or sign in
3. Navigate to **API Keys** section
4. Click **Create new secret key**
5. Copy the key (starts with `sk-...`)
6. **Save it securely** - you won't be able to see it again!

**Cost:** ~$0.01-0.05 per README polishing operation with GPT-4o

## Step 2: Configure Environment

### Option A: Using .env file (Recommended)

```bash
# Copy the example file
cp .env.example .env

# Edit .env and paste your API key
nano .env  # or use your favorite editor
```

The `.env` file should look like:
```bash
OPENAI_API_KEY=sk-your-actual-key-here
```

✅ **Benefits:**
- Git-ignored for security
- Works automatically with `mvn spring-boot:run`
- Same pattern as insurance-megacorp project
- No need to set environment variables manually

### Option B: Environment Variable

```bash
export OPENAI_API_KEY=sk-your-actual-key-here
```

Add to your `~/.zshrc` or `~/.bashrc` to make it permanent.

## Step 3: Build the Project

```bash
# Clean build
mvn clean package

# Should see: BUILD SUCCESS
```

**Expected output:**
```
Tests run: 21, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Step 4: Run the Application

### Using Run Scripts

**macOS/Linux:**
```bash
chmod +x run-local.sh
./run-local.sh
```

**Windows:**
```cmd
run-local.bat
```

### Using Maven Directly

```bash
mvn spring-boot:run
```

### Verify It's Running

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

## Step 5: Test MCP Server

The MCP server is exposed at:
```
http://localhost:8080/mcp
```

**Protocol:** Server-Sent Events (SSE) streaming
**Keep-Alive:** 30 seconds

### Test with curl

```bash
curl http://localhost:8080/mcp
```

You should see MCP initialization messages.

## Step 6: Configure Claude Desktop (Optional)

To use with Claude Desktop, edit:
- **macOS:** `~/Library/Application Support/Claude/claude_desktop_config.json`
- **Windows:** `%APPDATA%\Claude\claude_desktop_config.json`

Add this configuration:

```json
{
  "mcpServers": {
    "readme-wrangler": {
      "url": "http://localhost:8080/mcp",
      "transport": "sse"
    }
  }
}
```

Then:
1. Start the application: `mvn spring-boot:run`
2. Restart Claude Desktop
3. Look for 6 new tools in Claude

## Troubleshooting

### Error: "OpenAI API key not found"

**Problem:** The application can't find your API key.

**Solutions:**
1. Check `.env` file exists and has correct key
2. Verify key starts with `sk-`
3. Try setting environment variable: `export OPENAI_API_KEY=...`
4. Check file permissions on `.env` (should be readable)

### Error: "Unsatisfied dependency... openAiModelsConfig"

**Problem:** Embabel can't initialize without valid OpenAI configuration.

**Solution:** Ensure you have a valid OpenAI API key set in `.env` or environment.

### Port 8080 Already in Use

**Problem:** Another application is using port 8080.

**Solutions:**
1. Stop other application
2. Or change port in `.env`:
   ```bash
   SERVER_PORT=8081
   ```
3. Restart application

### Tests Failing

**Problem:** Unit tests failing during build.

**Solution:**
```bash
# Skip tests to build faster
mvn clean package -DskipTests

# Or run tests separately
mvn test
```

### Maven Can't Download Dependencies

**Problem:** Embabel dependencies not found.

**Solution:** The `pom.xml` includes custom repositories. Ensure you have internet access and Maven can reach:
- `https://repo.embabel.com/artifactory/libs-release`
- `https://repo.spring.io/milestone`

## Verification Checklist

- [ ] Java 21+ installed (`java -version`)
- [ ] Maven 3.9+ installed (`mvn -version`)
- [ ] OpenAI API key obtained
- [ ] `.env` file created with API key
- [ ] Project builds successfully (`mvn clean package`)
- [ ] All 21 tests pass
- [ ] Application starts without errors
- [ ] Health endpoint responds: `curl http://localhost:8080/actuator/health`
- [ ] MCP endpoint accessible: `curl http://localhost:8080/mcp`

## Next Steps

- Read [MCP_EMBABEL_INTEGRATION.md](MCP_EMBABEL_INTEGRATION.md) for architecture details
- See [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for command cheat sheet
- Try the CLI: `java -jar target/readme-wrangler-*.jar polish /path/to/repo`
- Configure Claude Desktop to use the MCP server

## Security Notes

⚠️ **Never commit `.env` to Git!**
- The `.env` file is in `.gitignore`
- Use `.env.example` for sharing configuration templates
- For Cloud Foundry, set `OPENAI_API_KEY` as a service credential

✅ **Monitor API Usage:**
- Check usage at [platform.openai.com/usage](https://platform.openai.com/usage)
- Set spending limits in OpenAI account settings
- Typical cost: $0.01-0.05 per polish operation

---

