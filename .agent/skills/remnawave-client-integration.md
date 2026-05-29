# Skill: Remnawave Client Integration

Trigger:
User asks about Remnawave, HWID, panel metadata, or subscription compatibility.

Procedure:

1. Locate request path in `lib/models/profile.dart`.
2. Identify headers and device metadata.
3. Do not log sensitive identifiers.
4. Validate panel compatibility with a controlled endpoint.
5. Update `COMMAND_TRACE.md`.
