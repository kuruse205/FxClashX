# FxClashX Agent Memory

Last updated: 2026-05-30.

This is the fast entry point for future agents. For full details, read `.agent/MEMORY_INDEX.md`; for compatibility with tools that expect `./agent`, read `agent/README.md`.

## Current State

- Branch: `main`.
- Origin: `https://github.com/kuruse205/FxClashX.git`.
- Current pushed commit: `13c699a chore(release): rename product artifacts to FxClashX`.
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

Built on Windows from `main` after product rename:

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

Local toolchain confirmed:

- Flutter `3.41.9` stable at `C:\Users\Erik\devdeps\flutter\bin\flutter.bat`.
- Dart `3.11.5`.
- Android NDK path used: `C:\Users\Erik\Android\Sdk\ndk\28.0.13004108`.

## Guardrails

- Current repo files and live command output override stored memory.
- Never record secrets or signing material.
- Do not weaken Android runtime security sanitizer.
- Do not mass-rename package identifiers without an explicit migration/release plan.
- Update `agent.md`, `agent/`, and `.agent/` after meaningful work.
