# Localization Update Runbook

Confirmed paths:

- ARB source: `arb/intl_en.arb`, `arb/intl_ru.arb`, `arb/intl_ja.arb`, `arb/intl_zh_CN.arb`.
- Generated localization: `lib/l10n/l10n.dart`, `lib/l10n/intl/messages_*.dart`.
- Config: `flutter_intl` section in `pubspec.yaml`.

Steps:

1. Update all required ARB locales.
2. Regenerate localization with the project's established Flutter Intl workflow.
3. Verify generated files changed consistently.
4. Search for direct hardcoded strings if UI text should be localized.
5. Validate Russian/English text fits UI surfaces.
