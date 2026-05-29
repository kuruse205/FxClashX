import 'dart:io';
import 'dart:math';

const int runtimeProxyMinPort = 49152;
const int runtimeProxyMaxPort = 65535;

class RuntimeConfigSecurityOptions {
  const RuntimeConfigSecurityOptions({
    required this.isAndroid,
    this.localProxyEnabled = false,
  });

  final bool isAndroid;
  final bool localProxyEnabled;
}

typedef RuntimePortAllocator = Future<int> Function();
typedef RuntimeSecretGenerator = String Function();

class RuntimeConfigSecuritySanitizer {
  const RuntimeConfigSecuritySanitizer({
    RuntimePortAllocator? portAllocator,
    RuntimeSecretGenerator? secretGenerator,
  })  : _portAllocator = portAllocator ?? _allocateEphemeralLoopbackPort,
        _secretGenerator = secretGenerator ?? _generateRuntimeToken;

  final RuntimePortAllocator _portAllocator;
  final RuntimeSecretGenerator _secretGenerator;

  Future<Map<String, dynamic>> sanitize(
    Map<String, dynamic> config,
    RuntimeConfigSecurityOptions options,
  ) async {
    if (!options.isAndroid) {
      return config;
    }

    config['allow-lan'] = false;
    config['bind-address'] = '127.0.0.1';
    config['port'] = 0;
    config['socks-port'] = 0;
    config['lan-allowed-ips'] = <String>[];
    config['lan-disallowed-ips'] = <String>[];
    config['skip-auth-prefixes'] = <String>[];

    if (options.localProxyEnabled) {
      config['mixed-port'] = await _portAllocator();
      config['authentication'] = [
        '${_secretGenerator()}:${_secretGenerator()}',
      ];
    } else {
      config['mixed-port'] = 0;
      config.remove('authentication');
    }

    _disableExternalController(config);
    return config;
  }

  void _disableExternalController(Map<String, dynamic> config) {
    config
      ..remove('external-controller')
      ..remove('secret');
  }
}

Future<int> _allocateEphemeralLoopbackPort() async {
  final random = Random.secure();
  for (var attempt = 0; attempt < 32; attempt++) {
    final port = runtimeProxyMinPort +
        random.nextInt(runtimeProxyMaxPort - runtimeProxyMinPort + 1);
    ServerSocket? socket;
    try {
      socket = await ServerSocket.bind(
        InternetAddress.loopbackIPv4,
        port,
        shared: false,
      );
      return port;
    } on SocketException {
      continue;
    } finally {
      await socket?.close();
    }
  }
  throw const SocketException('No free loopback port in ephemeral range');
}

String _generateRuntimeToken() {
  final random = Random.secure();
  const chars =
      'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  return List.generate(
    32,
    (_) => chars[random.nextInt(chars.length)],
    growable: false,
  ).join();
}
