const xhttpDelayWarningMessage =
    'XHTTP transport detected. Delay/ping test may be inaccurate, especially for stream-up/packet-up.';

class XhttpTransportInfo {
  const XhttpTransportInfo({
    required this.proxyName,
    required this.mode,
  });

  final String proxyName;
  final String mode;

  bool get hasRiskyDelayMode => mode == 'packet-up' || mode == 'stream-up';

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is XhttpTransportInfo &&
          runtimeType == other.runtimeType &&
          proxyName == other.proxyName &&
          mode == other.mode;

  @override
  int get hashCode => Object.hash(proxyName, mode);
}

Map<String, XhttpTransportInfo> detectXhttpTransports(
  Map<String, dynamic> rawConfig,
) {
  final proxies = rawConfig['proxies'];
  if (proxies is! List) return {};

  final metadata = <String, XhttpTransportInfo>{};
  for (final proxy in proxies) {
    if (proxy is! Map) continue;
    final name = proxy['name'];
    if (name is! String || name.isEmpty) continue;

    final type = proxy['type']?.toString().toLowerCase();
    final network = proxy['network']?.toString().toLowerCase();
    if (type != 'vless' || network != 'xhttp') continue;

    final xhttpOpts = proxy['xhttp-opts'];
    final rawMode = xhttpOpts is Map ? xhttpOpts['mode']?.toString() : null;
    final mode = _normalizeXhttpMode(rawMode);
    metadata[name] = XhttpTransportInfo(proxyName: name, mode: mode);
  }

  return metadata;
}

String xhttpTransportSignature(Map<String, XhttpTransportInfo> metadata) {
  final entries = metadata.entries.toList()
    ..sort((a, b) => a.key.compareTo(b.key));
  return entries.map((entry) => '${entry.key}:${entry.value.mode}').join('|');
}

String _normalizeXhttpMode(String? mode) {
  final normalized = mode?.trim().toLowerCase();
  return switch (normalized) {
    'stream-one' || 'stream-up' || 'packet-up' || 'auto' => normalized!,
    _ => 'auto',
  };
}
