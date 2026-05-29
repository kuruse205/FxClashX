# Error History

## 1. Android local proxy security regression

Symptoms:
Android runtime config exposes local HTTP/SOCKS proxy, allows LAN, unsafe bind-address, or skip-auth-prefixes.

Meaning:
Security sanitizer or runtime config patching may be bypassed.

Correct diagnostic:
Check generated runtime config, sanitizer tests, Android startup path, and profile override logic.

Wrong fix:
Do not rely on subscription YAML being safe; runtime sanitizer must enforce safety.

Status:
active

## 2. Subscription headers not applied

Symptoms:
Widgets, service name, support URL, profile style, server info, or settings are missing after adding/updating subscription.

Meaning:
Header parsing, base64 decoding, profile metadata persistence, or update policy may be broken.

Correct diagnostic:
Check raw response headers, providerHeaders storage, parser, update path, and UI consumer.

Wrong fix:
Do not hardcode UI values before confirming parser path.

Status:
verify-live-first

## 3. Remnawave HWID/client metadata not sent

Symptoms:
Panel does not receive expected HWID or client metadata.

Meaning:
Client request headers/metadata path may not execute or may be platform-specific.

Correct diagnostic:
Check subscription request builder, platform/device info, headers, and Remnawave compatibility.

Wrong fix:
Do not expose device identifiers in logs.

Status:
verify-live-first

## 4. Mihomo core starts but app state is wrong

Symptoms:
Core starts, but UI mode, traffic, selected profile, DNS, or VPN state is inconsistent.

Meaning:
FFI bridge, service isolate, state sync, or IPC may be out of sync.

Correct diagnostic:
Check service isolate logs, core action calls, state provider, and generated setup params.

Wrong fix:
Do not restart/kill the whole app as first fix.

Status:
verify-live-first

## 5. Generated FFI file mismatch

Symptoms:
Build fails or runtime calls break after core/header changes.

Meaning:
Generated FFI bindings may not match libclash headers.

Correct diagnostic:
Check ffigen config, libclash headers, generated file, and build_runner/ffigen workflow.

Wrong fix:
Do not manually edit generated FFI unless explicitly required and documented.

Status:
active

## 6. Release signing secret risk

Symptoms:
Workflow, logs, local files, or markdown contain signing secrets, keystore content, certificate values, Telegram token, or GitHub secret values.

Meaning:
Secret redaction failed or signing material was committed/logged.

Correct diagnostic:
Find output path and sanitize/redact.

Wrong fix:
Do not paste raw logs/secrets into `.agent`.

Status:
active

## 7. Build pipeline fails only on one platform

Symptoms:
Android/Windows/Linux/macOS build fails while others pass.

Meaning:
Platform-specific dependency, signing, core artifact, helper service, or Flutter channel issue.

Correct diagnostic:
Check platform matrix, setup.dart target, dependencies, core build, signing branch, and artifact path.

Wrong fix:
Do not change shared code before isolating platform-specific failure.

Status:
verify-live-first
