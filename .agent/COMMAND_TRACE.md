# Command Trace

## 2026-05-30 - Bootstrap Project Memory

Goal:
Bootstrap project memory for FxClashX.

Before:
Repository had no structured agent memory or memory state was incomplete.

Actions:

- Performed read-only repository audit.
- Identified Flutter/Dart project structure.
- Identified core/Mihomo/FFI areas.
- Identified Android VPN/security areas.
- Identified subscription/header/Remnawave areas.
- Identified build/release/signing workflow.
- Created `.agent` structure.
- Created `AGENTS.md`.
- Created runbooks and skills.
- Recorded confirmed facts and open questions.

Verification:

- Created-file list should be checked with `rg --files .agent`.
- Confirmed repo facts are in `PROJECT_STATE.md`.
- Unresolved questions are in `OPEN_QUESTIONS.md`.

Result:
Completed.

Rollback:
Remove `.agent/` and `AGENTS.md` if this memory layer must be reverted.

Notes:
No secrets stored.

## 2026-05-30 - Merge Main, Rename Product Artifacts, Rebuild APK

Goal:
Merge the upstream-safe work into `main`, make GitHub/product-facing naming `FxClashX`, and rebuild Android APKs from `main`.

Before:
`main` did not contain the new memory layer or upstream-safe work, and Android APK artifacts were named `FlClashX-android-*.apk`.

Actions:

- Committed memory bootstrap on `fx/upstream-dev-safe` as `c5d856b`.
- Merged `fx/upstream-dev-safe` into `main` with unrelated histories allowed as `21a7442`.
- Removed stale tracked files left by the unrelated-history merge as `16373d0`.
- Renamed product-visible and GitHub-facing release names to `FxClashX` as `13c699a`.
- Pushed `main` to `origin` (`https://github.com/kuruse205/FxClashX.git`).
- Rebuilt Android APKs with `dart.bat setup.dart android`.

Verification:

- `git status --short --branch` reported `main...origin/main` plus only pre-existing untracked `.claude/`.
- `rg` found no old `FlClashX-android-*` artifact templates or `pluralplay/FlClashX` GitHub release links in the checked product/release paths.
- APKs generated in `dist/`:
  - `FxClashX-android-arm64-v8a.apk`
  - `FxClashX-android-armeabi-v7a.apk`
  - `FxClashX-android-x86_64.apk`
  - `FxClashX-android-universal.apk`

Result:
Completed and pushed to GitHub at `13c699a`.

Rollback:
Revert `13c699a` for product/artifact naming only. Reverting merge/stale-file commits requires a separate branch-level rollback plan.

Notes:
No secrets stored. Compatibility identifiers were intentionally left unchanged.

## 2026-05-30 - Add Compatibility Agent Memory

Goal:
Fill `agent.md` and `./agent` context memory while keeping `.agent` as the detailed memory source.

Actions:

- Added `agent.md`.
- Added `agent/README.md`, `agent/CURRENT_STATE.md`, `agent/BUILD_ARTIFACTS.md`, and `agent/HANDOFF.md`.
- Added `.agent/RELEASE_STATE.md`.
- Updated memory index, project state, decisions, local patches, and handoff.

Verification:

- Check with `rg --files agent .agent`.

Result:
Completed locally.

## 2026-05-30 - Fix Android Runtime Service Package Resolution

Goal:
Install the GitHub-current arm64 APK on an ADB-connected phone, diagnose why the Android core did not start after launch, fix it, rebuild APKs, and record the incident.

Before:
`dist/FxClashX-android-arm64-v8a.apk` installed successfully as `com.fxclashx.app` on device `M2101K9AG`, but `logcat` showed Android trying to start `com.follow.clashx/.service.RemoteService` and failing with `not found`.

Actions:

- Installed `dist/FxClashX-android-arm64-v8a.apk` with `adb install -r -d`.
- Collected launch logs with `adb logcat`.
- Confirmed device ABI `arm64-v8a` and installed package `com.fxclashx.app`.
- Changed Android common component intent construction to use runtime `application.packageName` for component package names.
- Changed internal broadcast registration/sending permissions to use `Context.packageName`.
- Kept `com.follow.clashx` as compatibility namespace for Dart MethodChannel names, Kotlin class names, and internal action strings.
- Ran `flutter.bat test test\runtime_config_security_sanitizer_test.dart`.
- Built all Android APKs with `dart.bat setup.dart android`.
- Installed the final arm64 APK and verified `libcore.so` load plus `RemoteService created`.

Verification:

- `flutter.bat build apk --debug` completed before device validation.
- `flutter.bat test test\runtime_config_security_sanitizer_test.dart` passed all 8 tests.
- Final `dart.bat setup.dart android` completed and generated all four `dist/FxClashX-android-*.apk` artifacts.
- Final device `logcat` contained `Load .../lib/arm64/libcore.so ... ok` and `RemoteService created`.
- Final device `logcat` did not show the old `Unable to start service Intent { cmp=com.follow.clashx/.service.RemoteService } U=0: not found` failure in the checked window.

Result:
Completed locally; ready to push.

Rollback:
Revert the Android common component package changes in `Components.kt`, `Ext.kt`, and `GlobalState.kt`, then rebuild APKs. This would reintroduce the known `RemoteService` failure while the app id remains `com.fxclashx.app`.

Notes:
No secrets stored. A transient universal APK build failed once because Windows held `build\app\intermediates\dex\release\minifyReleaseWithR8\classes.dex`; stopping Gradle, removing `build`, running `flutter pub get`, and rebuilding resolved it.

## 2026-05-30 - Compare Upstream UI And Enable New Dashboard By Default

Goal:
Respond to the user's report that FxClashX did not show the updated `pluralplay/FlClashX` interface.

Actions:

- Fetched `upstream` from `https://github.com/pluralplay/FlClashX.git`.
- Compared `main` with `upstream/main` and `upstream/dev`.
- Confirmed `main` already contains `upstream/dev` at `b4ae2ac` (`v0.4.0-pre.12`), including the new dashboard commits.
- Confirmed dashboard file trees match upstream for `lib/views/dashboard` and `lib/views/theme.dart`.
- Found the reason the UI looked old: upstream left the new dashboard opt-in through nullable `AppSettingProps.newDashboard` or provider header `flclashx-newboard`.
- Changed FxClashX default `AppSettingProps.newDashboard` to `true`.
- Updated generated `config.freezed.dart` and `config.g.dart` manually because `build_runner` currently fails to compile with the local `analyzer_plugin`/`analyzer` dependency combination.
- Rebuilt Android APKs with `dart.bat setup.dart android`.
- Installed the new arm64 APK and verified the new dashboard appears by default on the device.

Verification:

- `flutter.bat test test\runtime_config_security_sanitizer_test.dart` passed all 8 tests.
- `flutter.bat analyze` ran but exited with the repository's existing warning/info debt: 668 issues; no new compile error was identified from the changed files.
- `dart.bat run build_runner build --delete-conflicting-outputs` failed before generation because `analyzer_plugin 0.12.0` does not compile against current `analyzer 7.6.0`.
- `dart.bat setup.dart android` completed and generated all four APKs in `dist/`.
- Final device `logcat` contained `Load .../lib/arm64/libcore.so ... ok` and `RemoteService created`.
- Final ADB screenshot check showed the new dashboard on `Главная`.

Result:
Completed locally; ready to push.

Rollback:
Set `AppSettingProps.newDashboard` default back to nullable/no default and update generated files, then rebuild APKs. This will make the new dashboard opt-in again.

## 2026-05-30 - Trigger GitHub Release APK Build

Goal:
Push current `main` and rebuild Android APK files in GitHub Releases.

Actions:

- Confirmed `main` was pushed at `504673e`.
- Pushed tag `v0.4.0-fx.5` to trigger `.github/workflows/build.yaml`.
- Observed run `26662247461`.
- Found pre-release build was blocked by macOS signing setup because Apple signing secrets were empty.
- Updated release workflow so macOS signing certificate setup, Xcode signing config, and reusable macOS notarization only run for stable tags; pre-release tags can still build/upload Android APKs without Apple signing secrets.

Status:
Workflow fix pending commit/push and a replacement pre-release tag.

## 2026-05-30 - Allow GitHub Prerelease APK Builds Without Signing Secrets

Goal:
Make GitHub Releases rebuild and publish Android APK files from a pre-release tag even when Apple signing secrets or Android signing secrets are not configured.

Actions:

- Observed replacement tag `v0.4.0-fx.6` run after the first macOS signing fix.
- Confirmed macOS signing setup was skipped for pre-release tags, but Android still failed because empty signing secrets produced an invalid `android/app/keystore.jks`.
- Updated `.github/workflows/build.yaml` so Android signing setup only writes `keystore.jks` and `android/local.properties` when all Android signing secrets are present.
- Added debug-signing fallback for incomplete Android signing secrets.
- Updated pre-release workflow behavior so non-Android setup/build/upload steps run only for stable tags; pre-release tags focus on Android APK artifacts.
- Updated release upload so signed macOS artifacts are downloaded only for stable tags.
- Made Telegram notifications skip cleanly when notification secrets are incomplete, after the GitHub release step.

Verification:

- Pushed `main` commit `7f578e4` and tag `v0.4.0-fx.7`.
- GitHub Actions run `26663124884` completed successfully.
- Android build job completed `dart setup.dart android` and uploaded `artifact-android`.
- Release upload job completed successfully and created pre-release `v0.4.0-fx.7`.
- Direct release asset checks returned HTTP `200` for `FxClashX-android-arm64-v8a.apk`, `FxClashX-android-armeabi-v7a.apk`, `FxClashX-android-universal.apk`, and `FxClashX-android-x86_64.apk`.

Rollback:
Revert the release workflow changes if signed Android releases plus desktop/macOS artifacts must be mandatory for every `v*` tag. Before rollback, configure all required repository secrets or pre-release APK publication will be blocked again.
