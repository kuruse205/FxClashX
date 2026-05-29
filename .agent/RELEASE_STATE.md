# Release State

Last updated: 2026-05-30.

## GitHub

- Origin: `https://github.com/kuruse205/FxClashX.git`.
- Current branch: `main`.
- Current pushed commit: latest `main` after the Android runtime package fix, new dashboard default, multi-platform release workflow fix, and release-state memory update.
- Release workflow uses `v*` tags. Pre-release tags build/upload Android, Windows, Linux, and unsigned macOS artifacts; stable tags still keep signed/notarized macOS behavior.
- Latest GitHub APK pre-release: `v0.4.0-fx.7` at commit `7f578e4`.
- GitHub Actions run `26663124884` completed successfully and published the four Android APK assets.
- Latest GitHub multi-platform pre-release: `v0.4.0-fx.9` at commit `6f12106`.
- GitHub Actions run `26664860579` completed successfully and published Android, Windows, Linux, and macOS assets.

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

Latest GitHub release APK rebuild:

- Tag: `v0.4.0-fx.7`.
- Release URL: `https://github.com/kuruse205/FxClashX/releases/tag/v0.4.0-fx.7`.
- Published assets checked with HTTP `200`: `FxClashX-android-arm64-v8a.apk`, `FxClashX-android-armeabi-v7a.apk`, `FxClashX-android-universal.apk`, and `FxClashX-android-x86_64.apk`.

Latest GitHub multi-platform release rebuild:

- Tag: `v0.4.0-fx.9`.
- Release URL: `https://github.com/kuruse205/FxClashX/releases/tag/v0.4.0-fx.9`.
- Actions run: `26664860579`.
- Published assets checked with HTTP `200`:
  - Android: `FxClashX-android-arm64-v8a.apk`, `FxClashX-android-armeabi-v7a.apk`, `FxClashX-android-universal.apk`, `FxClashX-android-x86_64.apk`.
  - Windows: `FxClashX-windows-amd64.zip`, `FxClashX-windows-amd64-setup.exe`, `FxClashX-windows-arm64.zip`, `FxClashX-windows-arm64-setup.exe`.
  - Linux: `FxClashX-linux-amd64.deb`, `FxClashX-linux-amd64.rpm`, `FxClashX-linux-amd64.AppImage`, `FxClashX-linux-arm64.deb`.
  - macOS unsigned prerelease DMGs: `FxClashX-macos-amd64.dmg`, `FxClashX-macos-arm64.dmg`.

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
