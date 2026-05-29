# Last Chat Handoff

User intent: install the GitHub-current Android arm64 APK on the connected phone, diagnose why the core did not start after launch, fix it, update memory, push to GitHub, and rebuild APKs for users.

Completed in this session:

- Installed `dist/FxClashX-android-arm64-v8a.apk` on ADB device `M2101K9AG`.
- Reproduced the startup failure in `logcat`.
- Root cause: Android app id is `com.fxclashx.app`, but common code built explicit `RemoteService` intents with old package `com.follow.clashx`. Android therefore looked for `com.follow.clashx/.service.RemoteService`, which is not installed.
- Fixed Android common code so component packages and internal broadcast permissions use runtime package name, while `com.follow.clashx` remains the compatibility namespace for Kotlin classes, Dart channels, and action names.
- Verified final arm64 APK on device: `libcore.so` loads and `RemoteService created` appears in `logcat`.
- Compared with `pluralplay/FlClashX`: our `main` already includes current `upstream/dev` (`b4ae2ac`, `v0.4.0-pre.12`) and its new dashboard files.
- Found why the UI looked old: upstream made the new dashboard opt-in. FxClashX now defaults `AppSettingProps.newDashboard` to `true`, while the "New look" / "Новый вид" setting can still turn it off.
- Verified by ADB screenshot that `Главная` shows the new dashboard by default.
- Rebuilt all Android APKs into `dist/`.
- Pushed the Android runtime/UI fixes to `main`.
- Fixed `.github/workflows/build.yaml` so prerelease tags publish Android APKs without being blocked by missing Apple signing secrets or incomplete Android signing secrets.
- Pushed tag `v0.4.0-fx.7` and verified GitHub Actions run `26663124884` completed successfully.
- GitHub pre-release `v0.4.0-fx.7` published all four Android APK assets.
- Expanded pre-release CI to build Android, Windows, Linux, and unsigned macOS artifacts.
- Fixed pre-release macOS DMG packaging by passing `create-dmg --no-code-sign`.
- Pushed tag `v0.4.0-fx.9` and verified GitHub Actions run `26664860579` completed successfully.
- GitHub pre-release `v0.4.0-fx.9` published Android APKs, Windows ZIP/setup EXE files, Linux DEB/RPM/AppImage files, and unsigned macOS DMGs.

Validation completed:

- `flutter.bat build apk --debug`
- `flutter.bat test test\runtime_config_security_sanitizer_test.dart`
- `dart.bat setup.dart android`
- `adb install -r -d .\dist\FxClashX-android-arm64-v8a.apk`
- Device `logcat` final check: no old `com.follow.clashx/.service.RemoteService not found` failure in the checked window.
- Device screenshot final check: new dashboard visible on `Главная`.
- GitHub release asset checks returned HTTP `200` for `FxClashX-android-arm64-v8a.apk`, `FxClashX-android-armeabi-v7a.apk`, `FxClashX-android-universal.apk`, and `FxClashX-android-x86_64.apk`.
- GitHub release asset checks for `v0.4.0-fx.9` returned HTTP `200` for all expected Android, Windows, Linux, and macOS assets.

Latest Android artifacts:

- `dist/FxClashX-android-arm64-v8a.apk`
- `dist/FxClashX-android-armeabi-v7a.apk`
- `dist/FxClashX-android-x86_64.apk`
- `dist/FxClashX-android-universal.apk`

Latest GitHub release:

- `https://github.com/kuruse205/FxClashX/releases/tag/v0.4.0-fx.9`

Keep in mind:

- Do not mass-rename `com.follow.clashx`; it is still the Kotlin namespace and channel/action compatibility namespace.
- For Android component package names, use runtime `application.packageName` / `Context.packageName`.
- Do not assume missing new UI means upstream is absent; first check `newDashboard` config/default and provider header behavior.
- Public product name and release artifact prefix remain `FxClashX`.
- Pre-existing untracked `.claude/` is unrelated.
