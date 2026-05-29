# FxClashX Agent Guide

This file is repository-local memory for FxClashX. The project is a Flutter/Dart multi-platform VPN/proxy client based on Mihomo/ClashMeta, with Android and desktop integrations.

Before any non-trivial task, read:

- `agent.md`
- `.agent/MEMORY_INDEX.md`
- `.agent/PROJECT_CUTOFF.md`
- `.agent/PROJECT_STATE.md`
- `.agent/LAST_CHAT_HANDOFF.md`

Compatibility entry points:

- `agent.md` is the short human/agent handoff.
- `agent/` mirrors the most important current-state facts for tools that expect `./agent`.
- `.agent/` remains the detailed memory source of truth.

For subscription, profile, and header work, read:

- `.agent/runbooks/subscription-profile-parsing.md`
- `.agent/runbooks/remnawave-headers-check.md`
- `.agent/skills/subscription-header-workflow.md`

For Android VPN and security work, read:

- `.agent/runbooks/android-runtime-security-check.md`
- `.agent/runbooks/local-proxy-security-check.md`
- `.agent/skills/android-vpn-security-workflow.md`

For Mihomo, FFI, and core work, read:

- `.agent/runbooks/mihomo-core-integration.md`
- `.agent/runbooks/ffi-core-bridge.md`
- `.agent/skills/mihomo-core-workflow.md`

For UI, widget, and localization work, read:

- `.agent/runbooks/widget-header-customization.md`
- `.agent/runbooks/localization-update.md`
- `.agent/skills/ui-widget-customization.md`
- `.agent/skills/localization-workflow.md`

For build, release, and signing work, read:

- `.agent/UPDATE_GATES.md`
- `.agent/runbooks/flutter-build.md`
- `.agent/runbooks/android-release-signing.md`
- `.agent/runbooks/release-pipeline.md`
- `.agent/skills/build-release-workflow.md`

Rules:

- Current repo files override memory.
- Live command output overrides stored memory.
- Never expose secrets.
- Do not change signing or release behavior without an explicit backup and rollback plan.
- Do not change Android runtime security without a validation or regression plan.
- Do not change subscription parsing without sample header/profile tests.
- Prefer the minimal diff that handles the task.
- Update memory after meaningful work.
