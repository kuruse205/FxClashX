# Agent Context Index

This directory is a compatibility memory layer for tools that expect `./agent`.
The detailed source of truth remains `.agent/`; the short entry point is `agent.md`.

Read order:

1. `../agent.md`
2. `CURRENT_STATE.md`
3. `BUILD_ARTIFACTS.md`
4. `HANDOFF.md`
5. `../.agent/MEMORY_INDEX.md`

Rules:

- Live repository state wins over memory.
- Do not store secrets.
- Keep product-visible naming as `FxClashX`.
- Keep compatibility identifiers unless a migration plan explicitly says otherwise.
