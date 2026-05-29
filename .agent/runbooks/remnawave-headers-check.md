# Remnawave Headers Check Runbook

Confirmed request metadata:

- `x-hwid`
- `x-device-os`
- `x-ver-os`
- `x-device-model`

Confirmed response state headers:

- `x-hwid-active`
- `x-hwid-not-supported`
- `x-hwid-max-devices-reached`
- `x-hwid-limit`

Steps:

1. Locate `lib/models/profile.dart` and `lib/utils/device_info_service.dart`.
2. Verify platform-specific HWID source.
3. Ensure logs do not expose device identifiers.
4. Confirm panel-compatible headers with a controlled test endpoint.
5. Verify notices and UI behavior for HWID unsupported/limit states.

Compatibility with specific Remnawave Panel versions is not confirmed in repo files.
