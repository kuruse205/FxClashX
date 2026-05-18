import 'package:flclashx/common/runtime_config_security_sanitizer.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  RuntimeConfigSecuritySanitizer sanitizer({
    List<int> ports = const [55000, 55001, 55002],
    List<String> secrets = const [
      'user-token',
      'password-token',
      'secret-token',
    ],
  }) {
    var portIndex = 0;
    var secretIndex = 0;
    return RuntimeConfigSecuritySanitizer(
      portAllocator: () async => ports[portIndex++],
      secretGenerator: () => secrets[secretIndex++],
    );
  }

  test('Android default disables local proxy', () async {
    final config = <String, dynamic>{
      'mixed-port': 7890,
      'allow-lan': true,
      'bind-address': '*',
      'tun': {'enable': true},
    };

    await sanitizer().sanitize(
      config,
      const RuntimeConfigSecurityOptions(isAndroid: true),
    );

    expect(config['mixed-port'], 0);
    expect(config['socks-port'], 0);
    expect(config['port'], 0);
    expect(config['allow-lan'], isFalse);
    expect(config['bind-address'], '127.0.0.1');
    expect(config['tun'], {'enable': true});
  });

  test('Android sanitizer removes LAN exposure', () async {
    final config = <String, dynamic>{
      'allow-lan': true,
      'bind-address': '*',
      'lan-allowed-ips': [
        '127.0.0.0/8',
        '10.0.0.0/8',
        '172.16.0.0/12',
        '192.168.0.0/16',
        '::1/128',
        'fc00::/7',
      ],
    };

    await sanitizer().sanitize(
      config,
      const RuntimeConfigSecurityOptions(isAndroid: true),
    );

    expect(config['allow-lan'], isFalse);
    expect(config['bind-address'], '127.0.0.1');
    expect(config['lan-allowed-ips'], isEmpty);
    expect(config['lan-disallowed-ips'], isEmpty);
  });

  test('Android sanitizer removes auth bypass', () async {
    final config = <String, dynamic>{
      'skip-auth-prefixes': ['127.0.0.1/8', '::1/128'],
    };

    await sanitizer().sanitize(
      config,
      const RuntimeConfigSecurityOptions(isAndroid: true),
    );

    expect(config['skip-auth-prefixes'], isEmpty);
  });

  test('Android local proxy enabled requires authentication', () async {
    final config = <String, dynamic>{
      'mixed-port': 7890,
      'allow-lan': true,
      'bind-address': '*',
    };

    await sanitizer(
      ports: [55000],
      secrets: ['runtime-user', 'runtime-password'],
    ).sanitize(
      config,
      const RuntimeConfigSecurityOptions(
        isAndroid: true,
        localProxyEnabled: true,
      ),
    );

    expect(config['mixed-port'], inInclusiveRange(49152, 65535));
    expect(config['mixed-port'], 55000);
    expect(config['port'], 0);
    expect(config['socks-port'], 0);
    expect(config['allow-lan'], isFalse);
    expect(config['bind-address'], '127.0.0.1');
    final authentication = config['authentication'];
    expect(authentication, isA<List>());
    expect(authentication, hasLength(1));
    expect(authentication.single, 'runtime-user:runtime-password');
    final parts = (authentication.single as String).split(':');
    expect(parts, hasLength(2));
    expect(parts[0], isNotEmpty);
    expect(parts[1], isNotEmpty);
    expect(config['skip-auth-prefixes'], isEmpty);
    expect(config['lan-allowed-ips'], isEmpty);
    expect(config['lan-disallowed-ips'], isEmpty);
  });

  test('Subscription cannot disable local proxy authentication', () async {
    final config = <String, dynamic>{
      'mixed-port': 7890,
      'authentication': <String>[],
      'skip-auth-prefixes': ['127.0.0.1/8'],
    };

    await sanitizer(
      ports: [55000],
      secrets: ['runtime-user', 'runtime-password'],
    ).sanitize(
      config,
      const RuntimeConfigSecurityOptions(
        isAndroid: true,
        localProxyEnabled: true,
      ),
    );

    expect(config['mixed-port'], 55000);
    expect(config['authentication'], ['runtime-user:runtime-password']);
    expect(config['skip-auth-prefixes'], isEmpty);
  });

  test('Android default disables unsafe external controller config', () async {
    final config = <String, dynamic>{
      'external-controller': '0.0.0.0:9090',
      'secret': '',
    };

    await sanitizer().sanitize(
      config,
      const RuntimeConfigSecurityOptions(isAndroid: true),
    );

    expect(config.containsKey('external-controller'), isFalse);
    expect(config.containsKey('secret'), isFalse);
  });

  test('Non-Android behavior is preserved', () async {
    final config = <String, dynamic>{
      'mixed-port': 7890,
      'allow-lan': true,
      'bind-address': '*',
      'external-controller': '0.0.0.0:9090',
      'secret': '',
    };

    await sanitizer().sanitize(
      config,
      const RuntimeConfigSecurityOptions(isAndroid: false),
    );

    expect(config['mixed-port'], 7890);
    expect(config['allow-lan'], isTrue);
    expect(config['bind-address'], '*');
    expect(config['external-controller'], '0.0.0.0:9090');
    expect(config['secret'], '');
    expect(config.containsKey('authentication'), isFalse);
  });

  test('Generated credentials are only added to runtime config copy', () async {
    final persistentProfile = <String, dynamic>{
      'mixed-port': 7890,
      'external-controller': '127.0.0.1:9090',
    };
    final runtimeConfig = Map<String, dynamic>.from(persistentProfile);

    await sanitizer(
      ports: [55000, 55001],
      secrets: ['runtime-user', 'runtime-password', 'controller-secret'],
    ).sanitize(
      runtimeConfig,
      const RuntimeConfigSecurityOptions(
        isAndroid: true,
        localProxyEnabled: true,
      ),
    );

    expect(runtimeConfig['authentication'], ['runtime-user:runtime-password']);
    expect(runtimeConfig.containsKey('external-controller'), isFalse);
    expect(runtimeConfig.containsKey('secret'), isFalse);
    expect(persistentProfile.containsKey('authentication'), isFalse);
    expect(persistentProfile.containsKey('secret'), isFalse);
    expect(persistentProfile['mixed-port'], 7890);
  });
}
