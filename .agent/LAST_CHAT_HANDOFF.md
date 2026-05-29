# Last Chat Handoff

User intent: treat this fork as the real product for users. Product-visible name and GitHub-facing release naming should be `FxClashX`.

Current git state after the last completed release-name work:

- Branch: `main`.
- Origin: `https://github.com/kuruse205/FxClashX.git`.
- Current pushed commit: `13c699a chore(release): rename product artifacts to FxClashX`.
- Working tree was clean except pre-existing untracked `.claude/`.

Completed:

- Bootstrapped project memory in `AGENTS.md` and `.agent/`.
- Merged `fx/upstream-dev-safe` into `main`.
- Removed stale tracked files left by the unrelated-history merge.
- Renamed product-visible names, GitHub release links, release templates, and artifact prefixes to `FxClashX`.
- Rebuilt Android APKs from `main`; outputs are `dist/FxClashX-android-*.apk`.
- Added compatibility memory entry points: `agent.md` and `agent/`.

Latest local Android build:

- Flutter `3.41.9` stable, Dart `3.11.5`.
- Android NDK `C:\Users\Erik\Android\Sdk\ndk\28.0.13004108`.
- Command: `dart.bat setup.dart android`.
- Artifact details and hashes: see `.agent/RELEASE_STATE.md` and `agent/BUILD_ARTIFACTS.md`.

Intentional compatibility left unchanged:

- Dart package/import name `flclashx`.
- Android namespace/internal package `com.follow.clashx`.
- Kotlin class names such as `FlClashXApplication` and `FlClashXTileService`.
- Subscription provider header prefix `flclashx-*`.
- Android migration cleanup for old notification channel `FlClashX_Core`.

Needs live verification later:

- Real Android VPN behavior on device/emulator.
- Remnawave panel compatibility.
- FFI generation path.
- Signing ownership and official release artifact signing/notarization.
