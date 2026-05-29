# FFI/Core Bridge Runbook

Current state:

- `pubspec.yaml` configures ffigen output at `lib/clash/generated/clash_ffi.dart`.
- The configured header entry is `libclash/android/arm64-v8a/libclash.h`.
- Audit found headers under `libclash/android/includes/<ABI>/libclash.h`.
- Audit did not find `lib/clash/generated/`.
- Android bridge currently uses MethodChannel/AIDL in `lib/clash/lib.dart` and Android service code.
- Desktop bridge uses a core process socket in `lib/clash/service.dart`.

Before FFI changes:

1. Decide whether Dart FFI is still active or obsolete.
2. Verify header paths and generated output path.
3. Locate generator command and required tool versions.
4. Do not manually edit generated bindings unless explicitly required and documented.
5. Validate Android and desktop bridge behavior separately.
