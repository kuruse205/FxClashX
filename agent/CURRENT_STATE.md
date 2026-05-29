# Current State

Last updated: 2026-05-30.

## Git

- Branch: `main`.
- Origin: `https://github.com/kuruse205/FxClashX.git`.
- Current pushed commit: `13c699a chore(release): rename product artifacts to FxClashX`.
- Recent durable commits:
  - `13c699a` product/release artifact rename to `FxClashX`.
  - `16373d0` removed stale files after upstream-safe merge.
  - `21a7442` merged `fx/upstream-dev-safe` into `main`.
  - `c5d856b` bootstrapped project agent memory.
- Local work after `d1586e5` fixes Android service package resolution and enables upstream new dashboard by default; commit/push pending.

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
