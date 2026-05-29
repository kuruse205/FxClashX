# Incident: Subscription Not Parsed

Diagnostics:

1. Capture raw response status and header names with values redacted unless they are non-sensitive test fixtures.
2. Check `lib/models/profile.dart`.
3. Check `lib/common/subscription_headers.dart`.
4. Check profile JSON persistence for `providerHeaders`.
5. Check UI consumer path in controller/providers/widgets.
6. Check update merge behavior in `lib/controller.dart`.
7. Validate with `flutter test test/subscription_headers_test.dart`.

Do not hardcode provider UI values before confirming parser and storage behavior.
