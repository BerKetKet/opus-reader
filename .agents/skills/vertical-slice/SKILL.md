---
name: vertical-slice
description: The protocol for building one roadmap slice end to end with gates and a report. Use at the start of every slice in docs/ROADMAP.md.
---

# Vertical slice protocol

1. Create branch `sNN-name`. Read the slice in docs/ROADMAP.md, the matching lines of docs/INTERACTION_SPEC.md and the screen references in docs/CANONICAL_SCREENS.md.
2. Write docs/slices/SNN_PLAN.md: data model changes, ViewModels and UiState, components needed, tests. Keep it short.
3. Build bottom-up: data and repositories, ViewModel, then UI from `:design` components. A component that is missing gets added to `:design` (with a gallery entry and a test), never built inside a feature.
4. Every callback is wired, every state persists, every gesture in the spec for this slice works.
5. Tests: unit tests for repositories and ViewModels, Compose UI tests with test tags for each interaction, fixtures for each canonical screen in the slice.
6. Run `tools/gates.sh`. Fix failures. After 3 honest attempts on one failure, record it in the report and stop.
7. Write docs/slices/SNN_REPORT.md: what works, gate output pasted verbatim, fidelity numbers per screen, what is inferred, what is missing. Tag `sNN-done`.
