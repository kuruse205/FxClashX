# Release Pipeline Runbook

Confirmed workflows:

- `.github/workflows/build.yaml`: tag-triggered release workflow for `v*`.
- `.github/workflows/build-core.yaml`: manual core release workflow.
- `.github/workflows/macos-sign-notarize.yaml`: reusable macOS signing/notarization workflow.

Build matrix:

- Android on Ubuntu.
- Windows amd64 and arm64.
- Linux amd64 and arm64.
- macOS amd64 and arm64.

Artifacts:

- Android APKs per ABI plus universal.
- Windows ZIP and setup EXE.
- Linux DEB, RPM/AppImage where supported.
- macOS DMG, signed/notarized for tag builds.
- Stable releases generate sha256 files.

Warnings:

- Telegram notification secrets are used in release workflow.
- Apple certificate and API key secrets are used in macOS signing.
- Android keystore secrets are used in release build.

Any release pipeline edit requires a rollback plan and artifact validation.
