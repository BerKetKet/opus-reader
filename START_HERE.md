# Starting the new project

## Why a new project
The first attempt matched the screenshots screen by screen. Each screen had its own spacers and its own state, so adding real behaviour meant rewriting everything. The new plan builds the foundations first (data, state, a reader engine interface, a design system measured once) and then stacks 13 small slices on top. Each slice has to pass tests before the next starts, and every slice report must show real numbers.

## What carries over from the old project
- Reference screenshots (docs/reference, docs/kindle_audit).
- The measuring and comparison tools from the `real-ui` branch, which are better than my first versions (pixdiff with mask-aware bands, compare_typography, run_fidelity with the liveness check).
- The Bookerly and Amazon Ember font files, and the agent's own summary of what went wrong (docs/LESSONS.md).
- The decoded Kindle resources, if they are still in /tmp. If not, the kindle-resources skill regenerates them in slice S01.

## What does not carry over
Screens, fixtures and navigation code. They are the thing that made interaction hard.

## What the kit adds
- AGENTS.md with hard architecture and visual rules, enforced by tools/guard.py.
- docs/ARCHITECTURE.md: four Gradle modules, state in ViewModels, Room persistence, a reader engine interface that the fake and the real engine can both fill.
- docs/ROADMAP.md: slices S00 to S12 with acceptance criteria.
- docs/INTERACTION_SPEC.md: every gesture and behaviour.
- Skills: vertical-slice, design-system-first, kindle-resources, pixel-fidelity-loop, natural-layout, interaction-completeness, ui-fonts, gutenberg-catalog.
- KICKOFF_PROMPT_V3.md: paste it into Antigravity once the project is open.

Keep the new repository private. It will hold fonts and screenshots.

## Steps
The unzip path assumes the kit is in your Downloads folder; change it if not.

```
mkdir -p ~/projects/opus-reader
cd ~/projects/opus-reader
git init
unzip -o ~/Downloads/reader-app-kit-v3.zip -d .
mkdir -p docs/reference docs/kindle_audit docs/kindle_res design-fonts
cp -r ~/projects/kindling/docs/reference/. docs/reference/
cp -r ~/projects/kindling/docs/kindle_audit/. docs/kindle_audit/
for f in pixdiff.py measure.py compare_typography.py run_fidelity.py capture.sh; do git -C ~/projects/kindling show real-ui:tools/$f > tools/$f; done
for f in bookerly_regular bookerly_bold bookerly_display amazon_ember_light amazon_ember_regular amazon_ember_medium; do git -C ~/projects/kindling show real-ui:app/src/main/res/font/$f.ttf > design-fonts/$f.ttf; done
git -C ~/projects/kindling show real-ui:docs/SUMMARY_AND_DIFFICULTIES.md > docs/LESSONS.md
cp -r /tmp/kindle_res/resources/res/values docs/kindle_res/
chmod +x tools/capture.sh tools/gates.sh
git add -A
git commit -m "chore: start new project from kit v3"
```

Then open ~/projects/opus-reader in Antigravity and paste the contents of KICKOFF_PROMPT_V3.md. If the last `cp` fails because /tmp was cleared, ignore it.
