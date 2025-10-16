# README Wrangler - Quick Reference

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
