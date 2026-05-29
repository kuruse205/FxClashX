# Android Build Debug Runbook

Prerequisites:

- Flutter and Android SDK/NDK installed.
- Current project uses compileSdk 36 and NDK `28.0.13004108`.
- Android default applicationId is `com.fxclashx.app`.

Debug steps:

1. Run `flutter pub get`.
2. Run `flutter test test/runtime_config_security_sanitizer_test.dart` if runtime config is touched.
3. Build with `flutter build apk --debug` or the project wrapper if core artifacts are needed.
4. For VPN issues, inspect `RemoteService`, `FlVpnService`, `ServicePlugin`, `TilePlugin`, and `GlobalState.kt`.
5. Collect logs with sensitive identifiers redacted.

Do not print HWID, tokens, key material, or raw subscription secrets in diagnostics.
