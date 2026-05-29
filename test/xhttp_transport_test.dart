import 'package:flclashx/common/xhttp_transport.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  test('detects vless xhttp packet-up proxy', () {
    final config = {
      'proxies': [
        {
          'name': 'xhttp-node',
          'type': 'vless',
          'network': 'xhttp',
          'xhttp-opts': {'mode': 'packet-up'},
        },
      ],
    };

    final metadata = detectXhttpTransports(config);

    expect(metadata.keys, ['xhttp-node']);
    expect(metadata['xhttp-node']?.mode, 'packet-up');
    expect(metadata['xhttp-node']?.hasRiskyDelayMode, isTrue);
  });

  test('supports stream-up stream-one and auto modes', () {
    final metadata = detectXhttpTransports({
      'proxies': [
        {
          'name': 'stream-up',
          'type': 'VLESS',
          'network': 'XHTTP',
          'xhttp-opts': {'mode': 'stream-up'},
        },
        {
          'name': 'stream-one',
          'type': 'vless',
          'network': 'xhttp',
          'xhttp-opts': {'mode': 'stream-one'},
        },
        {
          'name': 'auto',
          'type': 'vless',
          'network': 'xhttp',
          'xhttp-opts': {'mode': 'auto'},
        },
      ],
    });

    expect(metadata['stream-up']?.mode, 'stream-up');
    expect(metadata['stream-one']?.mode, 'stream-one');
    expect(metadata['auto']?.mode, 'auto');
  });

  test('normalizes missing or unknown mode to auto', () {
    final metadata = detectXhttpTransports({
      'proxies': [
        {
          'name': 'missing',
          'type': 'vless',
          'network': 'xhttp',
        },
        {
          'name': 'unknown',
          'type': 'vless',
          'network': 'xhttp',
          'xhttp-opts': {'mode': 'future-mode'},
        },
      ],
    });

    expect(metadata['missing']?.mode, 'auto');
    expect(metadata['unknown']?.mode, 'auto');
  });

  test('ignores non-XHTTP proxies and does not mutate input', () {
    final config = {
      'proxies': [
        {
          'name': 'ws',
          'type': 'vless',
          'network': 'ws',
        },
        {
          'name': 'trojan-xhttp',
          'type': 'trojan',
          'network': 'xhttp',
        },
      ],
    };
    final snapshot = config.toString();

    final metadata = detectXhttpTransports(config);

    expect(metadata, isEmpty);
    expect(config.toString(), snapshot);
  });

  test('builds deterministic metadata signature', () {
    final signature = xhttpTransportSignature({
      'b': const XhttpTransportInfo(proxyName: 'b', mode: 'stream-up'),
      'a': const XhttpTransportInfo(proxyName: 'a', mode: 'packet-up'),
    });

    expect(signature, 'a:packet-up|b:stream-up');
  });
}
