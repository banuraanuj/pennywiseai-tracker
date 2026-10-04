# PennyWise Phased Roadmap (agent-executable)

> **Put this file at `docs/ROADMAP.md`.**
> Kickoff prompt (the only thing you ever need to paste):
>
> `Read docs/ROADMAP.md and follow its Execution Protocol. Execute the next pending task.`

---

## 0. Run settings (edit these, nothing else needs manual input)

```
RUN_MODE: stage        # single = one task then stop | stage = run until end of current stage then stop | all = run everything, stop only on BLOCKED
WORK_BRANCH: roadmap/work
MAX_FIX_ATTEMPTS: 2    # per failing gate before marking a task BLOCKED
```

---

## 1. Execution Protocol (agent: follow exactly)

1. **Bootstrap (every session).** Read `AGENTS.md`, `CLAUDE.md`, `CONTRIBUTING.md`, `TODO.md`, `.gitmessage`, and `docs/AUDIT.md` if it exists. Read this whole file.
2. **Pick the task.** Find the first task in the Progress Tracker (section 3) whose status is `[ ]`. Never skip ahead, never work on more than one task at a time. If a task is `[!]` (BLOCKED), stop and report it.
3. **Branch.** Ensure you are on `WORK_BRANCH` (create from the current branch if missing). Never commit to `main`. Never force-push. Never rewrite history.
4. **Execute only that task's scope.** If you notice problems outside the scope, do not fix them. Append them to section 6 (Backlog) with file:line and severity.
5. **Verify with gates (section 2).** Run every gate that applies. Fix failures. After `MAX_FIX_ATTEMPTS` failed attempts on the same gate, revert the task's uncommitted changes, set status `[!]`, write the reason in the Run Log, and stop.
6. **Commit.** Small atomic commits following `.gitmessage`. Final commit for the task must include the updated `ROADMAP.md`.
7. **Update this file.** In the same commit: set the task to `[x]`, add a Run Log entry (section 5) with date, task id, what changed, files touched, test evidence, risks, and any product decision needed.
8. **Continue or stop** according to `RUN_MODE`:
   - `single`: stop after one task.
   - `stage`: continue to the next task only if it belongs to the same stage; otherwise stop and print a Stage Summary.
   - `all`: continue until everything is `[x]` or a task is `[!]`.
9. **Stage Summary** (printed at every stop): tasks completed, gates status, open risks, backlog additions, and the next pending task id.

### Global rules (apply to every task)

- Privacy: no new network calls, analytics, telemetry, crash reporters, or new permissions beyond what a task explicitly says. All insights are computed locally from existing Room data. The README Privacy section must remain true.
- Do not break: SMS parsing, rules engine, backup/restore (`.pennywisebackup`), Pro/entitlement gating, Play/F-Droid flavors, widgets, biometric lock.
- Any Room schema change requires a real `Migration` plus a migration test. No destructive migration. Backup/restore and CSV export must include new fields.
- All user-facing text in string resources (Crowdin-compatible). Material 3 only, no hardcoded colors. Keep AGPL v3 license intact.
- No big-bang rewrites. Prefer reuse of existing composables, use cases, and repositories.
- If something is ambiguous, state the assumption in one line in the Run Log and continue. Only stop for a genuine product decision that cannot be reasonably assumed (log it under "Needs decision" and mark the task `[!]`).
- Pure logic (calculations, detection, matching) must live in testable non-UI classes with unit tests.

---

## 2. Gates

| Gate | Command | When |
|---|---|---|
| G1 Env + fast tests | `./init.sh` | every task |
| G2 Unit tests | `./gradlew test` | every task |
| G3 Lint | `./gradlew lint` | every task that touches code |
| G4 Build | `./gradlew assembleDebug` | every task that touches code |
| G5 Instrumented/UI | `./gradlew connectedDebugAndroidTest` | only if a device/emulator is available; otherwise note "skipped: no device" in the Run Log |
| G6 Migration test | Room migration test for the changed schema | any task that changes the schema |
| G7 Docs-only | none of the above required, but the diff must contain no source changes | Stage A tasks |

A task is done only when all applicable gates pass.

---

## 3. Progress Tracker (single source of truth)

Legend: `[ ]` pending, `[x]` done, `[!]` blocked

### Stage A: Audit (docs only)
- [x] T01 Map the app
- [x] T02 Find the glitches
- [x] T03 Find the UX friction

### Stage B: Stability and performance
- [ ] T04 Fix wrong-data bugs (P0)
- [ ] T05 Performance and recomposition
- [ ] T06 State restoration and safe actions
- [ ] T07 Loading, empty and error states
- [ ] T08 Layout, accessibility, robustness

### Stage C: Intuitive UI
- [ ] T09 Navigation and Home
- [ ] T10 Global period/currency and drill-down
- [ ] T11 Transaction list and detail

### Stage D1: Core missing use cases
- [ ] T12 Needs Review inbox
- [ ] T13 Duplicates and internal transfers
- [ ] T14 Merchant cleanup
- [ ] T15 Safe-to-spend and month-end forecast
- [ ] T16 Credit cards and bills
- [ ] T17 Insights engine

### Stage D2: Extended use cases
- [ ] T18 Subscriptions and recurring
- [ ] T19 Budgets
- [ ] T20 Accounts and net worth
- [ ] T21 Splits, notes, tags, reimbursables
- [ ] T22 Reports and sharing
- [ ] T23 Fast capture
- [ ] T24 Notifications
- [ ] T25 On-device AI assistant

### Stage E: Final gate
- [ ] T26 QA and release readiness

---

## 4. Task Definitions

### Stage A: Audit (docs only; gate G7)

#### T01 Map the app (DONE; kept for reference)
Create `docs/AUDIT.md` containing: (1) text diagram of screens, entry points, back-stack; (2) table of every Room entity/column with "shown in UI? (where)" and "used in any calculation?"; (3) a ranked list "Stored but never surfaced". Cite file paths.

#### T02 Find the glitches
Append "Glitch Inventory" to `docs/AUDIT.md`. Review Compose UI and ViewModels for:
- unstable LazyColumn keys / missing `contentType`; excess recomposition; `collectAsState` vs `collectAsStateWithLifecycle`; duplicate flow emissions; heavy main-thread work
- missing Room indexes for filters/sorts in use; non-paginated lists
- state lost on rotation/process death/theme/locale change
- double-tap navigation, double-submit, race conditions on save/delete
- IME/edge-to-edge/cutout issues; flicker or layout jumps on first load; chart animation restarts
- timezone/DST/month-boundary bugs; mixed-currency totals and rounding; locale number grouping (lakh/crore)
- missing empty/error/loading/permission-denied states; accessibility gaps
For each: file:line, one-line impact, severity P0 (wrong data/crash) / P1 (visible glitch) / P2 (polish).

#### T03 Find the UX friction
Append "UX Friction" to `docs/AUDIT.md`. Walk through and count taps/confusion for: first run + SMS permission; seeing today's spend; fixing a wrong category; searching a past transaction; creating a budget; checking subscriptions; checking an account balance; exporting data. Describe behavior with: permission denied, 10k+ historical SMS, zero parsed SMS, parsing in progress. End with the 10 highest-impact improvements, ordered.

### Stage B: Stability and performance

#### T04 Fix wrong-data bugs (P0)
Fix every P0 in `docs/AUDIT.md`: period/date boundaries, timezone/DST, currency conversion and rounding, mixed-currency totals, aggregation errors, number formatting. Each fix gets a unit test that fails before and passes after. No UI styling changes. Mark items resolved in AUDIT.md.

#### T05 Performance and recomposition
Fix AUDIT.md performance items: stable keys + contentType in lazy lists, `@Stable`/`@Immutable` models, `remember`/`derivedStateOf`, `collectAsStateWithLifecycle`, `stateIn(WhileSubscribed)`, `distinctUntilChanged`, moving aggregation off main thread, Room indexes (with migration + test), pagination (Paging 3 or windowed queries) for the transaction list. Measure recomposition counts or frame timings on Home, Transactions, Analytics, Budget before/after and record numbers in the Run Log. Verify with 20k seeded transactions (create the debug-only seeder here if it does not exist; reuse it in T26).

#### T06 State restoration and safe actions
UI state survives rotation, process death, theme and locale change (`rememberSaveable`, `SavedStateHandle`): filters, selected period, scroll position, in-progress forms. Debounce navigation and submit/delete. Replace confirm dialogs for reversible actions (delete, recategorize) with Undo snackbars. ViewModel state tests.

#### T07 Loading, empty and error states
Shared skeleton/shimmer, empty state (explanation + primary action), and error state with retry across Home, Transactions, Analytics, Budgets, Accounts, Subscriptions. Handle SMS permission denied, zero transactions, and first-time import with a non-blocking progress indicator. Reuse shared composables.

#### T08 Layout, accessibility, robustness
Edge-to-edge and cutouts, IME insets, font scale to 1.3x, RTL, small screens, tablets/foldables (adaptive where cheap), contentDescription on icons and charts, 48dp touch targets, contrast in light/dark, TalkBack order. Add Compose UI or screenshot checks for the worst offenders.

### Stage C: Intuitive UI

#### T09 Navigation and Home
In 10 lines in the Run Log, justify, then implement: primary navigation with at most 5 destinations; secondary items to Settings or contextual menus. Home answers in 3 seconds: (1) spend today/this month vs budget, (2) what needs attention, (3) what changed vs last period. Prioritized stack of cards; only show cards that have data. Keep every existing feature reachable. Update strings and tests.

#### T10 Global period/currency and drill-down
Selected period and display currency are global, persistent, and visible at the top of main screens. Every number is tappable (totals, chart bars/slices, category rows, budget bars) and opens the transaction list pre-filtered to exactly what produced it. Test that list totals equal the tapped number.

#### T11 Transaction list and detail
List: sticky date headers with daily totals, inline search + filter chips (category, merchant, account, type, amount range, date), swipe actions, long-press multi-select with bulk categorize/delete (Undo). Detail: show all captured data (raw SMS, bank, account last4, balance after, currency), one-tap "Fix category" and "Apply to all similar" (creates a rule). Stay performant at 20k rows.

### Stage D1: Core missing use cases

#### T12 Needs Review inbox
Queue for: uncategorized, low-confidence/partially parsed SMS, unknown merchants, failed parses. Count card/badge on Home. Swipe to resolve, bulk categorize, "apply to similar". Add a minimal review-status column only if required (migration + test). Tests for queue-selection logic.

#### T13 Duplicates and internal transfers
Detect (a) probable duplicates (same amount, near timestamp, same/similar merchant, different sender) and (b) internal transfers and credit-card bill payments (matching debit/credit across tracked accounts or payment to a known card). Confirmed transfers are excluded from income/expense totals. Suggestions appear in Needs Review with confirm/undo. Thresholds are named constants. Test edge cases (same-day same-amount legitimate purchases, timezone).

#### T14 Merchant cleanup
Conservatively normalize noisy merchant strings (prefixes, order ids, trailing digits). Rename once with optional retroactive apply, merge aliases, convert a merge into a Smart Rule. Preview affected transactions before applying. Tests with realistic Indian UPI/card strings.

#### T15 Safe-to-spend and month-end forecast
"Safe to spend" card from balances, upcoming recurring items/subscriptions, average daily spend, typical income timing. Show safe-to-spend per day, projected month-end balance, and a "how this is calculated" explainer. Hide or explain when balance data is missing. Pure functions, fully unit-tested.

#### T16 Credit cards and bills
Per-card outstanding, available limit, utilization, statement/due date, minimum due where SMS provides them. Separate cards from bank accounts in net-worth math. Local due-date reminders (WorkManager/AlarmManager) with user toggles. Show what is missing instead of guessing.

#### T17 Insights engine
Rule-based, explainable (no LLM): month-over-month and same-period comparisons, biggest category movers, unusually large transactions vs the user's baseline, new merchants, subscription price changes, budget spend velocity, savings rate. Each insight is tappable (opens underlying transactions), dismissible, and its type can be muted. Cap to a few ranked by relevance. Unit tests per generator.

### Stage D2: Extended use cases

#### T18 Subscriptions and recurring
Upcoming-charges timeline, total monthly/yearly cost, price-change alerts, "possibly cancelled/unused" detection, mark false positives, distinguish bill vs subscription, link to transaction history.

#### T19 Budgets
Create a budget from the last 3 months' actuals (suggested amounts), optional rollover, threshold notifications at 50/80/100%, category progress cards on Home, end-of-month recap. Existing Limit/Target/Expected types keep working. Tests for period and rollover math.

#### T20 Accounts and net worth
Net worth across accounts with balance-history chart, low-balance alerts, stale-balance warning (no SMS for N days), per-account cash-flow summary. Respect multi-currency and card-vs-bank separation. Handle accounts with no balance data.

#### T21 Splits, notes, tags, reimbursables
Split a transaction across categories; notes; tags; "to be reimbursed / shared" flag with outstanding-amount tracker. Schema change: Migration + migration test; update backup/restore and CSV export; ensure analytics use split amounts correctly.

#### T22 Reports and sharing
Monthly/annual summary screen, locally generated shareable image or PDF, tax-oriented CSV filters, "year in review". Everything generated on device. Respect existing Pro limits for export.

#### T23 Fast capture
Quick-add from a home-screen widget and a notification action, "repeat last", recent merchant/category suggestions in the manual-entry form. Minimize taps; validate amount, date, currency.

#### T24 Notifications
Centralize local notifications: channels, per-type toggles, quiet hours. Types: new transaction needing category, large transaction, budget thresholds, bill due, weekly digest. Grouped and actionable (e.g., categorize from the notification). Handle `POST_NOTIFICATIONS` runtime permission on Android 13+.

#### T25 On-device AI assistant
Feed the LLM pre-aggregated structured context from SQL (never let the model do arithmetic), show the transactions behind each answer, suggested prompts based on the user's real data, graceful handling of "model not downloaded", low RAM, and cancelled generation. Keep the model download opt-in and privacy claims accurate.

### Stage E: Final gate

#### T26 QA and release readiness
Compose UI tests for critical journeys: first run, permission denied, Needs Review resolve, add/edit/delete with Undo, create rule from a transaction, budget creation, period/currency switch. Ensure the debug-only seed generator covers large, mixed-currency, and edge-case data. Walk the matrix (Android 8/12/14+, phone and tablet, light/dark, font 1.3x, RTL, airplane mode, 20k transactions) and record results. Update README features, add a CHANGELOG entry with before/after performance numbers, and list known remaining issues.

---

## 5. Run Log (agent appends newest at the bottom)

Entry template:

```
### YYYY-MM-DD  T## <title>  [DONE | BLOCKED]
- Changes:
- Files touched:
- Gates: G1 ✓ G2 ✓ G3 ✓ G4 ✓ G5 (skipped: no device) G6 n/a
- Evidence (tests added / metrics before-after):
- Assumptions:
- Risks / Needs decision:
```

### Log

### 2025-02-24  T03 Find the UX friction  [DONE]
- Changes: appended "UX Friction" to `docs/AUDIT.md`. Walked through user journeys (First run, Today's spend, Budgets, Subscriptions) and calculated tap counts. Highlighted onboarding scan block and empty states. Ended with the top 10 highest-impact improvements ordered by priority.
- Files touched: `docs/AUDIT.md`
- Gates: G7 ✓ (Docs-only)
- Evidence (tests added / metrics before-after): N/A, documentation audit only.
- Assumptions: Navigating flows via code inspection adequately maps to true user tap counts.
- Risks / Needs decision: None.

### 2025-02-24  T02 Find the glitches  [DONE]
- Changes: appended "Glitch Inventory" to `docs/AUDIT.md` mapping all glitches identified in Compose UI and ViewModels (performance bottlenecks, double-tap navs, TZ/date bugs, missing indexes/empty states).
- Files touched: `docs/AUDIT.md`
- Gates: G7 ✓ (Docs-only)
- Evidence (tests added / metrics before-after): N/A, documentation audit only.
- Assumptions: Assuming the timezone issue stems from `LocalDate.now()` not using injected clocks consistently, flagged as P0 data bug.
- Risks / Needs decision: None.

### (earlier)  T01 Map the app  [DONE]
- Changes: created docs/AUDIT.md (screen map, data inventory, "stored but never surfaced" list).

---

## 6. Backlog (out-of-scope findings; agent appends, never fixes unprompted)

| Found during | File:line | Severity | Note |
|---|---|---|---|

---

## 7. Needs decision (human input only when a task is `[!]`)

| Task | Question | Options the agent proposes | Default if no answer |
|---|---|---|---|
