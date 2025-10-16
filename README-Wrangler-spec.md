# README Wrangler — an Embabel + Spring Boot doc polisher

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

---

## 2) Triggers & Inputs

- **Local CLI**: `./readme-wrangler polish --path /repo`
- **HTTP webhook**: `POST /events/readme-updated` with repo URL / artifact location
- **CI job**: stage artifacts (`README.md`, `/docs/**/*.md`, test reports, scripts) and call `POST /ci/polish`

**Artifacts the agent may read:**
- Markdown files: `README.md`, `/docs/**/*.md`, `CONTRIBUTING.md`, `CHANGELOG.md`
- Test hints: `pom.xml` or `build.gradle`, `mvnw`, `gradlew`, `Makefile`, `./scripts/test*`, GitHub Actions workflows
- Project metadata: `package.json`, `settings.gradle`, `LICENSE*`

---

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

---

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

---

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

---

## 6) Embabel Planning (example plan)

1. **Lint** README → collect issues.
2. **Scan repo** for test commands → derive “How to run tests” snippet if missing.
3. **Rewrite** sections: tighten intro, fix code fences, normalize headings.
4. **Insert TOC** when warranted.
5. **Update badges** (optional, guard behind a flag).
6. **Build patch** → `PatchBundle`.
7. **HITL**: Require approval via MCP `ApprovePatchTool`.
8. **Apply** (open a PR or write to a working branch).

---

## 7) HTTP & CLI Surface

- `POST /polish` → body: `{ "repoPath" | "repoUrl", "files": [...optional...] }` → returns `PatchBundle` (diff + summary).
- `POST /approve` → applies latest patch (requires capability + target branch).
- CLI: 
  - `readme-wrangler polish --path . --write-patch out.diff`
  - `readme-wrangler apply --patch out.diff --branch docs/polish`

---

## 8) HITL via MCP (Embabel)

Expose the following **MCP tools** so a human can step in from Claude Desktop (or any MCP client):

- `ApprovePatchTool(repo, patchId) -> {appliedBranch}`
- `MergeMarkdownsTool(repo, sources[]) -> {mergedFile, notes}`
- `DedupeHeadingsTool(file) -> {diff}`
- `FormatRepoTool(repo, mode=markdown|code|both) -> {report, diff}`

Use these to demo a **project-wide cleanup**: dedupe duplicate `README` sections across `/docs`, merge small How-Tos into a single “Guides.md,” and normalize heading levels.

---

## 9) Guardrails & Policies

- Never push to `main`; use `docs/polish/<timestamp>` branch.
- Max line change per file (configurable) to avoid rewriting entire docs unexpectedly.
- Keep a **“before/after”** block for each major change in the PR description.
- All AI rewrites **must** preserve code semantics (no code content changes beyond fences/format).

---

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

---

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

---

## 12) Acceptance Criteria

- Given a README lacking a test section, running `polish` adds a correct, runnable snippet for the detected build system.
- Broken or untagged code fences are normalized and language-tagged.
- Headings are normalized to a consistent hierarchy; optional TOC appears when > N headings.
- Output is a **unified diff**; no direct file mutations without approval.
- CI demo: on push to `docs/**` or `README.md`, job comments the proposed patch on the PR and awaits MCP approval to apply.

---

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

---

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

---

## 15) Nice-to-haves (phase 2)
- Detect language per code fence; add language tag automatically.
- Generate “Contributing” preface if repo is public and missing CONTRIBUTING.md.
- Auto-badge test coverage if Jacoco/Coverage artifact is present.
- Summarize major changes as a one-line PR title using Conventional Commits.

---

## 16) License & Ownership
- Apache-2.0 (default). Include `NOTICE` and a short rationale in README.

---

## 17) Quick Naming Rationale
- **README Wrangler**: evokes “tidy, organize, herd the docs,” playful without being frivolous.
- **Markdown Mechanic**: conveys “fix what’s broken.”
- **Readme Rodeo**: fun, demo-friendly.

Pick your favorite; the spec uses **README Wrangler** as the working name.
