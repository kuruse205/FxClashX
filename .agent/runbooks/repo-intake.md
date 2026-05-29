# Repo Intake Runbook

Purpose: understand repository structure before changing code.

Steps:

1. Run `git status --short --branch`.
2. List files with `rg --files`.
3. Read `pubspec.yaml`, `README_EN.md`, `setup.dart`, `build.yaml`, `Makefile`, and workflow files.
4. Read `lib/main.dart`, `lib/application.dart`, `lib/state.dart`, `lib/clash/`, `lib/models/profile.dart`, and relevant providers.
5. Inspect Android manifests and Gradle files before Android work.
6. Inspect generated files and tests related to the task.

Output:

- Confirmed facts.
- Open questions.
- Secret-risk zones without values.
- Minimal validation commands.
