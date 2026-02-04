# Technology Stack: Developer Documentation

**Project:** Notiva Developer Documentation
**Researched:** 2026-02-04
**Scope:** Stack dimension for developer documentation for a Java Android app

## Recommended Stack

### Core Documentation Format

| Technology | Version | Purpose | Why | Confidence |
|------------|---------|---------|-----|------------|
| Markdown (CommonMark) | N/A | Primary authoring format | Universal, renders natively on GitHub, version-controllable, low barrier to contribution. GitHub renders Markdown files automatically without any build step. | HIGH |
| Mermaid | Latest | Diagrams in documentation | Native GitHub rendering since Feb 2022, text-based (version-controllable), supports flowcharts, sequence diagrams, ER diagrams, class diagrams - all needed for Android architecture docs. No external tooling required. | HIGH |

### Documentation Generator (Optional)

| Technology | Version | Purpose | Why | Confidence |
|------------|---------|---------|-----|------------|
| MkDocs Material | Latest (9.x) | Static site generation | Best-in-class for docs-as-code. Built-in search, dark/light mode, mobile-responsive, Mermaid integration, 50,000+ organizations use it. MIT licensed. | HIGH |

**Recommendation:** Start with raw Markdown + Mermaid in `docs/` folder. GitHub renders this natively with zero setup. Add MkDocs Material later ONLY IF you need:
- Full-text search across documentation
- Professional hosted website (beyond GitHub repo browsing)
- Navigation sidebar with hierarchy
- Social cards for link sharing

For Notiva (single Android app, developer audience familiar with GitHub), raw Markdown is sufficient.

### Linting and Quality

| Tool | Purpose | Why | When to Use | Confidence |
|------|---------|-----|-------------|------------|
| markdownlint-cli2 | Markdown formatting consistency | Enforces CommonMark compliance, catches broken links, validates structure. VS Code extension available. | Always - catches issues before commit | MEDIUM |
| Vale (optional) | Prose style/grammar | Enforces writing style guides (Google, Microsoft). Overkill for developer docs, useful for user-facing content. | Only if docs grow large or have multiple contributors | LOW |

### Hosting

| Option | Cost | Best For | Why | Confidence |
|--------|------|----------|-----|------------|
| GitHub (repo browsing) | Free | Simple projects | Zero setup. Markdown + Mermaid render automatically. This is the recommended approach. | HIGH |
| GitHub Pages | Free | Professional site | Auto-deploys from repo. Works with MkDocs Material via GitHub Actions. | HIGH |
| Read the Docs | Free tier | Open source | Alternative to GitHub Pages with built-in versioning. | MEDIUM |

**Recommendation:** Use GitHub repository browsing (docs/ folder in repo). This is already where developers will be - no extra hosting needed. Add GitHub Pages only if you want a separate documentation website.

### VS Code Extensions (Development)

| Extension | Purpose | Why |
|-----------|---------|-----|
| Markdown All in One | Authoring experience | Table formatting, TOC generation, preview |
| Mermaid Preview | Diagram preview | Live preview while editing Mermaid diagrams |
| markdownlint | Linting in editor | Inline warnings for style violations |
| Markdown Preview Mermaid Support | Combined preview | Renders Mermaid in VS Code's Markdown preview |

## What NOT to Use

| Technology | Why Not | Use Instead |
|------------|---------|-------------|
| Docusaurus | Requires Node.js/React knowledge, overkill for single-app docs, complex setup | MkDocs Material or raw Markdown |
| GitBook | Closed-source, paid for teams, adds lock-in | GitHub + MkDocs Material |
| Confluence/Notion | Not version-controlled, separate from code, drift risk | Markdown in repo |
| JavaDoc generation | You explicitly scoped this out; conceptual docs > API reference | Inline code examples in Markdown |
| AsciiDoc | Less ecosystem support than Markdown, GitHub renders but Mermaid integration weaker | Markdown |
| reStructuredText | Python-centric, steeper learning curve | Markdown |
| Draw.io/Lucidchart | Binary files, not version-controllable, requires separate tool | Mermaid diagrams |

## Mermaid Diagram Types for Android Docs

Based on Notiva's architecture, these Mermaid diagram types are most useful:

| Diagram Type | Use Case | Example in Notiva |
|--------------|----------|-------------------|
| Flowchart (`flowchart TD`) | Data flow, user flows | Reminder creation flow, notification trigger flow |
| Sequence Diagram (`sequenceDiagram`) | Component interactions | Activity -> ViewModel -> Repository -> DAO |
| Class Diagram (`classDiagram`) | Class relationships | ReminderModel, RecurrenceType relationships |
| ER Diagram (`erDiagram`) | Database schema | Room entities (ReminderModel) |
| State Diagram (`stateDiagram-v2`) | State machines | Reminder states (active, triggered, snoozed, dismissed) |

**Mermaid Best Practices:**
- Keep diagrams simple (max 10-15 nodes)
- Use meaningful labels, not abbreviations
- Add comments with `%%` for maintenance
- Test in [Mermaid Live Editor](https://mermaid.live/) before committing
- Break complex flows into multiple diagrams

## File Structure Recommendation

```
docs/
  README.md              # Documentation index/landing page
  ARCHITECTURE.md        # System overview with Mermaid diagrams
  SETUP.md               # Development environment setup
  FEATURES.md            # Feature documentation (reminders, notifications, recurrence)
  DATABASE.md            # Room schema and data model
  COMPONENTS.md          # UI components and screens
  TESTING.md             # Test guide (unit, instrumented, manual)
  CONTRIBUTING.md        # Contribution guidelines
  images/                # Screenshots (PNG, keep small)
```

## Markdown Conventions

### Recommended Style

```markdown
# Top-Level Heading (document title only)

## Major Section

### Subsection

**Bold** for emphasis
`inline code` for code references
```java (fenced code blocks with language)

| Column | Column |  (tables for structured data)
|--------|--------|

- Bullet lists for unordered items
1. Numbered lists for ordered steps

> Blockquotes for callouts/notes
```

### Link Strategy

- Use **relative links** between docs: `[Setup Guide](./SETUP.md)`
- Use **absolute links** for external resources
- Link to source files where helpful: `[ReminderModel.java](../app/src/main/java/com/ava/notiva/model/ReminderModel.java)`

## CI/CD Integration (Optional)

If you want automated quality checks:

```yaml
# .github/workflows/docs.yml
name: Documentation Checks
on: [push, pull_request]
jobs:
  lint:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: DavidAnson/markdownlint-cli2-action@v19
        with:
          globs: 'docs/**/*.md'
```

This runs markdownlint on all documentation files for every PR.

## Installation Commands

If using MkDocs Material (optional, not recommended initially):

```bash
# Install MkDocs with Material theme
pip install mkdocs-material

# Install Mermaid plugin for MkDocs
pip install mkdocs-mermaid2-plugin

# Serve locally
mkdocs serve

# Build static site
mkdocs build
```

If using markdownlint (recommended):

```bash
# Install globally
npm install -g markdownlint-cli2

# Run on docs folder
markdownlint-cli2 "docs/**/*.md"
```

## Alternatives Considered

| Category | Recommended | Alternative | Why Not Alternative |
|----------|-------------|-------------|---------------------|
| Format | Markdown | AsciiDoc | Less GitHub integration, steeper learning curve |
| Diagrams | Mermaid | PlantUML | Requires Java runtime, more complex syntax |
| Diagrams | Mermaid | Draw.io | Binary files, not version-controllable |
| Generator | None (raw MD) | Docusaurus | Overkill for single-project docs |
| Generator | MkDocs Material | Hugo | Hugo is faster but less documentation-focused |
| Hosting | GitHub repo | Netlify | Extra complexity for no benefit |
| Linting | markdownlint | textlint | markdownlint is simpler, more focused |

## Summary Recommendation

**For Notiva developer documentation:**

1. **Write in Markdown** - Universal, renders on GitHub
2. **Use Mermaid for diagrams** - Native GitHub support, text-based
3. **Store in `docs/` folder** - Conventional, discoverable
4. **No static site generator initially** - GitHub renders docs automatically
5. **Add markdownlint** - Catch formatting issues early
6. **Consider MkDocs Material later** - Only if you need search or a standalone site

This stack minimizes tooling overhead while maximizing compatibility with the existing Android development workflow. Developers can contribute docs using the same Git workflow they use for code.

## Sources

### HIGH Confidence (Official Documentation)
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) - Official site
- [MkDocs](https://www.mkdocs.org/) - Official site
- [GitHub Blog: Mermaid Diagrams in Markdown](https://github.blog/developer-skills/github/include-diagrams-markdown-files-mermaid/) - Official announcement
- [Mermaid Syntax Reference](https://mermaid.js.org/intro/syntax-reference.html) - Official docs
- [GitHub Pages Documentation](https://docs.github.com/en/pages) - Official docs
- [markdownlint GitHub](https://github.com/DavidAnson/markdownlint) - Official repo

### MEDIUM Confidence (Multiple Sources Agree)
- [GitBook vs Docusaurus 2026 Comparison](https://www.gitbook.com/comparison/gitbook-vs-docusaurus) - Vendor comparison
- [MkDocs Material GitHub](https://github.com/squidfunk/mkdocs-material) - 50K+ stars validation
- [Write the Docs: Docs as Code](https://www.writethedocs.org/guide/docs-as-code/) - Community standard

### LOW Confidence (WebSearch Only - Validate Before Acting)
- Tool popularity rankings from aggregator sites
- Specific version numbers (verify against official releases)
