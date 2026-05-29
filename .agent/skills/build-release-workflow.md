# Skill: Build Release Workflow

Trigger:
User asks to build APK/desktop release, change workflow, signing, or artifact names.

Procedure:

1. Load `UPDATE_GATES.md` and release runbook.
2. Do not expose secrets.
3. Check platform matrix.
4. Validate `setup.dart`.
5. Run dry build if possible.
6. Document artifacts.
