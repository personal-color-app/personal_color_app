# CLAUDE.md

## Project

OliveMe is an Android Kotlin personal color app. Use `docs/TRUTH_SPEC.md` as
the single source of truth and `Personalcolor design/` as the visual reference.

## Skill routing

When the user's request matches an available gstack skill, invoke it. When in
doubt, prefer the skill that produces reviewable evidence.

Key routing rules:

- Product ideas or scope decisions -> invoke `/gstack-office-hours` or `/gstack-plan-ceo-review`.
- Architecture or data flow review -> invoke `/gstack-plan-eng-review`.
- Design system or visual fidelity review -> invoke `/gstack-plan-design-review` or `/gstack-design-review`.
- Browser QA, local UI inspection, screenshots -> invoke `/gstack-browse`.
- Pre-merge review -> invoke `/gstack-review`.
- Full QA pass -> invoke `/gstack-qa`.
- Documentation after shipping -> invoke `/gstack-document-release`.

## Repository rules

- Browse only with `gstack-browse`.
- Work on `dev`; merge to `main` only through PR.
- Keep only `main` and `dev` branches locally and remotely.
- Keep `plan/` out of git.
- Keep `Personalcolor design/` in git.
- Never commit `local.properties`, `.env*`, keystores, or generated build output.

## Android rules

- Android project lives under `android/`.
- Package/application id is `com.oliveme.app`.
- The app must fail closed into a visible fallback state, not crash.
- Gemini, Kakao, TFLite, Room, camera/gallery, and map features must all have
  mock or fallback behavior for demo stability.
