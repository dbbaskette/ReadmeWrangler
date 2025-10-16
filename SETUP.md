# 🚀 Setup Guide

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

---

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

---

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

---

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

---

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

---

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

---

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

---

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

---

## Next Steps

- Read [MCP_EMBABEL_INTEGRATION.md](MCP_EMBABEL_INTEGRATION.md) for architecture details
- See [QUICK_REFERENCE.md](QUICK_REFERENCE.md) for command cheat sheet
- Try the CLI: `java -jar target/readme-wrangler-*.jar polish /path/to/repo`
- Configure Claude Desktop to use the MCP server

---

## Security Notes

⚠️ **Never commit `.env` to Git!**
- The `.env` file is in `.gitignore`
- Use `.env.example` for sharing configuration templates
- For Cloud Foundry, set `OPENAI_API_KEY` as a service credential

✅ **Monitor API Usage:**
- Check usage at [platform.openai.com/usage](https://platform.openai.com/usage)
- Set spending limits in OpenAI account settings
- Typical cost: $0.01-0.05 per polish operation
