# Incident: Android VPN Fails

Read-only diagnostics:

1. Check recent changes with `git status --short`.
2. Inspect `lib/main.dart`, `lib/clash/lib.dart`, `lib/manager/vpn_manager.dart`, `android/app/src/main/kotlin/com/follow/clashx/GlobalState.kt`, `ServicePlugin.kt`, `RemoteService.kt`, and `FlVpnService.kt`.
3. Confirm VPN permission flow through `TempActivity` and `VpnService.prepare`.
4. Check runtime config sanitizer if startup/config was touched.
5. Collect logs with HWID, tokens, subscription URLs, and credentials redacted.
6. If config/security-related, prepare rollback before changing behavior.

Validation:

- Focused Android device/emulator test.
- Runtime sanitizer test.
- Tile/widget cold-start path if related.
