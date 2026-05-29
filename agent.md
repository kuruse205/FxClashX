# FxClashX Agent Memory

Last updated: 2026-05-30.

This is the fast entry point for future agents. For full details, read `.agent/MEMORY_INDEX.md`; for compatibility with tools that expect `./agent`, read `agent/README.md`.

## Current State

- Branch: `main`.
- Origin: `https://github.com/kuruse205/FxClashX.git`.
- Current pushed commit: latest `main` after the Android runtime package fix.
- GitHub release pre-release tags build/upload Android APK artifacts only, skip signed macOS artifact download, and can use debug signing fallback if Android signing secrets are incomplete.
- `main` already includes the upstream-safe work from `fx/upstream-dev-safe`.
- The working tree was clean after push except the pre-existing untracked `.claude/` directory.

## Product Identity

- Public product name: `FxClashX`.
- GitHub repository and release links should point to `kuruse205/FxClashX`.
- Release artifact prefix is `FxClashX`, including Android APKs.
- Do not blindly rename compatibility identifiers:
  - Dart package remains `flclashx`.
  - Android namespace/package internals include `com.follow.clashx`.
  - Some Kotlin class names still include `FlClashX`.
  - Subscription header compatibility still uses `flclashx-*`.

## Latest Android Build

Built on Windows from `main` after fixing Android runtime service package resolution:

- `dist/FxClashX-android-arm64-v8a.apk`
- `dist/FxClashX-android-armeabi-v7a.apk`
- `dist/FxClashX-android-x86_64.apk`
- `dist/FxClashX-android-universal.apk`

Build command:

```powershell
$env:PATH='C:\Users\Erik\devdeps\flutter\bin;' + $env:PATH
$env:ANDROID_NDK='C:\Users\Erik\Android\Sdk\ndk\28.0.13004108'
dart.bat setup.dart android
```

Device validation on 2026-05-30:

- Installed `dist/FxClashX-android-arm64-v8a.apk` on ADB device `M2101K9AG`.
- Before fix, `logcat` showed `Unable to start service Intent { cmp=com.follow.clashx/.service.RemoteService } U=0: not found`.
- Cause: after the public Android `applicationId` changed to `com.fxclashx.app`, common Android code still built explicit service intents with old package `com.follow.clashx`.
- Fix: keep `com.follow.clashx` as compatibility namespace for classes/channels/actions, but use the runtime `application.packageName` / `Context.packageName` for Android component packages and internal broadcast permissions.
- After fix, `logcat` showed `Load .../lib/arm64/libcore.so ... ok` and `RemoteService created`.
- Compared against `pluralplay/FlClashX`: our `main` already contains upstream `dev` at `b4ae2ac` (`v0.4.0-pre.12`), including the new dashboard files.
- The new dashboard was present but upstream kept it opt-in via nullable `newDashboard` / `flclashx-newboard`; FxClashX now defaults `newDashboard` to `true` so users see the updated interface by default.
- Final device screenshot check showed the new dashboard on `Главная` without requiring a provider header.

Local toolchain confirmed:

- Flutter `3.41.9` stable at `C:\Users\Erik\devdeps\flutter\bin\flutter.bat`.
- Dart `3.11.5`.
- Android NDK path used: `C:\Users\Erik\Android\Sdk\ndk\28.0.13004108`.

## Guardrails

- Current repo files and live command output override stored memory.
- Never record secrets or signing material.
- Do not weaken Android runtime security sanitizer.
- Do not mass-rename package identifiers without an explicit migration/release plan.
- Do not reintroduce hardcoded `com.follow.clashx` as an Android component package while `applicationId` is `com.fxclashx.app`; use runtime package name for explicit intents.
- Update `agent.md`, `agent/`, and `.agent/` after meaningful work.
