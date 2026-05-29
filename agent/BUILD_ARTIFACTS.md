# Build Artifacts

Last Android release build: 2026-05-30 from `main` at commit `13c699a`.

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
| `FxClashX-android-arm64-v8a.apk` | 53615740 | `a0872b325cd597258449234a2a51dd5eab41d9eb4763b10301ab608b4f63f30a` |
| `FxClashX-android-armeabi-v7a.apk` | 53682358 | `7b34329f87cda61ae317cdd8237ef0eb3d541f8971d337754c537930242a4f1e` |
| `FxClashX-android-universal.apk` | 117433937 | `ca4c54f1a55227f685f259f83c1efed61aac839dee9610b80d2a28ce88ca2fe2` |
| `FxClashX-android-x86_64.apk` | 55747625 | `ffc39f80333322951d751e12072ef9e9b3aca06bbea9cd2c5eddf97ba1f60b39` |

Old `FlClashX-android-*.apk` files were removed from `dist/` before this build.
