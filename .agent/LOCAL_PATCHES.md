# Local Patches And Fork Behavior

Confirmed fork/custom behavior:

- Android runtime security sanitizer exists in `lib/common/runtime_config_security_sanitizer.dart` and is covered by `test/runtime_config_security_sanitizer_test.dart`.
- Android local proxy runtime behavior forces loopback binding and generated authentication when enabled, and disables mixed-port when disabled.
- Remnawave/HWID request metadata is sent from `lib/models/profile.dart` using `DeviceInfoService`.
- Remnawave response state headers are parsed in `lib/common/subscription_headers.dart`.
- Custom subscription headers with `flclashx-*` prefix are collected and stored in `Profile.providerHeaders`.
- Dashboard widget customization uses provider headers such as `flclashx-widgets`, `flclashx-view`, `flclashx-custom`, `flclashx-denywidgets`, `flclashx-servicename`, `flclashx-servicelogo`, `flclashx-serverinfo`, `flclashx-background`, `flclashx-settings`, `flclashx-globalmode`, `flclashx-hex`, and `flclashx-androidsecure`.
- Android TV support is indicated by Leanback manifest entries and README notes.
- macOS uses status-bar style behavior in `macos/Runner/StatusBarController.swift` and a fixed-size app surface in `lib/application.dart`.
- Custom release workflow builds Android, Windows, Linux, and macOS artifacts from tags and includes macOS sign/notarize workflow.
- Generated core version file `lib/core_version.dart` is written by `setup.dart` from `core/go.mod`.

Potential drift/conflict areas:

- Visible product and release artifact naming was normalized to FxClashX; compatibility identifiers may still use flclashx/FlClashX where required.
- README.md appears to contain mojibake text in the current workspace.
- `pubspec.yaml` ffigen header path does not match the observed `libclash/android/includes/<ABI>/libclash.h` layout.
- `lib/clash/generated/clash_ffi.dart` is configured but not present.
- `setup.dart` generated-file comment mentions `core/constant/version.go`, while current implementation reads `core/go.mod`.

Verify before updates.
