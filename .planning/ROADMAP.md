# Roadmap: Notiva Developer Documentation

## Overview

This roadmap delivers comprehensive developer documentation for the Notiva Android reminder app. The journey progresses from establishing the documentation foundation (README, setup, architecture) through reference materials (database, testing) to feature documentation (reminders, notifications, recurrence) and culminates with UI documentation and contribution guidelines. Each phase builds on previous work, establishing vocabulary and mental models that later phases reference.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: Documentation Hub** - Create README.md as central navigation for all documentation ✓
- [x] **Phase 2: Setup Guide** - Document environment setup, prerequisites, and first build ✓
- [ ] **Phase 3: Architecture Overview** - Create architecture documentation with Mermaid diagrams
- [ ] **Phase 4: Database Reference** - Document Room schema with entities and ER diagram
- [ ] **Phase 5: Testing Guide** - Document testing approach, running tests, and writing tests
- [ ] **Phase 6: Reminders Feature** - Document reminder CRUD operations and data flow
- [ ] **Phase 7: Notifications Feature** - Document notification triggers, channels, and permissions
- [ ] **Phase 8: Recurrence and Constraints** - Document recurrence logic and Android-specific constraints
- [ ] **Phase 9: UI Documentation** - Document screens, components, and navigation flow
- [ ] **Phase 10: Code Examples and Contributing** - Add code examples and contribution guidelines

## Phase Details

### Phase 1: Documentation Hub
**Goal**: Developers can discover and navigate to all documentation from a single entry point
**Depends on**: Nothing (first phase)
**Requirements**: FOUND-01
**Success Criteria** (what must be TRUE):
  1. docs/README.md exists with clear project overview
  2. README contains "I want to..." navigation table linking to all planned docs
  3. README displays badges for build status and license
  4. Developer landing on docs/ immediately knows what docs exist and where to find them
**Plans:** 1 plan

Plans:
- [x] 01-01-PLAN.md — Create documentation hub with overview, badges, and navigation table ✓

### Phase 2: Setup Guide
**Goal**: New developers can set up their environment and run the app successfully
**Depends on**: Phase 1
**Requirements**: FOUND-02
**Success Criteria** (what must be TRUE):
  1. docs/SETUP.md exists with complete setup instructions
  2. Prerequisites (Android Studio version, SDK, Java) are clearly listed
  3. Clone, build, and run steps work on a fresh machine
  4. Common setup errors and resolutions are documented
  5. Developer can go from clone to running app in under 15 minutes
**Plans:** 1 plan

Plans:
- [x] 02-01-PLAN.md — Create comprehensive setup guide with prerequisites, build steps, and troubleshooting ✓

### Phase 3: Architecture Overview
**Goal**: Developers understand the app's structure, layers, and component relationships
**Depends on**: Phase 1
**Requirements**: FOUND-03
**Success Criteria** (what must be TRUE):
  1. docs/ARCHITECTURE.md exists with high-level overview
  2. Mermaid component diagram shows layers (UI, ViewModel, Repository, Data)
  3. Key patterns documented (MVVM, Hilt DI, Room, WorkManager)
  4. Component responsibilities are clear (what each layer does)
  5. Developer can draw the architecture on a whiteboard after reading
**Plans:** 1 plan

Plans:
- [ ] 03-01-PLAN.md — Create architecture overview with layer diagram, data flow diagram, and pattern documentation

### Phase 4: Database Reference
**Goal**: Developers understand the data model and can work with Room entities
**Depends on**: Phase 3
**Requirements**: REF-01, REF-02
**Success Criteria** (what must be TRUE):
  1. docs/DATABASE.md exists with schema documentation
  2. ReminderModel entity fields are documented with types and purposes
  3. Room DAO methods are documented with query explanations
  4. Mermaid ER diagram shows entity relationships
  5. Developer can write a new Room query after reading
**Plans**: TBD

Plans:
- [ ] 04-01: Create database documentation

### Phase 5: Testing Guide
**Goal**: Developers can run existing tests and write new ones
**Depends on**: Phase 3
**Requirements**: REF-03
**Success Criteria** (what must be TRUE):
  1. docs/TESTING.md exists with testing approach documentation
  2. Commands to run unit tests and instrumented tests are documented
  3. Test directory structure is explained
  4. Example test cases demonstrate patterns used in codebase
  5. Developer can add a new test after reading
**Plans**: TBD

Plans:
- [ ] 05-01: Create testing guide

### Phase 6: Reminders Feature
**Goal**: Developers understand how reminders work from UI to database
**Depends on**: Phase 4
**Requirements**: FEAT-01
**Success Criteria** (what must be TRUE):
  1. docs/features/REMINDERS.md exists with CRUD documentation
  2. Data flow from UpsertReminderActivity to Room is documented
  3. Reminder lifecycle states are explained
  4. Sequence diagram shows create/edit/delete flows
  5. Developer can trace a reminder creation through the code
**Plans**: TBD

Plans:
- [ ] 06-01: Create reminders feature documentation

### Phase 7: Notifications Feature
**Goal**: Developers understand how notifications are triggered and displayed
**Depends on**: Phase 6
**Requirements**: FEAT-02
**Success Criteria** (what must be TRUE):
  1. docs/features/NOTIFICATIONS.md exists with notification documentation
  2. ReminderTriggerWorker workflow is documented
  3. Notification channels and their purposes are explained
  4. Required permissions (POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM) documented
  5. Developer can debug a notification issue after reading
**Plans**: TBD

Plans:
- [ ] 07-01: Create notifications feature documentation

### Phase 8: Recurrence and Constraints
**Goal**: Developers understand recurrence logic and Android platform constraints
**Depends on**: Phase 7
**Requirements**: FEAT-03, FEAT-04
**Success Criteria** (what must be TRUE):
  1. docs/features/RECURRENCE.md exists with recurrence type documentation
  2. All recurrence types (daily, weekly, monthly, custom) are explained
  3. WorkManager scheduling integration is documented
  4. Android constraints documented (doze mode, battery optimization, reboot handling)
  5. Developer knows why WorkManager was chosen over AlarmManager
**Plans**: TBD

Plans:
- [ ] 08-01: Create recurrence and constraints documentation

### Phase 9: UI Documentation
**Goal**: Developers understand screens, layouts, and navigation patterns
**Depends on**: Phase 6
**Requirements**: UI-01, UI-02
**Success Criteria** (what must be TRUE):
  1. docs/UI.md exists with screens overview
  2. MainActivity and UpsertReminderActivity are documented
  3. Key UI components and their purposes are explained
  4. Mermaid navigation flow diagram shows screen transitions
  5. Developer can add a new screen following existing patterns
**Plans**: TBD

Plans:
- [ ] 09-01: Create UI documentation

### Phase 10: Code Examples and Contributing
**Goal**: Developers have concrete examples and know how to contribute
**Depends on**: Phase 8, Phase 9
**Requirements**: FEAT-05, FOUND-04
**Success Criteria** (what must be TRUE):
  1. Code examples added to feature docs referencing actual source files
  2. CONTRIBUTING.md exists with PR process and code style
  3. Commit message conventions are documented
  4. Documentation contribution process is included
  5. New contributor can submit their first PR following the guide
**Plans**: TBD

Plans:
- [ ] 10-01: Add code examples and create contributing guide

## Progress

**Execution Order:**
Phases execute in numeric order: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 8 -> 9 -> 10

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Documentation Hub | 1/1 | Complete ✓ | 2026-02-04 |
| 2. Setup Guide | 1/1 | Complete ✓ | 2026-02-04 |
| 3. Architecture Overview | 0/1 | Planned | - |
| 4. Database Reference | 0/1 | Not started | - |
| 5. Testing Guide | 0/1 | Not started | - |
| 6. Reminders Feature | 0/1 | Not started | - |
| 7. Notifications Feature | 0/1 | Not started | - |
| 8. Recurrence and Constraints | 0/1 | Not started | - |
| 9. UI Documentation | 0/1 | Not started | - |
| 10. Code Examples and Contributing | 0/1 | Not started | - |

---
*Roadmap created: 2026-02-04*
*Last updated: 2026-02-05*
