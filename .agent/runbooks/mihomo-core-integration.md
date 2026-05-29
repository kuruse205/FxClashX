# Mihomo Core Integration Runbook

Confirmed paths:

- Go core source wrapper: `core/`.
- Mihomo dependency: `core/go.mod`.
- Current dependency: `github.com/metacubex/mihomo v1.19.25`.
- Build wrapper: `setup.dart`.
- Generated visible version: `lib/core_version.dart`.
- Android prebuilt libs: `libclash/android/<ABI>/libclash.so`.
- Android headers: `libclash/android/includes/<ABI>/libclash.h`.

Workflow:

1. Check `core/go.mod` and `lib/core_version.dart`.
2. Check `setup.dart` for build tags and target matrix.
3. Build a core-only target before full app builds when possible.
4. Verify Android CMake/jniLibs layout if Android core changes.
5. Update `LOCAL_PATCHES.md` if custom build tags or artifacts change.
