# OpenAI API Key Setup

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
