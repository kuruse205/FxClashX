import 'package:dio/dio.dart';

const remnawaveHwidActiveHeader = 'x-hwid-active';
const remnawaveHwidNotSupportedHeader = 'x-hwid-not-supported';
const remnawaveHwidMaxDevicesReachedHeader = 'x-hwid-max-devices-reached';
const remnawaveHwidLimitHeader = 'x-hwid-limit';
const flClashXNewDomainHeader = 'flclashx-newdomain';

const _responseStateHeaders = {
  remnawaveHwidActiveHeader,
  remnawaveHwidNotSupportedHeader,
  remnawaveHwidMaxDevicesReachedHeader,
  remnawaveHwidLimitHeader,
};

Map<String, String> collectProviderHeaders(Headers headers) {
  final providerHeaders = <String, String>{};
  const headersToCollect = [
    'announce',
    'support-url',
    'profile-update-interval',
    remnawaveHwidActiveHeader,
    remnawaveHwidNotSupportedHeader,
    remnawaveHwidMaxDevicesReachedHeader,
    remnawaveHwidLimitHeader,
  ];

  for (final headerName in headersToCollect) {
    final value = headers.value(headerName);
    if (value != null && value.isNotEmpty) {
      providerHeaders[headerName] = value;
    }
  }

  headers.forEach((name, values) {
    final normalizedName = name.toLowerCase();
    if (normalizedName.startsWith('flclashx-') && values.isNotEmpty) {
      providerHeaders[normalizedName] = values.first;
    }
  });

  return providerHeaders;
}

Map<String, String> mergeProviderHeaders({
  required Map<String, String> previousHeaders,
  required Map<String, String> responseHeaders,
}) {
  final mergedHeaders = <String, String>{};

  for (final entry in previousHeaders.entries) {
    final normalizedKey = entry.key.toLowerCase();
    if (normalizedKey.startsWith('flclashx-')) {
      mergedHeaders[normalizedKey] = entry.value;
    }
  }

  for (final entry in responseHeaders.entries) {
    mergedHeaders[entry.key.toLowerCase()] = entry.value;
  }

  for (final responseStateHeader in _responseStateHeaders) {
    if (!responseHeaders.containsKey(responseStateHeader)) {
      mergedHeaders.remove(responseStateHeader);
    }
  }

  return mergedHeaders;
}

String applySubscriptionDomainRedirect({
  required String currentUrl,
  required Map<String, String> providerHeaders,
}) {
  final newHost = providerHeaders[flClashXNewDomainHeader];
  if (!_isSafeBareHost(newHost)) return currentUrl;

  final currentUri = Uri.tryParse(currentUrl);
  if (currentUri == null || currentUri.host.isEmpty) return currentUrl;
  if (currentUri.host.toLowerCase() == newHost!.toLowerCase()) {
    return currentUrl;
  }

  return currentUri.replace(host: newHost).toString();
}

bool isHwidLimitReached(Map<String, String> providerHeaders) =>
    _isTrue(providerHeaders[remnawaveHwidMaxDevicesReachedHeader]) ||
    _isTrue(providerHeaders[remnawaveHwidLimitHeader]);

bool isHwidNotSupported(Map<String, String> providerHeaders) =>
    _isTrue(providerHeaders[remnawaveHwidNotSupportedHeader]);

bool _isTrue(String? value) => value?.trim().toLowerCase() == 'true';

bool _isSafeBareHost(String? value) {
  if (value == null || value.isEmpty) return false;
  if (value.trim() != value) return false;
  if (RegExp(r'\s').hasMatch(value)) return false;
  if (value.contains('://') ||
      value.contains('/') ||
      value.contains('?') ||
      value.contains('#') ||
      value.contains('@') ||
      value.contains(':')) {
    return false;
  }
  final parsed = Uri.tryParse('https://$value');
  return parsed != null && parsed.host == value;
}
