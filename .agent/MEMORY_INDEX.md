# Memory Index

Use this route map before starting work.

| Task | Read first |
| --- | --- |
| Repo intake | `runbooks/repo-intake.md`, `PROJECT_STATE.md`, `OPEN_QUESTIONS.md` |
| Local development | `runbooks/local-dev.md`, `runbooks/flutter-build.md` |
| Flutter UI edit | `skills/safe-flutter-editing.md`, `PROJECT_STATE.md`, `UPDATE_GATES.md` |
| Dashboard/widget changes | `runbooks/widget-header-customization.md`, `skills/ui-widget-customization.md` |
| Subscription/profile parsing | `runbooks/subscription-profile-parsing.md`, `skills/subscription-header-workflow.md` |
| Remnawave custom headers | `runbooks/remnawave-headers-check.md`, `skills/remnawave-client-integration.md` |
| HWID/client metadata | `runbooks/remnawave-headers-check.md`, `LOCAL_PATCHES.md` |
| Android VPN service | `runbooks/android-build-debug.md`, `runbooks/incident-android-vpn-fails.md` |
| Android local proxy security | `runbooks/android-runtime-security-check.md`, `runbooks/local-proxy-security-check.md` |
| Mihomo core integration | `runbooks/mihomo-core-integration.md`, `skills/mihomo-core-workflow.md` |
| FFI bridge | `runbooks/ffi-core-bridge.md`, `skills/mihomo-core-workflow.md` |
| Desktop tray/status bar | `PROJECT_STATE.md`, `runbooks/desktop-build.md` |
| Linux dependencies | `runbooks/desktop-build.md`, `runbooks/local-dev.md` |
| macOS signing/notarization | `runbooks/android-release-signing.md`, `runbooks/release-pipeline.md`, `UPDATE_GATES.md` |
| Android signing/APK release | `runbooks/android-release-signing.md`, `runbooks/release-pipeline.md` |
| Current release/artifact state | `RELEASE_STATE.md`, `COMMAND_TRACE.md`, `LAST_CHAT_HANDOFF.md` |
| Localization/ARB | `runbooks/localization-update.md`, `skills/localization-workflow.md` |
| Generated code/build_runner | `PROJECT_STATE.md`, `runbooks/flutter-build.md` |
| Release pipeline | `runbooks/release-pipeline.md`, `skills/build-release-workflow.md` |

Incident routes:

- Android VPN fails: `runbooks/incident-android-vpn-fails.md`
- Subscription not parsed: `runbooks/incident-subscription-not-parsed.md`
- Widget headers not applied: `runbooks/widget-header-customization.md`, `runbooks/incident-subscription-not-parsed.md`
- Local proxy security regression: `runbooks/local-proxy-security-check.md`, `ERROR_HISTORY.md`
- Build fails: `runbooks/incident-build-fails.md`
- Release artifact missing: `runbooks/release-pipeline.md`, `runbooks/incident-build-fails.md`
- Generated code mismatch: `runbooks/flutter-build.md`, `runbooks/ffi-core-bridge.md`
