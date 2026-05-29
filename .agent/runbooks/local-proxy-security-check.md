# Local Proxy Security Check Runbook

Threat model:

- Malicious local apps on Android may try to access exposed HTTP/SOCKS proxy listeners.
- Subscription YAML may try to enable LAN access, unauthenticated mixed-port, unsafe bind address, or auth bypass prefixes.

Checks:

1. Verify local proxy listener state in runtime config.
2. Confirm loopback binding.
3. Confirm auth exists when local proxy is enabled.
4. Confirm mixed-port is disabled when local proxy is disabled.
5. Confirm ephemeral port range.
6. Confirm logs do not print generated credentials.
7. Run runtime security sanitizer tests.
