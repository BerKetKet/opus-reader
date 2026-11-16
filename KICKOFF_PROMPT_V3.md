You are building "Opus Reader" from scratch: a personal Android EPUB reader whose UI is a faithful copy of the Kindle Android app, with our own content (imported EPUBs and Project Gutenberg). Not for publication. An earlier attempt failed because it matched screenshots screen by screen with fixed spacers, in-screen state and fixture branches, so real interaction could not be added. This time behaviour comes first and the look comes from a measured design system.

The old project is at ~/projects/kindling (read-only reference; the branch `real-ui` has useful measurements). Do not copy its screens, fixtures or navigation. You may reuse its measured values after re-verifying them.

Read first, in this order: AGENTS.md, docs/LESSONS.md, docs/ARCHITECTURE.md, docs/ROADMAP.md, docs/INTERACTION_SPEC.md, docs/CANONICAL_SCREENS.md, and every skill in .agents/skills/. Then look at the reference screenshots in docs/reference/ and docs/kindle_audit/.

Work through the slices in docs/ROADMAP.md, S00 to S12, in order, using the vertical-slice skill. Do not start a slice until the previous one passes tools/gates.sh. Each slice ends with docs/slices/SNN_REPORT.md containing the pasted gate output, fidelity numbers per screen, what is inferred and what is missing, and a git tag `sNN-done`. Work autonomously and decide instead of asking. If a gate still fails after 3 honest attempts, write the blocker in the report and stop.

Non-negotiables: no screenshot pixels in the app, no fixture flags in production code, no default no-op callbacks, no feature code that does its own measurements, real fonts, real containers, real persistence, and honest numbers in every report. Run `python3 tools/guard.py .` before every commit.

Start with S00. When you finish the work you can complete, write a short plain-language summary: which slices are done, what works on the phone, what is inferred, what is missing, and the exact build and run commands in fenced code blocks at the very end.
