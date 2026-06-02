# AGENTS.md

## Project Rule

This repository implements OliveMe, an Android Kotlin personal color app. The
single source of truth is `docs/TRUTH_SPEC.md`.

All implementation, QA, design, documentation, and review decisions must read
and follow `docs/TRUTH_SPEC.md` first. When the implementation and
`docs/TRUTH_SPEC.md` disagree, either fix the implementation or update
`docs/TRUTH_SPEC.md` to match verified facts before continuing.

Use `@test-android-apps` for real Android app QA, including emulator flows,
screenshots, UI tree captures, logcat, crash buffers, and performance evidence.

Use `gstack-browse` for checking the HTML design source. `Personalcolor design/`
is the immutable visual reference and must not be edited during Android
implementation work.

## gstack

Use `gstack-browse` for all web browsing. Do not use generic web browsing for
project research, local UI checks, or documentation verification.

Available Codex skills:

- `gstack-office-hours`
- `gstack-plan-ceo-review`
- `gstack-plan-eng-review`
- `gstack-plan-design-review`
- `gstack-design-consultation`
- `gstack-design-shotgun`
- `gstack-design-html`
- `gstack-review`
- `gstack-ship`
- `gstack-land-and-deploy`
- `gstack-canary`
- `gstack-benchmark`
- `gstack-browse`
- `gstack-open-gstack-browser`
- `gstack-qa`
- `gstack-qa-only`
- `gstack-design-review`
- `gstack-setup-browser-cookies`
- `gstack-setup-deploy`
- `gstack-setup-gbrain`
- `gstack-sync-gbrain`
- `gstack-retro`
- `gstack-investigate`
- `gstack-document-release`
- `gstack-cso`
- `gstack-autoplan`
- `gstack-plan-devex-review`
- `gstack-devex-review`
- `gstack-careful`
- `gstack-freeze`
- `gstack-guard`
- `gstack-unfreeze`
- `gstack-upgrade`
- `gstack-learn`

## Git Workflow

- Work only on `dev` for implementation.
- Merge to `main` only through a reviewed pull request.
- Keep only `main` and `dev` locally and remotely.
- Open a GitHub issue for major architecture, security, grading, or review
  decisions before changing behavior.
- Link completed issues in the PR body with `Closes #...`.

## Source Rules

- Track `Personalcolor design/`; it is the required visual reference.
- Do not track `plan/`; it is local planning input only.
- Do not commit API keys, local credentials, keystores, Gradle caches, or
  generated build output.
- Use defensive Android code: no runtime crash is acceptable in demo flows.
