# Update Gates

## Before any app/core/security/build change

1. Check git status.
2. Check current branch.
3. Check current version/tag.
4. Check local patches.
5. Check generated files.
6. Check Flutter/Dart version.
7. Check build_runner/generated code requirements.
8. Check Mihomo/core version and FFI headers.
9. Check Android runtime security tests or validation plan.
10. Check subscription/header sample inputs.
11. Check platform impact: Android, Windows, Linux, macOS.
12. Check signing/release secrets are not touched.
13. Create rollback plan.
14. Prefer minimal diff.

## After any app/core/security/build change

1. Run format/analyze if available.
2. Run tests if available.
3. Run build target if relevant.
4. Validate Android runtime config safety if Android touched.
5. Validate subscription header parsing if profile/subscription touched.
6. Validate FFI/core bridge if core touched.
7. Validate affected platform behavior.
8. Verify no secrets in logs or markdown.
9. Update `COMMAND_TRACE.md`.
10. Update `PROJECT_STATE.md` if durable state changed.
11. Update `LOCAL_PATCHES.md` if patch/drift changed.
12. Update `PROJECT_CUTOFF.md` if freshness changed.

## Never do blindly

- Edit signing secrets.
- Commit keystore/certificates.
- Edit generated files without documenting generator.
- Weaken Android local proxy sanitizer.
- Expose unauthenticated local proxy.
- Break subscription compatibility.
- Change release pipeline without rollback.
- Mass rename package/app identifiers without release plan.
