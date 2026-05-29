# Handoff

The user wants this fork treated as the product for their users.

Completed in the last work session:

- Merged upstream-safe work into `main`.
- Removed stale tracked files left by the unrelated-history merge.
- Renamed public product, GitHub release links, and release artifact prefixes to `FxClashX`.
- Rebuilt Android APKs on `main`; output files now use `FxClashX-android-*`.
- Pushed `main` to `origin` at `13c699a`.

Intentional compatibility left in place:

- Dart package/import name `flclashx`.
- Android internal package/namespace `com.follow.clashx`.
- Kotlin class names such as `FlClashXApplication` and `FlClashXTileService`.
- Subscription provider header prefix `flclashx-*`.
- A migration cleanup line still deletes old Android notification channel `FlClashX_Core`.

Before future release changes:

- Check `git status --short --branch`.
- Confirm branch is `main` or the requested branch.
- Verify product-visible names stay `FxClashX`.
- Rebuild affected artifacts and update this memory.
