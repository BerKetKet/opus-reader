# AGENTS.md — Opus Reader (new project)

Personal Android EPUB reader, not published. Its UI is a faithful copy of the Kindle Android app, with our own content. The render engine is separate work and plugs in later through `:engine`.

## Source of truth
docs/ARCHITECTURE.md, docs/ROADMAP.md, docs/INTERACTION_SPEC.md, docs/CANONICAL_SCREENS.md, docs/reference/ and docs/kindle_audit/ (reference screenshots, 1080x2340 at 420 dpi), docs/kindle_res/ (values decoded from the Kindle app), docs/LESSONS.md (what went wrong in the first attempt; read it).

## Order of work
Behaviour first, skin second. Build vertical slices (docs/ROADMAP.md). Each slice delivers data, ViewModel, UI, gestures, persistence and tests, using design-system components. Visual fidelity is measured on every slice and enforced in slice S11. Never start a slice before the previous one passes its gates.

## Architecture rules (hard)
- Modules: `:design` (tokens, fonts, components), `:engine` (ReaderEngine API, page model, fake engine), `:data` (Room, repositories, EPUB import, catalog), `:app` (features, navigation, DI). `:design` and `:engine` never depend on `:data`.
- Unidirectional state. Each feature has a ViewModel exposing one UiState and taking events. Composables are stateless: state in, callbacks out. Ephemeral UI state only (scroll, focus) may live in composables.
- No default no-op callbacks (`= {}`) on screen or component parameters. Every callback is supplied.
- Feature composables never import `:data`. Manual DI through `AppContainer`. Room and DataStore for persistence. Reading positions are `Locator(spineIndex, charOffset)`, never page numbers.
- The reader talks only to `ReaderEngine`. Never touch the real engine work.

## Visual rules
- Sizes, colours and type come from `:design` tokens, derived from docs/kindle_res and the references. Feature code uses components and tokens only: no `.refPx`, no raw dp heights, no per-screen spacer stacks.
- Real fonts (Bookerly and Amazon Ember files in :design). Vector icons only. Real text, real containers.
- Never put a crop or recreation of a reference screenshot in the app. No `ContentScale.FillBounds`. No fixed heights, offsets, clips or translations to imitate a cut-off or covered element: build complete elements in real scrollable containers.
- Sheets are one real component with peek and expanded states, drag, scrim and back handling.
- Covers are the only raster images, and only for debug seeding, in `src/debug/res`.

## Fixtures
Debug source set only. Fixtures seed the database and set ViewModel state (open sheet, scroll, theme) through a debug controller and `opus://fixture/<id>` deep links (MainActivity is singleTask). There are no fixture branches in UI code. A liveness check must show the pixels respond to data and font scale.

## Gates (every slice)
`tools/gates.sh` runs: assembleDebug, testDebugUnitTest, lintDebug, connectedDebugAndroidTest, `python3 tools/guard.py .` (must print GUARD OK), and the fidelity run for that slice's screens. Never claim done without pasting the gate output into docs/slices/SNN_REPORT.md.

## Workflow
Branch per slice `sNN-name`, small commits with real messages, tag `sNN-done`. No zips of changes. Log choices in docs/DECISIONS.md. Decide instead of asking. If a gate still fails after 3 honest attempts, write the blocker in the slice report and stop.

## Privacy
No real emails, device names or account names in code, docs or assets. The repo stays private. Fonts and screenshots never leave it.

## Talking to the user
He does not write code. Plain, short language. Explanations first, then all instructions together at the very end. Commands in fenced code blocks, bare, no `#` comment lines (they break his zsh).
