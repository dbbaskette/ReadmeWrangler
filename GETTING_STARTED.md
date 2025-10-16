# Getting Started with README Wrangler

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

```bash
mvn clean install
```

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
