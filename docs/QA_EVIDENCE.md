# OliveMe QA Evidence

Date: 2026-06-01
Branch: `feature/ui-ux-settings-simplification`

## Build

- Windows Gradle: `:app:compileDebugKotlin` passed.
- Windows Gradle: `:app:testDebugUnitTest :app:assembleDebug` passed.
- Windows Gradle: `:app:installDebug` passed on `emulator-5554`.

## HTML Reference

- Tool: `gstack-browse`
- Artifact directory: `/tmp/oliveme-html-reference-1qI9bJ`
- Captured: `html-login.png`, `html-text.txt`
- Source URL: `file:///mnt/c/Users/pjjpj/Desktop/personal_color_app/Personalcolor%20design/index.html`

## Android Emulator QA

- Skill guidance: `@test-android-apps/android-emulator-qa`
- Artifact directory: `/tmp/oliveme-android-qa-ysSBsL`
- Emulator serial: `emulator-5554`
- Package: `com.oliveme.app`
- Captured artifact types: screenshots, UI tree XML, UI summaries, logcat, crash buffer, gfxinfo framestats.
- Crash buffer: `/tmp/oliveme-android-qa-ysSBsL/crash.txt` is 0 bytes.

## Verified Flow Highlights

- Login:
  - `21-login-fresh.*`, `22-email-sheet-fresh.*`
  - `이메일로 로그인하기` opens the email/password sheet.
  - `데모로 시작` displays the random nickname flow and moves to 2FA.
- 2FA:
  - `23-2fa-fresh.*`, `24-main-fresh.*`
  - A drawn digit `1` moves to Main without crash.
- Main and Settings:
  - `24-main-fresh.*`, `25-drawer-fresh.*`, `09-settings.*`
  - Drawer contains the reduced navigation and Settings opens as a real screen.
- Result:
  - `26-result-fresh.*`, `27-result-overflow-fresh.*`
  - Save remains visible; share is reachable from overflow.
- Map:
  - `30-map-fresh.*`
  - Filters are reduced to `전체`, `영업 중`, `저장`; selected store shows favorite and directions actions.
- MyPage:
  - `32-mypage-fresh.*`, `33-mypage-overflow-fresh.*`
  - Tabs are `리포트`, `이력`, `매장`; report save/share are reachable from overflow.
- Diagnosis:
  - `39-diagnosis-sheet-after-fix-retry.*`, `40-diagnosis-sheet-pick.*`
  - Upload card opens the action panel with `사진 선택`, `카메라`, `갤러리`, and `샘플로 체험`.

## Notes

- A first UI dump immediately after app launch returned `ERROR: null root node returned by UiTestAutomationBridge`; retrying after a short wait produced valid UI trees.
- A coordinate-heavy exploratory script briefly left the app for Google/system screens. Final accepted artifacts use app UI tree targets and app-local navigation.
- `gfxinfo-framestats.txt` was captured for a quick performance snapshot. Perfetto was not added because the smoke flow did not show a blocking jank/crash issue.
