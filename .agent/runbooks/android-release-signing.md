# Android Release Signing Runbook

Signing files and values are sensitive. Store variable names only.

Observed names:

- `android/app/keystore.jks`
- `android/local.properties`
- `storePassword`
- `keyAlias`
- `keyPassword`
- GitHub secrets: `KEYSTORE`, `KEY_ALIAS`, `STORE_PASSWORD`, `KEY_PASSWORD`

Rules:

1. Do not read or paste signing values.
2. Do not commit keystores, certificates, or local secret property files.
3. Release signing falls back to debug signing if configured release values are absent; verify this behavior before relying on an artifact.
4. Any signing workflow change requires a backup/rollback plan.
5. After release build, verify artifact signatures without exposing secrets.
