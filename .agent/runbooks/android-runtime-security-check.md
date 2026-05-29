# Android Runtime Security Check Runbook

Key file: `lib/common/runtime_config_security_sanitizer.dart`.

Security expectations on Android:

- `allow-lan` false.
- `bind-address` `127.0.0.1`.
- `port` 0.
- `socks-port` 0.
- `skip-auth-prefixes` empty.
- LAN allowed/disallowed lists empty.
- External controller and `secret` removed.
- `mixed-port` 0 unless local proxy is explicitly enabled.
- Local proxy uses ephemeral port range 49152-65535 and generated authentication.

Validation:

1. Run `flutter test test/runtime_config_security_sanitizer_test.dart`.
2. If Android startup path is touched, inspect generated runtime config before core setup.
3. Check `flclashx-androidsecure` behavior in `lib/state.dart`.
4. Confirm unsafe subscription YAML cannot override runtime safety.
