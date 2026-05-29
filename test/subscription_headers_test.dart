import 'package:dio/dio.dart';
import 'package:flclashx/common/subscription_headers.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  Headers headers(Map<String, List<String>> values) => Headers.fromMap(values);

  test('collects Remnawave headers and keeps x-hwid-limit alias', () {
    final collected = collectProviderHeaders(headers({
      'announce': ['base64:notice'],
      'support-url': ['https://support.example'],
      'profile-update-interval': ['6'],
      'x-hwid-active': ['true'],
      'x-hwid-not-supported': ['false'],
      'x-hwid-max-devices-reached': ['true'],
      'x-hwid-limit': ['true'],
    }));

    expect(collected['announce'], 'base64:notice');
    expect(collected['support-url'], 'https://support.example');
    expect(collected['profile-update-interval'], '6');
    expect(collected['x-hwid-active'], 'true');
    expect(collected['x-hwid-not-supported'], 'false');
    expect(collected['x-hwid-max-devices-reached'], 'true');
    expect(collected['x-hwid-limit'], 'true');
    expect(isHwidLimitReached(collected), isTrue);
  });

  test('normalizes flclashx headers to lowercase', () {
    final collected = collectProviderHeaders(headers({
      'FlClashX-NewDomain': ['new.example.com'],
      'FLCLaSHX-ServiceName': ['service'],
    }));

    expect(collected['flclashx-newdomain'], 'new.example.com');
    expect(collected['flclashx-servicename'], 'service');
  });

  test('merge keeps old flclashx headers but drops stale HWID state', () {
    final merged = mergeProviderHeaders(
      previousHeaders: {
        'flclashx-servicename': 'old',
        'x-hwid-max-devices-reached': 'true',
      },
      responseHeaders: {
        'flclashx-view': 'list',
      },
    );

    expect(merged['flclashx-servicename'], 'old');
    expect(merged['flclashx-view'], 'list');
    expect(merged.containsKey('x-hwid-max-devices-reached'), isFalse);
  });

  test('domain redirect changes only host and is idempotent', () {
    const url = 'https://old.example:8443/path/to/sub?token=1#frag';
    final headers = {'flclashx-newdomain': 'new.example'};

    final updated = applySubscriptionDomainRedirect(
      currentUrl: url,
      providerHeaders: headers,
    );
    final updatedAgain = applySubscriptionDomainRedirect(
      currentUrl: updated,
      providerHeaders: headers,
    );

    expect(updated, 'https://new.example:8443/path/to/sub?token=1#frag');
    expect(updatedAgain, updated);
  });

  test('domain redirect ignores unsafe header values', () {
    const url = 'https://old.example/path?token=1';

    for (final invalidHost in [
      'https://new.example',
      'new.example/path',
      'new.example?x=1',
      'new.example#frag',
      'new.example:443',
      ' new.example',
      'new.example ',
    ]) {
      expect(
        applySubscriptionDomainRedirect(
          currentUrl: url,
          providerHeaders: {'flclashx-newdomain': invalidHost},
        ),
        url,
      );
    }
  });
}
