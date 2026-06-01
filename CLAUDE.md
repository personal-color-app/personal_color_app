# CLAUDE.md

## Project

OliveMe is an Android Kotlin personal color app. Use `docs/TRUTH_SPEC.md` as
the single source of truth and `Personalcolor design/` as the visual reference.
The default shared harness is `AGENTS.md`; this file is only a Claude
compatibility supplement. If this file and `AGENTS.md` disagree, follow
`AGENTS.md` and `docs/TRUTH_SPEC.md`.

## Skill routing

When the user's request matches an available gstack skill, invoke it. When in
doubt, prefer the skill that produces reviewable evidence.

Key routing rules:

- Product ideas or scope decisions -> invoke `/gstack-office-hours` or `/gstack-plan-ceo-review`.
- Architecture or data flow review -> invoke `/gstack-plan-eng-review`.
- Design system or visual fidelity review -> invoke `/gstack-plan-design-review` or `/gstack-design-review`.
- Browser QA, local UI inspection, screenshots -> invoke `/gstack-browse`.
- Android emulator QA, screenshots, UI tree, logcat, crash buffers, and
  performance evidence -> follow `@test-android-apps`.
- Pre-merge review -> invoke `/gstack-review`.
- Full QA pass -> invoke `/gstack-qa`.
- Documentation after shipping -> invoke `/gstack-document-release`.

## Repository rules

- Browse only with `gstack-browse`.
- Use `gstack-browse` to compare the HTML design source. Do not edit
  `Personalcolor design/` during Android implementation work.
- Work on `dev`; merge to `main` only through PR.
- Keep only `main` and `dev` branches locally and remotely.
- Keep `plan/` out of git.
- Keep `Personalcolor design/` in git.
- Never commit `local.properties`, `.env*`, keystores, or generated build output.

## Android rules

- Android project lives under `android/`.
- Package/application id is `com.oliveme.app`.
- Before and after behavior changes, check drift against `docs/TRUTH_SPEC.md`.
- The app must fail closed into a visible fallback state, not crash.
- Gemini, Kakao, TFLite, Room, camera/gallery, and map features must all have
  mock or fallback behavior for demo stability.
