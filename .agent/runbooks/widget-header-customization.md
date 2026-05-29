# Widget Header Customization Runbook

Header families to check:

- `flclashx-widgets`
- `flclashx-view`
- `flclashx-custom`
- `flclashx-denywidgets`
- `flclashx-servicename`
- `flclashx-servicelogo`
- `flclashx-serverinfo`
- `flclashx-background`
- `flclashx-settings`
- `flclashx-globalmode`
- `flclashx-hex`
- `flclashx-androidsecure`

Validation samples:

1. Missing header: fallback should be stable.
2. Invalid base64: raw fallback should be safe.
3. Unknown widget name: ignored or safely skipped.
4. Missing proxy group for server info: fallback button remains usable.
5. Deny editing enabled: dashboard editing blocked only where intended.

Relevant paths include `lib/controller.dart`, `lib/providers/state.dart`, `lib/views/dashboard/`, and `lib/views/proxies/`.
