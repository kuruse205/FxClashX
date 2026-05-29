# Local Development Runbook

Use this for local setup and non-release development.

1. Check `flutter --version` and `dart --version`.
2. Run `flutter pub get`.
3. If generated code is stale, run `dart run build_runner build --delete-conflicting-outputs`.
4. Use `flutter analyze` and `flutter test` for broad validation.
5. For Linux desktop, install `libayatana-appindicator3-dev` and `libkeybinder-3.0-dev`.
6. Avoid touching `android/local.properties`, keystores, certificates, or `.env` files.

Common failure points:

- Flutter version mismatch.
- Missing generated files.
- Missing local plugins under `plugins/`.
- Missing Android NDK for core build.
- Missing Linux desktop libraries.
