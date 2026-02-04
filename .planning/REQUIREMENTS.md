# Requirements: Notiva Developer Documentation

**Defined:** 2026-02-04
**Core Value:** Developers can quickly understand how the app works and confidently make changes without breaking existing functionality.

## v1 Requirements

Requirements for initial release. Each maps to roadmap phases.

### Foundation

- [x] **FOUND-01**: README.md with project overview, badges, and quick start link ✓
- [x] **FOUND-02**: Setup instructions covering prerequisites, clone, build, and first run ✓
- [ ] **FOUND-03**: Architecture overview with Mermaid component diagram showing layers and patterns
- [ ] **FOUND-04**: CONTRIBUTING.md with PR process, code style guide, and commit conventions

### Reference

- [ ] **REF-01**: Database schema documentation with Room entities and relationships
- [ ] **REF-02**: ER diagram for Room schema using Mermaid
- [ ] **REF-03**: Testing guide covering unit tests, instrumented tests, and how to run them

### Features

- [ ] **FEAT-01**: Reminders feature documentation with CRUD operations and data flow
- [ ] **FEAT-02**: Notifications feature documentation with triggers, channels, and permissions
- [ ] **FEAT-03**: Recurrence logic documentation with types, scheduling, and WorkManager integration
- [ ] **FEAT-04**: Android-specific constraints documented (doze mode, battery optimization, permissions)
- [ ] **FEAT-05**: Code examples referencing actual source files for key patterns

### UI

- [ ] **UI-01**: Screens overview documenting MainActivity and UpsertReminderActivity
- [ ] **UI-02**: Navigation flow diagram showing screen transitions using Mermaid

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### Enhancements

- **ENH-01**: Troubleshooting guide for common issues
- **ENH-02**: Architecture Decision Records (ADRs) for key choices
- **ENH-03**: Onboarding checklist for new contributors
- **ENH-04**: Glossary of Android/Hilt/Room/WorkManager terms
- **ENH-05**: Screenshots for UI documentation
- **ENH-06**: MkDocs Material site generation for search and navigation

## Out of Scope

| Feature | Reason |
|---------|--------|
| JavaDoc/API generation | Conceptual docs with inline examples more valuable |
| User-facing docs (FAQ, help) | This is developer documentation only |
| Video tutorials | Written docs with diagrams sufficient for developers |
| Localized documentation | English only for v1 |
| Automated diagram generation | Manual Mermaid diagrams are sufficient |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| FOUND-01 | Phase 1 | Complete |
| FOUND-02 | Phase 2 | Complete |
| FOUND-03 | Phase 3 | Pending |
| FOUND-04 | Phase 10 | Pending |
| REF-01 | Phase 4 | Pending |
| REF-02 | Phase 4 | Pending |
| REF-03 | Phase 5 | Pending |
| FEAT-01 | Phase 6 | Pending |
| FEAT-02 | Phase 7 | Pending |
| FEAT-03 | Phase 8 | Pending |
| FEAT-04 | Phase 8 | Pending |
| FEAT-05 | Phase 10 | Pending |
| UI-01 | Phase 9 | Pending |
| UI-02 | Phase 9 | Pending |

**Coverage:**
- v1 requirements: 14 total
- Mapped to phases: 14
- Unmapped: 0

---
*Requirements defined: 2026-02-04*
*Last updated: 2026-02-04 after roadmap creation*
