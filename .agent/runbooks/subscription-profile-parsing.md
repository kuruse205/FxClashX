# Subscription/Profile Parsing Runbook

Confirmed paths:

- Profile model/update path: `lib/models/profile.dart`.
- Header parser: `lib/common/subscription_headers.dart`.
- Header tests: `test/subscription_headers_test.dart`.
- Runtime config merge path: `lib/state.dart`.

Steps:

1. Create or use sample response headers and sample profile YAML.
2. Confirm request headers sent by `Profile.update`.
3. Confirm response headers collected by `collectProviderHeaders`.
4. Confirm `providerHeaders` persists in `Profile`.
5. Confirm update behavior merges or replaces headers as intended.
6. Confirm UI consumers read decoded/fallback values safely.
7. Run `flutter test test/subscription_headers_test.dart`.

Never hardcode UI output before confirming parser and storage behavior.
