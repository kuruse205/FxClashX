# Release State

Last updated: 2026-05-30.

## GitHub

- Origin: `https://github.com/kuruse205/FxClashX.git`.
- Current branch: `main`.
- Current pushed commit: `13c699a chore(release): rename product artifacts to FxClashX`.

## Product Naming

- Public product name: `FxClashX`.
- Release artifact prefix: `FxClashX`.
- Release templates and GitHub API calls point to `kuruse205/FxClashX`.
- Compatibility identifiers intentionally remain where changing them would require migration:
  - Dart package `flclashx`.
  - Android namespace/internal package `com.follow.clashx`.
  - Kotlin class names containing `FlClashX`.
  - Subscription header prefix `flclashx-*`.

## Latest Android APK Build

Built locally on Windows from `main` at `13c699a`.

Command:

```powershell
$env:PATH='C:\Users\Erik\devdeps\flutter\bin;' + $env:PATH
$env:ANDROID_NDK='C:\Users\Erik\Android\Sdk\ndk\28.0.13004108'
dart.bat setup.dart android
```

Toolchain:

- Flutter `3.41.9` stable.
- Dart `3.11.5`.
- Android NDK `C:\Users\Erik\Android\Sdk\ndk\28.0.13004108`.

Artifacts:

| File | Size bytes | SHA256 |
| --- | ---: | --- |
| `dist/FxClashX-android-arm64-v8a.apk` | 53615740 | `a0872b325cd597258449234a2a51dd5eab41d9eb4763b10301ab608b4f63f30a` |
| `dist/FxClashX-android-armeabi-v7a.apk` | 53682358 | `7b34329f87cda61ae317cdd8237ef0eb3d541f8971d337754c537930242a4f1e` |
| `dist/FxClashX-android-universal.apk` | 117433937 | `ca4c54f1a55227f685f259f83c1efed61aac839dee9610b80d2a28ce88ca2fe2` |
| `dist/FxClashX-android-x86_64.apk` | 55747625 | `ffc39f80333322951d751e12072ef9e9b3aca06bbea9cd2c5eddf97ba1f60b39` |
