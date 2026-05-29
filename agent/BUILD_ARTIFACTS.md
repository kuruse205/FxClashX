# Build Artifacts

Last Android release build: 2026-05-30 from `main` after Android service package and default dashboard fixes.

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
- Mihomo core version from `core/go.mod`: `v1.19.25`.

Generated APKs in `dist/`:

| File | Size bytes | SHA256 |
| --- | ---: | --- |
| `FxClashX-android-arm64-v8a.apk` | 53615692 | `c230dcd0c42b16a62a4397d30b87e74009c42d8ef756e15d1611faa22771e436` |
| `FxClashX-android-armeabi-v7a.apk` | 53682226 | `557cb5c4e2b2c88acb3532cc12f02af603766243b048e6edff2797676cea88c3` |
| `FxClashX-android-universal.apk` | 117433977 | `62b14e00ce6f66a62e8112dc244baaab1fbb0eed1ceba9dd3debbf034f507dfa` |
| `FxClashX-android-x86_64.apk` | 55747633 | `133117cdc1a447ff984740830512a4362680fce0f40b319df108242b687f43c3` |

Final arm64 APK was installed on ADB device `M2101K9AG`; `libcore.so` loaded, `RemoteService created` appeared, and the new dashboard appeared by default.

GitHub release APK rebuild:

- Tag: `v0.4.0-fx.7`.
- GitHub Actions run: `26663124884`.
- Release URL: `https://github.com/kuruse205/FxClashX/releases/tag/v0.4.0-fx.7`.
- Published Android assets checked with HTTP `200`: `FxClashX-android-arm64-v8a.apk`, `FxClashX-android-armeabi-v7a.apk`, `FxClashX-android-universal.apk`, and `FxClashX-android-x86_64.apk`.

GitHub multi-platform release rebuild:

- Tag: `v0.4.0-fx.9`.
- GitHub Actions run: `26664860579`.
- Release URL: `https://github.com/kuruse205/FxClashX/releases/tag/v0.4.0-fx.9`.
- Published assets checked with HTTP `200`: Android APKs, Windows `amd64`/`arm64` ZIP and setup EXE files, Linux `amd64` DEB/RPM/AppImage and `arm64` DEB files, and unsigned macOS `amd64`/`arm64` DMGs.
