# Release State

Last updated: 2026-05-30.

## GitHub

- Origin: `https://github.com/kuruse205/FxClashX.git`.
- Current branch: `main`.
- Current pushed commit: latest `main` after the Android runtime package fix.
- Release workflow uses `v*` tags. Pre-release tags build/upload Android APK artifacts only, skip signed macOS artifact download, and can use debug signing fallback when Android signing secrets are incomplete.

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

Built locally on Windows from `main` after fixing Android runtime service package resolution.

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
| `dist/FxClashX-android-arm64-v8a.apk` | 53615692 | `c230dcd0c42b16a62a4397d30b87e74009c42d8ef756e15d1611faa22771e436` |
| `dist/FxClashX-android-armeabi-v7a.apk` | 53682226 | `557cb5c4e2b2c88acb3532cc12f02af603766243b048e6edff2797676cea88c3` |
| `dist/FxClashX-android-universal.apk` | 117433977 | `62b14e00ce6f66a62e8112dc244baaab1fbb0eed1ceba9dd3debbf034f507dfa` |
| `dist/FxClashX-android-x86_64.apk` | 55747633 | `133117cdc1a447ff984740830512a4362680fce0f40b319df108242b687f43c3` |

Device validation:

- Installed `dist/FxClashX-android-arm64-v8a.apk` on ADB device `M2101K9AG` (`arm64-v8a`).
- Confirmed `com.fxclashx.app/com.follow.clashx.service.RemoteService` starts.
- Confirmed `libcore.so` loads and `RemoteService created` appears in `logcat`.
- The previous `Unable to start service Intent { cmp=com.follow.clashx/.service.RemoteService } U=0: not found` message did not recur in the final check.
- Confirmed new dashboard UI appears by default after changing `AppSettingProps.newDashboard` default to `true`.
