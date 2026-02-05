# Notiva Developer Documentation

## What This Is

Comprehensive developer documentation for Notiva, an Android reminder app. The documentation enables developers to understand the app's architecture, set up their development environment, and contribute to the codebase.

## Core Value

Developers can quickly understand how the app works and confidently make changes without breaking existing functionality.

## Current State (v1.0 Shipped)

**Documentation Suite:**
- docs/README.md — Navigation hub with badges and "I want to..." pattern
- docs/SETUP.md — Environment setup with 10+ troubleshooting items
- docs/ARCHITECTURE.md — 3-layer MVVM with Mermaid diagrams
- docs/DATABASE.md — Room schema with ER diagram and query examples
- docs/TESTING.md — Test guide with Java templates
- docs/UI.md — Screen documentation with navigation flowchart
- docs/CODE_EXAMPLES.md — 16 source file references for key patterns
- docs/features/REMINDERS.md — CRUD flows with 5 sequence diagrams
- docs/features/NOTIFICATIONS.md — Triggers, channels, 6 permissions
- docs/features/RECURRENCE.md — 7 types, Android constraints
- CONTRIBUTING.md — PR process, code style, commit conventions

**Total:** 6,216 lines of documentation across 11 files

## Requirements

### Validated

- FOUND-01: README.md with project overview, badges, and quick start link — v1.0
- FOUND-02: Setup instructions covering prerequisites, clone, build, and first run — v1.0
- FOUND-03: Architecture overview with Mermaid component diagram showing layers and patterns — v1.0
- FOUND-04: CONTRIBUTING.md with PR process, code style guide, and commit conventions — v1.0
- REF-01: Database schema documentation with Room entities and relationships — v1.0
- REF-02: ER diagram for Room schema using Mermaid — v1.0
- REF-03: Testing guide covering unit tests, instrumented tests, and how to run them — v1.0
- FEAT-01: Reminders feature documentation with CRUD operations and data flow — v1.0
- FEAT-02: Notifications feature documentation with triggers, channels, and permissions — v1.0
- FEAT-03: Recurrence logic documentation with types, scheduling, and WorkManager integration — v1.0
- FEAT-04: Android-specific constraints documented (doze mode, battery optimization, permissions) — v1.0
- FEAT-05: Code examples referencing actual source files for key patterns — v1.0
- UI-01: Screens overview documenting MainActivity and UpsertReminderActivity — v1.0
- UI-02: Navigation flow diagram showing screen transitions using Mermaid — v1.0

### Active

(None — v1.0 complete. Define new requirements for next milestone.)

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
| Markdown + Mermaid | Standard, renders on GitHub, version-controlled | ✓ Good |
| docs/ folder structure | Conventional location, easy to find | ✓ Good |
| No JavaDoc generation | Conceptual docs more valuable than API reference | ✓ Good |
| "I want to..." navigation | Developer-centric access pattern | ✓ Good |
| Feature docs in features/ | Organized structure for growing docs | ✓ Good |
| 10-phase structure | Comprehensive coverage from 14 requirements | ✓ Good |

---
*Last updated: 2026-02-05 after v1.0 milestone*
