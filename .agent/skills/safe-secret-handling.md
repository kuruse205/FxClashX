# Skill: Safe Secret Handling

Trigger:
Any task touches signing, keystore, GitHub Actions secrets, Telegram token, certificates, or private config.

Procedure:

1. Show variable names only.
2. Never print values.
3. Use redacted examples.
4. Detect secret risk.
5. Do not store raw secrets in memory.
