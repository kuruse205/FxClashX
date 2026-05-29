# Flutter Build Runbook

Known commands:

- `flutter pub get`
- `dart run build_runner build --delete-conflicting-outputs`
- `flutter analyze`
- `flutter test`
- `dart setup.dart android`
- `dart setup.dart windows --arch amd64`
- `dart setup.dart windows --arch arm64`
- `dart setup.dart linux --arch amd64`
- `dart setup.dart linux --arch arm64`
- `dart setup.dart macos --arch arm64`
- `dart setup.dart macos --arch amd64`

Validation:

1. Confirm dependencies with `flutter pub get`.
2. Run focused tests first, then broader `flutter test`.
3. If touching generated models/providers, run build_runner.
4. If touching core/build paths, use `dart setup.dart <platform> --out core` before full app builds where possible.
5. Keep generated-file changes grouped with their source changes.
