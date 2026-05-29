# Project Cutoff

Date: 2026-05-30, workspace timezone Asia/Yekaterinburg.

Fresh sources:

1. Live command output from the current workspace.
2. Current repository files.
3. Fresh `.agent` memory.
4. Historical notes.
5. Old chat assumptions.

Requires live verification:

- Build, release, and signing behavior.
- Android runtime behavior and VPN service startup.
- Mihomo/core version and generated core artifacts.
- FFI headers and generated bindings.
- Subscription headers and provider profile data.
- Release workflow outputs and artifact names.

Do not treat old chats, memo files, or README claims as proof when current files or current command output disagree. Build/release/signing, Android runtime config, Mihomo version, generated FFI, subscription headers, and release workflow must be checked against current files and current commands before changing them.
