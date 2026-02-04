# Developer Documentation Pitfalls

**Domain:** Developer documentation for Android Java application (Notiva)
**Researched:** 2026-02-04
**Confidence:** HIGH (multiple authoritative sources corroborated)

## Critical Pitfalls

Mistakes that cause documentation to be ignored, abandoned, or actively harmful.

---

### Pitfall 1: Documentation-Code Drift

**What goes wrong:** Documentation describes behavior that no longer matches the codebase. Examples for Notiva: documenting deprecated alarm scheduling when the app now uses WorkManager, showing Kotlin syntax when the codebase is Java, describing a feature that was removed.

**Why it happens:**
- Documentation lives separately from code changes
- No process ties documentation updates to code PRs
- "We'll update docs later" becomes "never"

**Consequences:**
- Developers make changes based on wrong assumptions
- Hours wasted debugging issues that "shouldn't happen"
- Trust in documentation erodes — developers stop reading it
- New contributors are actively misled

**Prevention:**
- Treat docs updates as part of the PR checklist (code change = doc update)
- Put documentation in the same `docs/` folder, version-controlled alongside code
- Include documentation accuracy checks in code review
- Date stamp key sections ("Last verified: 2026-02-04")

**Warning Signs:**
- Documentation mentions APIs/classes that don't exist in grep results
- Code examples fail to compile or run
- Contributors repeatedly ask questions answered (wrongly) in docs

**Phase Mapping:** Must be addressed from Phase 1. Every phase that modifies behavior must include doc updates.

---

### Pitfall 2: Missing the "Why" — Documenting What Without Rationale

**What goes wrong:** Documentation explains what components do, but not why design decisions were made. For Notiva: explaining that `ReminderTriggerWorker` uses WorkManager without explaining why (doze mode, battery optimization, reliability vs AlarmManager).

**Why it happens:**
- Engineers assume context is obvious
- "Why" requires extra effort to articulate
- Original decision-makers leave the project

**Consequences:**
- Refactors break things because devs don't understand constraints
- Repeated debates about decisions already made and resolved
- Architecture slowly degrades as devs work around things they don't understand

**Prevention:**
- Include "Why this approach?" sections in architecture docs
- Document constraints (e.g., "WorkManager chosen because Android doze mode kills AlarmManager")
- Record architectural decision records (ADRs) for significant choices
- When documenting components, ask "what would confuse a new developer?"

**Warning Signs:**
- PRs that undo or conflict with intentional design choices
- Same questions asked repeatedly in issues/discussions
- "Why is this done this way?" comments in code

**Phase Mapping:** Architecture Overview (Phase 1) must include rationale. Each feature doc should explain "why this way."

---

### Pitfall 3: Wrong Abstraction Level — Too High or Too Low

**What goes wrong:** Documentation is either so high-level it's useless ("the app sends notifications") or so detailed it's overwhelming (line-by-line code walkthrough). Neither helps developers actually work on the codebase.

**Why it happens:**
- No clear audience definition
- Writers default to their comfort level
- Mixing overview docs with implementation details

**Consequences:**
- High-level only: Devs still don't know how to actually change things
- Low-level only: Can't see the forest for the trees, hard to maintain
- Mixed levels: Confusing, inconsistent experience

**Prevention:**
- Define explicit abstraction levels for different doc types:
  - Overview: Components and relationships (Mermaid diagrams)
  - Feature docs: How subsystems work together
  - Code examples: Specific patterns to follow
- Keep each document at ONE abstraction level
- Use C4 model thinking: Context > Container > Component > Code

**Warning Signs:**
- Single doc tries to cover everything from architecture to code snippets
- Devs say "I read the docs but still don't understand how to..."
- Diagrams mix high-level boxes with implementation details

**Phase Mapping:** Architecture Overview = high level. Feature docs = medium. Code examples = low level. Maintain separation.

---

### Pitfall 4: Assuming Prior Knowledge

**What goes wrong:** Documentation uses jargon, acronyms, or assumes familiarity without explanation. For Notiva: assuming readers know Hilt, Room, WorkManager, or Android lifecycle without any primer.

**Why it happens:**
- Writers are experts who forget what they once didn't know
- "Everyone knows what DI means" — no, they don't
- Curse of knowledge

**Consequences:**
- New contributors bounce immediately
- Only senior Android devs can onboard
- Creates exclusive rather than inclusive community

**Prevention:**
- Define terms on first use or link to explanations
- Include a "Prerequisites" section stating expected knowledge
- Have someone unfamiliar with the project review docs
- Create a glossary for project-specific terms

**Warning Signs:**
- Acronyms used without expansion (DI, DAO, VM without context)
- Setup guide assumes tools are already installed/configured
- No explanation of Android-specific concepts for general developers

**Phase Mapping:** Setup Guide must be explicit about prerequisites. Architecture docs should link to Android developer docs for concepts.

---

## Moderate Pitfalls

Mistakes that cause frustration and inefficiency but don't completely derail documentation.

---

### Pitfall 5: Unclear Diagrams

**What goes wrong:** Architecture diagrams lack legends, unlabeled arrows, mixed abstraction levels, or missing titles. For Notiva Mermaid diagrams: arrows without labels, colors without meaning, icons without explanation.

**Why it happens:**
- "It's obvious what that arrow means" — it's not
- Diagrams created quickly without polish
- No diagram style guide

**Prevention:**
- Every diagram must have:
  - Title and brief description
  - Legend explaining colors, line styles, icons
  - Labels on all arrows showing relationship type
  - Author and date
- Use consistent styling across all diagrams
- Review diagrams with someone who hasn't seen them

**Warning Signs:**
- Arrows without labels
- Multiple colors with no legend
- Technology icons without identification
- "What does this line mean?" questions

**Phase Mapping:** Establish diagram conventions in Phase 1 (Architecture Overview). All subsequent diagrams follow standard.

**Sources:** [IcePanel - Top 6 mistakes in software architecture diagrams](https://icepanel.io/blog/2023-02-21-top-6-mistakes-in-software-architecture-diagrams)

---

### Pitfall 6: Missing Getting Started / Quick Win

**What goes wrong:** Documentation has no clear entry point for new developers. No "clone, build, run" quick start. For Notiva: no clear path from "I just cloned this repo" to "I made and tested a change."

**Why it happens:**
- Maintainers haven't cloned fresh in months/years
- Assumed knowledge of build tooling
- README is stale or minimal

**Consequences:**
- First impression is frustration
- High bounce rate for potential contributors
- Only people who already know how to set it up can contribute

**Prevention:**
- Getting Started is the most important section — invest heavily
- Test with fresh machine/user periodically
- Include exact commands (not "install Android Studio" but "download from X, install, import project")
- Define success criteria ("You should see the reminder list")

**Warning Signs:**
- Setup instructions have "TODO" or vague steps
- No mention of required SDK versions, API levels
- "Works on my machine" syndrome
- Issues asking how to build/run the project

**Phase Mapping:** Setup Guide (Phase 2) is critical path. Test with fresh environment before finalizing.

**Sources:** [Google Open Source Blog - Building great open source documentation](https://opensource.googleblog.com/2018/10/building-great-open-source-documentation.html)

---

### Pitfall 7: No Code Examples or Broken Examples

**What goes wrong:** Documentation describes concepts but never shows working code, or shows code that doesn't compile/run. For Notiva: explaining Room queries without showing actual DAO patterns from the codebase.

**Why it happens:**
- "The code is in the repo" mindset
- Examples copied without testing
- Examples rot as codebase evolves

**Consequences:**
- Developers can't translate concepts to implementation
- Copy-paste examples that break
- Frustration and distrust

**Prevention:**
- Pull examples from actual codebase (use file references)
- Every concept needs at least one working example
- Test code examples as part of doc review
- For Notiva: reference actual classes like `ReminderDao.java`, `ReminderTriggerWorker.java`

**Warning Signs:**
- "See the code" without specific file/line references
- Examples use different naming than actual codebase
- Java examples in a Kotlin style (or vice versa)
- Import statements missing or wrong

**Phase Mapping:** Feature Documentation (Phase 3) must include tested code snippets. Reference actual files in repo.

---

### Pitfall 8: One-Size-Fits-All Structure

**What goes wrong:** Same document tries to serve everyone: new contributors, maintainers, and casual readers. Results in too much detail for some, not enough for others.

**Why it happens:**
- Desire for completeness
- "Just put it all in one doc"
- No audience segmentation

**Consequences:**
- Documents become unwieldy
- Hard to navigate
- No one reads the whole thing

**Prevention:**
- Segment by audience/purpose:
  - README: Quick overview, links to details
  - Setup Guide: New developers
  - Architecture: Understanding the system
  - Feature Docs: Working on specific areas
  - Contributing: Submitting changes
- Each doc has ONE primary purpose

**Warning Signs:**
- Single document > 2000 lines
- Mixing "how to set up" with "how the alarm system works"
- Table of contents has 20+ items

**Phase Mapping:** Define doc structure in Phase 1. Each phase produces docs for specific purposes.

**Sources:** [Gliffy - Software Architecture Documentation Best Practices](https://www.gliffy.com/blog/architecture-documentation-best-practices)

---

### Pitfall 9: Undiscoverable Documentation

**What goes wrong:** Great docs exist but developers can't find them. No clear navigation, poor naming, no search, buried in unexpected locations.

**Why it happens:**
- Docs added ad-hoc over time
- No information architecture
- Assumed knowledge of where things live

**Consequences:**
- Developers ask questions answered in docs
- Duplicate documentation created
- Effort wasted on docs no one reads

**Prevention:**
- Central index (docs/README.md) with clear navigation
- Consistent naming conventions
- Cross-link related documents
- Logical folder structure (architecture/, features/, guides/)
- README.md in repo root links to docs/

**Warning Signs:**
- Questions in issues that are answered in docs
- Developers saying "I didn't know that doc existed"
- No obvious entry point from repo root

**Phase Mapping:** Establish structure in Phase 1. Every doc must be linked from index.

---

## Minor Pitfalls

Mistakes that cause friction but are easily fixable.

---

### Pitfall 10: Inconsistent Formatting and Style

**What goes wrong:** Different docs use different heading styles, code formatting, terminology. Creates unprofessional appearance and cognitive friction.

**Prevention:**
- Establish style guide before writing (or adopt existing like Google's)
- Use consistent Markdown conventions
- Standardize terminology (e.g., always "reminder" not sometimes "alarm")
- Use linter for Markdown (markdownlint)

**Phase Mapping:** Establish conventions in Phase 1. Apply consistently.

---

### Pitfall 11: Missing Contribution Guidelines

**What goes wrong:** Developers want to contribute docs but don't know how. No guidance on style, process, or expectations.

**Prevention:**
- CONTRIBUTING.md includes documentation section
- Explain doc PR process
- Define acceptable doc formats
- Welcome doc contributions explicitly

**Phase Mapping:** Contribution Guidelines (Phase 4) must cover documentation contributions.

**Sources:** [contributing.md - Best Practices for Maintainers](https://contributing.md/best-practices-for-maintainers-of-open-source-projects/)

---

### Pitfall 12: Ignoring Mobile/Android-Specific Context

**What goes wrong:** Generic documentation advice applied without considering Android specifics. Missing coverage of: lifecycle awareness, background processing constraints, permission handling, device fragmentation.

**Prevention:**
- Feature docs must address Android-specific concerns
- Include API level requirements
- Document permission requirements and runtime handling
- Note device-specific quirks (e.g., manufacturer battery optimization)

**Phase Mapping:** Feature Documentation (Phase 3) must include Android-specific context for notification/alarm behavior.

---

## Phase-Specific Warnings

| Phase | Likely Pitfall | Mitigation |
|-------|---------------|------------|
| Architecture Overview | Wrong abstraction level, unclear diagrams | Use C4 levels, diagram conventions |
| Setup Guide | Assuming prior knowledge, untested steps | Fresh machine test, explicit prerequisites |
| Feature Documentation | Missing "why", code-doc drift | Include rationale, reference actual code |
| Contribution Guidelines | Missing doc contribution guidance | Include documentation process |
| Testing & Refinement | Examples go stale | Periodic verification against codebase |

---

## Anti-Patterns to Explicitly Avoid

| Anti-Pattern | Why It's Tempting | What To Do Instead |
|--------------|-------------------|-------------------|
| "The code is self-documenting" | Less work | Code explains what, docs explain why and how |
| Wall of text | Feels complete | Use headings, bullets, diagrams |
| Generated API docs only | Automated | Conceptual docs explain usage patterns |
| "We'll doc it later" | Ship fast | Doc is part of "done" |
| Copy-paste from other projects | Fast start | Verify matches YOUR codebase |
| Over-documentation | Feels thorough | Focus on high-value areas |

---

## Documentation Health Checklist

Use this checklist to detect pitfalls early:

- [ ] Can a new dev clone and run in < 30 minutes following only written docs?
- [ ] Do all diagrams have titles, legends, and labeled arrows?
- [ ] Is every code example tested and current?
- [ ] Does each doc have ONE clear purpose and audience?
- [ ] Are "why" decisions documented alongside "what"?
- [ ] Is there a clear navigation/index to find all docs?
- [ ] Do docs reference actual file paths that exist in the repo?
- [ ] Has someone unfamiliar with the project reviewed the docs?
- [ ] Is the documentation process part of PR checklist?
- [ ] Are Android-specific constraints (permissions, lifecycle, background limits) documented?

---

## Sources

### HIGH Confidence (Official documentation, authoritative sources)
- [Google Open Source Blog - Building great open source documentation](https://opensource.googleblog.com/2018/10/building-great-open-source-documentation.html)
- [Google Documentation Style Guide - Best Practices](https://google.github.io/styleguide/docguide/best_practices.html)
- [GitHub - Open Source Guides: Best Practices for Maintainers](https://opensource.guide/best-practices/)
- [Mermaid.js Documentation - Syntax Reference](https://mermaid.js.org/intro/syntax-reference.html)

### MEDIUM Confidence (Multiple sources agree, verified patterns)
- [IcePanel - Top 6 mistakes in software architecture diagrams](https://icepanel.io/blog/2023-02-21-top-6-mistakes-in-software-architecture-diagrams)
- [Gliffy - Software Architecture Documentation Best Practices](https://www.gliffy.com/blog/architecture-documentation-best-practices)
- [Document360 - Developer Documentation Mistakes](https://document360.com/blog/developer-documentation-mistakes/)
- [Stack Overflow - Why developers hate documentation](https://stackoverflow.blog/2024/12/19/developers-hate-documentation-ai-generated-toil-work/)
- [contributing.md - Best Practices for Maintainers](https://contributing.md/best-practices-for-maintainers-of-open-source-projects/)
- [Archbee - Developer Documentation Mistakes](https://www.archbee.com/blog/developer-documentation-mistakes)

### LOW Confidence (Single source, community discussion)
- [DEV Community - Reasons Why Developers Hate Your Docs](https://dev.to/goodylili/reasons-why-developers-hate-your-docs-52ge)
- [Qodo.ai - Code Documentation Best Practices 2026](https://www.qodo.ai/blog/code-documentation-best-practices-2026/)
