# 📝 README Wrangler

![Maven Build](https://img.shields.io/badge/build-maven-blue) ![JDK 21](https://img.shields.io/badge/JDK-21-orange) ![Spring Boot 3.5.6](https://img.shields.io/badge/Spring%20Boot-3.5.6-green) ![Embabel 0.1.3](https://img.shields.io/badge/Embabel-0.1.3-purple) ![Spring AI 1.1.0-M3](https://img.shields.io/badge/Spring%20AI-1.1.0--M3-blue) ![License](https://img.shields.io/badge/license-Apache--2.0-green)

**Make every repo's README shine with AI-powered agents.**

README Wrangler is a Spring Boot application that automatically polishes repository documentation using the **Embabel agent framework** and **Spring AI MCP server**. It analyzes README files, fixes common issues, and generates patches for human review via Model Context Protocol—always Human-In-The-Loop (HITL).

🌱 **Powered by Embabel GOAP** - Uses Goal-Oriented Action Planning for intelligent documentation improvements
🤖 **MCP Server** - Exposes tools via Model Context Protocol for Claude and other AI clients
🔧 **Patch-Based** - Never writes directly; always requires human approval

---

## 🎯 Features

### Core Capabilities
- 🔍 **Smart Analysis** - Detects build systems (Maven, Gradle, Makefile) and scans documentation
- 📋 **Missing Test Sections** - Automatically adds "How to Run Tests" with correct commands
- 🎨 **Code Block Fixes** - Ensures all code fences have language tags
- 📊 **Heading Normalization** - Fixes heading hierarchy and converts to ATX format (#)
- 📑 **TOC Generation** - Adds table of contents when documents exceed threshold
- 🏷️ **Badge Management** - Generates build, license, and JDK badges

### AI & Agent Features
- 🌱 **Embabel Agent** - Goal-Oriented Action Planning (GOAP) for intelligent polishing
- 🤖 **MCP Server** - Model Context Protocol server with streaming HTTP support
- 🔄 **HITL Workflow** - Human approval required before applying changes
- 🧠 **Dynamic Planning** - Agent adapts strategy based on repository structure

### Interfaces
- 🌐 **REST API** - HTTP endpoints for programmatic access
- 💻 **CLI** - Command-line interface with Picocli
- 🔌 **MCP Tools** - Six MCP tools for Claude Desktop and other MCP clients

---

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [Installation](#installation)
- [Usage](#usage)
  - [MCP Client (Claude Desktop)](#mcp-client-claude-desktop)
  - [CLI Usage](#cli-usage)
  - [REST API](#rest-api)
- [MCP Tools](#mcp-tools)
- [Embabel Agent](#embabel-agent)
- [Configuration](#configuration)
- [Architecture](#architecture)
- [Development](#development)
- [Testing](#testing)
- [Deployment](#deployment)
- [Examples](#examples)
- [Contributing](#contributing)
- [License](#license)

---

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- OpenAI API key (for Embabel agent)
- Git (optional, for version control)

### Build and Run

```bash
# Clone the repository
git clone <repository-url>
cd ReadmeWrangler

# Copy .env.example to .env and add your OpenAI API key
cp .env.example .env
# Edit .env and set OPENAI_API_KEY=sk-your-actual-key-here

# Build the project
mvn clean package

# Run with local profile (automatically loads .env)
./run-local.sh

# Or use Maven directly
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The server starts on port 8080 with:
- REST API at `http://localhost:8080/api/v1`
- MCP Server at `http://localhost:8080/mcp` (SSE streaming)
- OpenAPI docs at `http://localhost:8080/swagger-ui.html`

---

## 📦 Installation

### From Source

```bash
# Build the JAR
mvn clean package

# The executable JAR will be at:
# target/readme-wrangler-1.0.0-SNAPSHOT.jar
```

### System Requirements

- **JDK**: OpenJDK 21 or newer
- **Memory**: 1GB minimum
- **Disk**: 100MB for application + space for repositories

---

## 💻 Usage

### MCP Client (Claude Desktop)

**Recommended:** Use via Claude Desktop for the best experience.

1. Add to `claude_desktop_config.json`:

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

2. Start README Wrangler server:

```bash
export OPENAI_API_KEY="sk-..."
./run-local.sh
```

3. In Claude Desktop, use natural language:

```
Please use the readme-wrangler MCP server to:
1. Scan my project at /Users/me/my-project
2. Polish the README
3. Show me what changes you would make
4. If they look good, approve the patch and save it
```

Claude will use the six MCP tools to analyze, polish, and manage patches.

### CLI Usage

README Wrangler provides a command-line interface for polishing documentation:

#### Polish Command

Analyze a repository and generate a patch:

```bash
# Polish current directory
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish

# Polish specific repository
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish /path/to/repo

# Write patch to file
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish --write-patch out.diff

# Customize settings
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar polish \
  --toc-threshold 5 \
  --badges true \
  --jdk 21
```

#### Apply Command

Review and apply patches:

```bash
# Apply patch to files
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar apply --patch out.diff

# Create branch and apply
java -jar target/readme-wrangler-1.0.0-SNAPSHOT.jar apply \
  --patch out.diff \
  --branch docs/polish
```

### REST API

Start the web server and use the HTTP endpoints:

```bash
# Start server
./run-local.sh

# Polish a repository via API
curl -X POST http://localhost:8080/api/v1/polish \
  -H "Content-Type: application/json" \
  -d '{
    "repoPath": "/path/to/repo",
    "config": {
      "addTocThresholdHeadings": 4,
      "badgesEnabled": true,
      "jdkVersion": "21"
    }
  }'
```

#### API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/polish` | POST | Polish repository and return patch |
| `/api/v1/health` | GET | Health check |
| `/actuator/health` | GET | Actuator health endpoint |
| `/actuator/metrics` | GET | Application metrics |
| `/swagger-ui.html` | GET | OpenAPI documentation |

---

## 🔌 MCP Tools

README Wrangler exposes six MCP tools that AI clients like Claude can use:

### 1. scan_repository
Scans a repository to detect build system and markdown files.

**Parameters:**
- `repoPath` (required) - Absolute path to repository

**Returns:** Build system, file counts, and file lists

### 2. polish_readme
Analyzes and polishes README, generating a patch.

**Parameters:**
- `repoPath` (required) - Repository path
- `patchId` (optional) - Unique identifier

**Returns:** Patch diff, summary of improvements, findings

### 3. approve_patch
Approves a pending patch and writes it to a file (HITL).

**Parameters:**
- `patchId` (required) - Patch to approve
- `outputPath` (required) - Where to save patch file

**Returns:** Patch file location and application instructions

### 4. reject_patch
Rejects a pending patch.

**Parameters:**
- `patchId` (required) - Patch to reject

### 5. list_pending_patches
Lists all patches awaiting approval.

**Returns:** Count and summary of all pending patches

### 6. generate_test_command
Generates appropriate test command for detected build system.

**Parameters:**
- `repoPath` (required) - Repository path

**Returns:** Test command snippet

See [MCP_EMBABEL_INTEGRATION.md](MCP_EMBABEL_INTEGRATION.md) for detailed documentation.

---

## 🌱 Embabel Agent

README Wrangler uses the **Embabel agent framework** with Goal-Oriented Action Planning (GOAP).

### Agent Architecture

```java
@Agent(description = "Polish repository README")
public class ReadmePolisherAgent {

    @Action
    @AchievesGoal(description = "Polish README and create patch")
    public PatchBundle polishReadmeGoal(PolishingContext context) {
        // Embabel dynamically plans optimal path to goal
    }

    @Condition
    public boolean shouldAddTestSection(String content, BuildSystem build) {
        // Conditions guide agent behavior
    }
}
```

### Key Benefits

- **Dynamic Planning** - GOAP finds optimal action sequences
- **Conditional Logic** - Adapts behavior based on repository state
- **Extensibility** - Add new actions without modifying existing code
- **LLM Integration** - Can use OpenAI for intelligent decisions

### Actions & Goals

The agent has one main goal and three sub-actions:

- **Main Goal:** `polishReadmeGoal` - Orchestrates the entire polishing process
- **Action:** `scanRepositoryGoal` - Detects build system and files
- **Action:** `lintMarkdownGoal` - Validates markdown syntax
- **Action:** `rewriteMarkdownGoal` - Applies all fixes

See [MCP_EMBABEL_INTEGRATION.md](MCP_EMBABEL_INTEGRATION.md) for architecture details.

---

## ⚙️ Configuration

Configuration is managed via [application.yml](src/main/resources/application.yml):

```yaml
wrangler:
  addTocThresholdHeadings: 4        # Min headings to trigger TOC
  headingStyle: "atx"               # Use # style headings
  codeFenceLanguages:               # Supported languages
    - java
    - bash
    - yaml
  badges:
    enabled: true                   # Enable badge generation
    ci: "GitHub Actions"
    jdk: "21"
  guardrails:
    maxChangesPerFile: 300          # Limit changes per file
    requireMcpApproval: true        # Require approval before apply
```

### Profiles

- **local** - Development profile (verbose logging)
- **cloud** - Production profile for Cloud Foundry

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   README Wrangler                       │
├─────────────────────────────────────────────────────────┤
│  REST API (HTTP)           CLI (Picocli)                │
├─────────────────────────────────────────────────────────┤
│                 PolishingService                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ Orchestrates polishing workflow                  │  │
│  └──────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────┤
│                     Tool Beans                          │
│  ┌───────────┬───────────┬──────────┬──────────────┐   │
│  │ Repo      │ Markdown  │ Text     │ TOC          │   │
│  │ Scanner   │ Linter    │ Rewriter │ Generator    │   │
│  ├───────────┼───────────┼──────────┼──────────────┤   │
│  │ Badge     │ Patch     │          │              │   │
│  │ Tool      │ Builder   │          │              │   │
│  └───────────┴───────────┴──────────┴──────────────┘   │
├─────────────────────────────────────────────────────────┤
│                   Domain Models                         │
│  RepoSnapshot • PatchBundle • PolishingFinding          │
│  ReadmeImprovements • BuildSystem                       │
└─────────────────────────────────────────────────────────┘
```

### Key Components

- **PolishingService** - Orchestrates the polishing workflow
- **RepoScannerTool** - Detects build systems and finds files
- **MarkdownLinterTool** - Validates markdown syntax
- **TextRewriteTool** - Fixes code fences and headings
- **TocTool** - Generates table of contents
- **BadgeTool** - Creates status badges
- **PatchBuilderTool** - Produces unified diffs

---

## 🛠️ Development

### Project Structure

```
ReadmeWrangler/
├── src/
│   ├── main/
│   │   ├── java/com/baskettecase/readmewrangler/
│   │   │   ├── cli/              # Command-line interface
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── domain/           # Domain models
│   │   │   ├── service/          # Business logic
│   │   │   └── tool/             # Polishing tools
│   │   └── resources/
│   │       └── application.yml   # Configuration
│   └── test/                     # JUnit 5 tests
├── fixtures/                     # Test fixtures
│   ├── maven-app/
│   ├── gradle-lib/
│   └── docs-split/
├── pom.xml                       # Maven configuration
├── manifest.yml                  # Cloud Foundry manifest
└── README.md                     # This file
```

### Building

```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Run tests only
mvn test

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 🧪 Testing

The project includes comprehensive JUnit 5 tests:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RepoScannerToolTest

# Run with coverage
mvn clean test jacoco:report
```

### Test Fixtures

Three sample repositories are provided in `/fixtures`:

1. **maven-app** - Maven project missing test section, untagged code blocks
2. **gradle-lib** - Gradle library with setext headings, needs TOC
3. **docs-split** - Multiple markdown files for merge scenarios

---

## 🚀 Deployment

### Cloud Foundry

Deploy to Cloud Foundry using the provided manifest:

```bash
# Build the application
mvn clean package

# Deploy to Cloud Foundry
cf push

# Check status
cf apps

# View logs
cf logs readme-wrangler --recent
```

### Docker (Future)

Docker support is planned for a future release.

---

## 📚 Examples

### Example 1: Polish Maven App

```bash
cd fixtures/maven-app
java -jar ../../target/readme-wrangler-1.0.0-SNAPSHOT.jar polish \
  --write-patch maven-app.diff
```

**Before:**
```markdown
```
mvn clean install
```
```

**After:**
```markdown
```bash
mvn clean install
```

+ Added "How to Run Tests" section
+ Fixed code fence language tags
```

### Example 2: API Usage

```bash
curl -X POST http://localhost:8080/api/v1/polish \
  -H "Content-Type: application/json" \
  -d '{
    "repoPath": "/Users/me/my-project"
  }' | jq '.summary'
```

---

## 📖 How to Run Tests

```bash
# Using Maven
mvn test

# Using Maven wrapper (if available)
./mvnw test

# Run specific test
mvn test -Dtest=PolishingServiceTest
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Code Standards

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Write JUnit 5 tests for all new code
- Use constructor injection over field injection
- Document public APIs with Javadoc

---

## 📄 License

This project is licensed under the **Apache License 2.0**.

```
Copyright 2025 README Wrangler Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- CommonMark for markdown parsing
- Picocli for CLI support
- All contributors and testers

---

**Made with ☕ and 🎯 by the README Wrangler team**
