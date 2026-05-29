# Project State

Confirmed by repo audit and local build on 2026-05-30.

## Purpose

FxClashX is a Flutter/Dart multi-platform proxy client fork based on FlClash, ClashMeta, and Mihomo. It targets Android and desktop platforms.

## App Identity

- Pub package: `flclashx` in `pubspec.yaml`.
- Pub version: `0.4.0+2026012301`.
- Shared app constant: `FxClashX` in `lib/common/constant.dart`.
- Origin repository: `https://github.com/kuruse205/FxClashX.git`.
- Public release artifact prefix: `FxClashX`.
- Android namespace: `com.follow.clashx`.
- Android default applicationId: `com.fxclashx.app`, overridable by Gradle property `applicationIdOverride`.
- Android default label: `FxClashX`, overridable by Gradle property `appLabelOverride`.
- macOS bundle id: `com.follow.clash`.
- Windows binary/project name: `FxClashX`.

## SDK And Stack

- Dart SDK constraint: `>=3.5.0 <4.0.0`.
- Flutter app with Material 3.
- State management: `flutter_riverpod`, `riverpod_annotation`, generated Riverpod providers.
- Model generation: `freezed`, `json_serializable`, `build_runner`.
- Localization: `flutter_intl`, ARB files in `arb/`, generated Dart in `lib/l10n/intl/`.

## Entrypoint And Architecture

- Entrypoint: `lib/main.dart`.
- App root: `lib/application.dart`.
- Main layers include `AppStateManager`, `ClashManager`, `ConnectivityManager`, platform managers, `ThemeManager`, and `MessageManager`.
- Android uses `VpnManager` and `TileManager`; desktop uses `WindowManager`, `TrayManager`, `HotKeyManager`, and `ProxyManager`.

## Main Directories

- `lib/`: Flutter app, models, providers, views, managers, plugins, clash bridge.
- `android/`: Android app/common/core/service modules and widgets.
- `core/`: Go Mihomo/ClashMeta integration.
- `libclash/`: prebuilt Android `libclash.so` and headers.
- `services/helper/`: Rust helper service for Windows.
- `arb/`: localization source files.
- `.github/workflows/`: CI build, core build, macOS signing/notarization workflows.
- `test/`: Dart tests for subscription headers, runtime config security, and XHTTP transport.

## Mihomo/Core Integration

- `core/go.mod` requires `github.com/metacubex/mihomo v1.19.25`.
- `setup.dart` reads Mihomo version from `core/go.mod`, builds Go core/lib artifacts, and writes `lib/core_version.dart`.
- Current generated core version file contains `kCoreVersionFromSource = 'v1.19.25'`.
- Android CMake links `android/core/src/main/cpp` against `android/core/src/main/jniLibs/<ABI>/libclash.so` when present.
- Repository has `libclash/android/<ABI>/libclash.so` and `libclash/android/includes/<ABI>/libclash.h`.

## FFI And Generated Files

- `pubspec.yaml` has `ffigen` output set to `lib/clash/generated/clash_ffi.dart`.
- `lib/clash/generated/` was not present during audit.
- Generated model/provider/localization files are committed under `lib/models/generated/`, `lib/providers/generated/`, and `lib/l10n/intl/`.
- `build.yaml` routes generated files into these generated directories.

## Android VPN And Runtime Security

- Android VPN service lives in `android/service/src/main/kotlin/com/follow/clashx/service/FlVpnService.kt`.
- Remote/core process entry is `RemoteService` in Android `:remote` process.
- Flutter-to-Android bridge is `lib/clash/lib.dart` through MethodChannel `com.follow.clashx/service`.
- Android actions in README and manifest include start, stop, and change actions under the application id.
- `lib/common/runtime_config_security_sanitizer.dart` enforces Android runtime safety:
  - `allow-lan = false`
  - `bind-address = 127.0.0.1`
  - `port = 0`
  - `socks-port = 0`
  - empty LAN allowed/disallowed lists
  - empty `skip-auth-prefixes`
  - `mixed-port = 0` unless local proxy is enabled
  - local proxy uses an ephemeral port and generated authentication
  - external controller and `secret` are removed
- Tests exist in `test/runtime_config_security_sanitizer_test.dart`.

## Subscription, Headers, Remnawave

- Profile subscription update path is `lib/models/profile.dart`.
- Request headers sent to providers can include `x-hwid`, `x-device-os`, `x-ver-os`, and `x-device-model`.
- Device metadata source is `lib/utils/device_info_service.dart`.
- Provider response headers are collected in `lib/common/subscription_headers.dart`.
- Collected headers include Remnawave HWID state headers, `support-url`, `profile-update-interval`, `announce`, and all `flclashx-*` headers.
- Header merge and domain redirect helpers are in `lib/common/subscription_headers.dart`.
- Subscription header tests exist in `test/subscription_headers_test.dart`.

## Widgets And Customization

- Dashboard widgets live under `lib/views/dashboard/widgets/`.
- Confirmed widgets include announce, metainfo, service info, change server button, hero connect, network speed, traffic usage, quick options, and others.
- Provider headers drive dashboard/proxy customization in `lib/controller.dart`, `lib/providers/state.dart`, and dashboard widget files.
- Android home-screen widgets are in `android/app/src/main/kotlin/com/follow/clashx/widgets/` with XML layouts/resources under `android/app/src/main/res/`.
- Android quick settings tile is `FlClashXTileService`.

## Supported Platforms

- Flutter targets present: Android, Windows, Linux, macOS.
- `setup.dart` supports build targets `android`, `windows`, `linux`, and `macos`.
- iOS is not configured as a target in the inspected repository.

## Build And Release

- Local build wrapper: `dart setup.dart <platform> [--arch <arch>] [--out core|app] [--env pre|stable]`.
- Local Windows Flutter path used successfully: `C:\Users\Erik\devdeps\flutter\bin\flutter.bat`.
- Local Flutter version confirmed: `3.41.9` stable with Dart `3.11.5`.
- Local Android NDK used successfully: `C:\Users\Erik\Android\Sdk\ndk\28.0.13004108`.
- Latest local Android build generated `dist/FxClashX-android-*.apk`; see `RELEASE_STATE.md`.
- Makefile shortcuts include Android ARM64, Android app, macOS ARM64, core-only targets, local macOS builds, notarization, and clean.
- CI release workflow: `.github/workflows/build.yaml`, tag-triggered on `v*`.
- CI core workflow: `.github/workflows/build-core.yaml`, manual input `version`.
- macOS sign/notarize reusable workflow: `.github/workflows/macos-sign-notarize.yaml`.
- Release upload creates GitHub release artifacts and stable sha256 files.

## Signing/Secret Variable Names

Variable names observed, values not recorded:

- Android: `KEYSTORE`, `KEY_ALIAS`, `STORE_PASSWORD`, `KEY_PASSWORD`.
- Android local properties: `storePassword`, `keyAlias`, `keyPassword`.
- macOS: `APPLE_CERTIFICATE`, `APPLE_CERTIFICATE_PASSWORD`, `APPLE_DEVELOPER_ID`, `APPLE_TEAM_ID`, `APPLE_API_KEY_BASE64`, `APPLE_API_ISSUER`, `APPLE_API_KEY_ID`.
- GitHub/notification: `GITHUB_TOKEN`, `TELEGRAM_BOT_TOKEN`, `TELEGRAM_CHANNEL_ID_1`, `TELEGRAM_CHANNEL_ID_2`, `TELEGRAM_CHANNEL_ID_3`.

## Known Commands

- `flutter pub get`
- `dart run build_runner build --delete-conflicting-outputs`
- `flutter analyze`
- `flutter test`
- `dart setup.dart android`
- `dart setup.dart android --arch arm64`
- `dart setup.dart android --arch arm64 --out core`
- `dart setup.dart windows --arch amd64`
- `dart setup.dart windows --arch arm64`
- `dart setup.dart linux --arch amd64`
- `dart setup.dart linux --arch arm64`
- `dart setup.dart macos --arch arm64`
- `dart setup.dart macos --arch amd64`
- `dart run msix:create`

## Must Verify Live

- Exact local Flutter version installed. Confirmed on 2026-05-30: Flutter `3.41.9`, Dart `3.11.5`.
- Whether `lib/clash/generated/clash_ffi.dart` is intentionally absent.
- Whether the `ffigen` header path in `pubspec.yaml` is stale.
- Whether generated files are regenerated locally or committed after each change.
- Android VPN behavior on real device/emulator.
- End-to-end Remnawave HWID behavior against a compatible panel.
- Signing/notarization secrets and ownership.
