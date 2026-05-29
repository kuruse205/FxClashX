# Current State

Last updated: 2026-05-30.

## Git

- Branch: `main`.
- Origin: `https://github.com/kuruse205/FxClashX.git`.
- Current pushed commit: latest `main` after Android runtime package resolution, default new dashboard, release workflow fix, and release-state memory update.
- Recent durable commits:
  - `7f578e4` allows APK prerelease publishing and documents release workflow behavior.
  - `6ce2bbd` skips macOS signing for prereleases.
  - `504673e` fixes Android service startup under runtime app id and enables upstream new dashboard by default.
  - `13c699a` product/release artifact rename to `FxClashX`.
- Latest GitHub APK pre-release: `v0.4.0-fx.7`, Actions run `26663124884`, release URL `https://github.com/kuruse205/FxClashX/releases/tag/v0.4.0-fx.7`.

## Identity

- Public product/app/release name: `FxClashX`.
- Repository links: `kuruse205/FxClashX`.
- Android default application id: `com.fxclashx.app`.
- Android namespace/internal package: `com.follow.clashx`.
- Pub package name: `flclashx`.
- Compatibility headers: `flclashx-*`.
- Upstream `pluralplay/FlClashX` `dev` at `b4ae2ac` is already included; FxClashX defaults the upstream new dashboard to enabled.

## Important Paths

- Product constant: `lib/common/constant.dart`.
- Build/release script: `setup.dart`.
- Android label strings: `android/app/src/main/res/values/strings.xml`.
- Release templates: `.github/release_template.md`, `.github/pre_release_template.md`.
- CI release workflow: `.github/workflows/build.yaml`.
- Detailed memory: `.agent/`.
