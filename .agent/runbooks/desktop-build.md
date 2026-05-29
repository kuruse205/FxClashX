# Desktop Build Runbook

Windows:

- Build through `dart setup.dart windows --arch amd64` or `--arch arm64`.
- Windows helper service is under `services/helper/`.
- ZIP and Inno Setup installer output are created in `dist/`.
- Optional MSIX uses `dart run msix:create`.

Linux:

- Build through `dart setup.dart linux --arch amd64` or `--arch arm64`.
- Dependencies include `libayatana-appindicator3-dev` and `libkeybinder-3.0-dev`.
- Outputs include DEB, RPM for amd64, and AppImage for amd64.

macOS:

- Build through `dart setup.dart macos --arch arm64` or `--arch amd64`.
- DMG creation uses `create-dmg`.
- Signing and notarization are separate workflows and require secrets.

Validate desktop tray/status behavior on the actual platform when touched.
