# Notiva Developer Documentation

## What This Is

Comprehensive developer documentation for Notiva, an Android reminder app. The documentation enables developers to understand the app's architecture, set up their development environment, and contribute to the codebase.

## Core Value

Developers can quickly understand how the app works and confidently make changes without breaking existing functionality.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Architecture overview document with Mermaid diagrams showing component relationships
- [ ] Setup and build instructions covering environment, dependencies, and first run
- [ ] Feature documentation explaining reminders, notifications, and recurrence logic
- [ ] Database schema documentation with entity relationships and Room usage
- [ ] UI components and screens documentation with screenshots/diagrams
- [ ] Code examples demonstrating key patterns (DI, Room queries, WorkManager)
- [ ] Testing guide covering unit tests, instrumented tests, and manual testing
- [ ] Contribution guidelines with code style, PR process, and commit conventions

### Out of Scope

- User-facing documentation (help articles, FAQ) — this is developer docs only
- API documentation generation (JavaDoc) — focus on conceptual docs with inline examples
- Localization/i18n of documentation — English only
- Video tutorials — written documentation with diagrams

## Context

**Existing App Stack:**
- Java Android application (no Kotlin)
- Room database for persistent storage
- Hilt for dependency injection
- WorkManager for scheduling reminder triggers
- Traditional XML layouts (no Jetpack Compose)
- Material Design components

**App Package:** `com.ava.notiva`

**Key Components:**
- `MainActivity` — displays reminder list
- `UpsertReminderActivity` — create/edit reminders
- `ReminderTriggerWorker` — WorkManager job for firing notifications
- `ReminderModel` — Room entity for reminders
- `ReminderDao` — Room data access
- `BootReceiver` — reschedules reminders after device reboot

**Codebase Map:** See `.planning/codebase/` for detailed analysis

## Constraints

- **Format**: Markdown files with Mermaid diagrams
- **Location**: `docs/` folder in repository root
- **Audience**: Developers with Android experience
- **Accuracy**: Must reflect actual codebase (Java, not Kotlin)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Markdown + Mermaid | Standard, renders on GitHub, version-controlled | — Pending |
| docs/ folder structure | Conventional location, easy to find | — Pending |
| No JavaDoc generation | Conceptual docs more valuable than API reference | — Pending |

---
*Last updated: 2026-02-04 after initialization*
