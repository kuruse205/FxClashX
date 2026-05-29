# Skill: Mihomo Core Workflow

Trigger:
User changes core, FFI, `setup.dart`, `libclash`, core version, or build tags.

Procedure:

1. Locate source of truth.
2. Check generated files.
3. Validate FFI headers/bindings.
4. Build minimal target.
5. Update `LOCAL_PATCHES.md` if custom behavior changes.
