# Skill: Safe Flutter Editing

Trigger:
User asks to change UI, settings, screens, widgets, profile cards, or app behavior.

Procedure:

1. Load `PROJECT_STATE.md`, `UPDATE_GATES.md`, and `LOCAL_PATCHES.md`.
2. Locate the exact widget/provider/model.
3. Make a minimal diff.
4. Run format/analyze/test if available and relevant.
5. Validate affected platform behavior.
6. Update memory after meaningful work.
