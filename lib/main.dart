import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:ui';

import 'package:flclashx/enum/enum.dart';
import 'package:flclashx/plugins/app.dart';
import 'package:flclashx/plugins/tile.dart';
import 'package:flclashx/plugins/vpn.dart';
import 'package:flclashx/state.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'application.dart';
import 'clash/core.dart';
import 'clash/lib.dart';
import 'common/common.dart';
import 'models/core.dart' as core_models show Action;
import 'models/models.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  if (Platform.isWindows || Platform.isLinux) {
    DartPluginRegistrant.ensureInitialized();
  }

  final version = await system.version;
  await clashCore.preload();
  await globalState.initApp(version);
  await android?.init();
  await window?.init(version);

  if (Platform.isAndroid) {
    // Accessing the singletons wires up method channel handlers.
    vpn;
    _wireAndroidTileListener();
  }

  HttpOverrides.global = FlClashHttpOverrides();
  runApp(const ProviderScope(
    child: Application(),
  ));
}

/// Handles start/stop/mode intents coming from the quick-settings tile or the
/// home-screen widget. Runs entirely in the main Flutter isolate now that the
/// `:remote` process hosts the Go core and the foreground service.
void _wireAndroidTileListener() {
  tile?.addListener(_MainTileListener());
  // Signal readiness so Kotlin can replay a pending START/STOP/CHANGE that
  // was queued while the Flutter engine was still booting (e.g. cold-start
  // from Always-on or a widget tap with no running UI).
  unawaited(tile?.signalServiceReady());
}

class _MainTileListener with TileListener {
  @override
  void onStart() {
    unawaited(_handleStart());
  }

  @override
  void onStop() {
    unawaited(_handleStop());
  }

  @override
  void onChangeMode(String mode) {
    unawaited(_handleChangeMode(mode));
  }
}

Future<void> _handleStart() async {
  try {
    unawaited(app?.tip(appLocalizations.startVpn));

    final profileId = globalState.config.currentProfileId;
    if (profileId == null) {
      unawaited(app?.tip("No profile selected"));
      return;
    }

    // Wait for _initCore to finish — it runs in addPostFrameCallback
    // concurrently with this handler. Starting VPN before core is ready
    // causes concurrent Go map access → SIGABRT.
    for (var i = 0; i < 30; i++) {
      if (await clashCore.isInit) break;
      await Future.delayed(const Duration(milliseconds: 500));
    }

    final profile = globalState.config.currentProfile;
    final title = profile?.label ?? profile?.id ?? "FlClashX";
    unawaited(clashLib?.updateNotificationParams(title: title));

    final rt = await clashLib?.startVpn() ?? 0;
    if (rt == 0) {
      commonPrint.log("Tile start: startVpn returned 0");
      unawaited(app?.tip("VPN start failed"));
      return;
    }

    await clashCore.startListener();
  } catch (e, stackTrace) {
    commonPrint.log("Tile onStart error: $e\n$stackTrace");
    unawaited(app?.tip("Start error: $e"));
  }
}

Future<void> _handleStop() async {
  try {
    unawaited(app?.tip(appLocalizations.stopVpn));
    await clashCore.stopListener();
    await clashLib?.stopVpn();
  } catch (e) {
    commonPrint.log("Tile onStop error: $e");
  }
}

Future<void> _handleChangeMode(String mode) async {
  try {
    final modeEnum = Mode.values.byName(mode);
    final patched = globalState.config.patchClashConfig.copyWith(
      mode: modeEnum,
    );
    globalState.config = globalState.config.copyWith(
      patchClashConfig: patched,
    );
    await preferences.saveConfig(globalState.config);

    final updateParamsMap = UpdateParams(
      tun: patched.tun.getRealTun(globalState.config.networkProps.routeMode),
      allowLan: patched.allowLan,
      findProcessMode: patched.findProcessMode,
      mode: modeEnum,
      logLevel: patched.logLevel,
      ipv6: patched.ipv6,
      tcpConcurrent: patched.tcpConcurrent,
      externalController: patched.externalController,
      unifiedDelay: patched.unifiedDelay,
      mixedPort: patched.mixedPort,
    ).toJson();

    final effective = globalState.effectiveExternalController.value;
    if (effective.isNotEmpty) {
      updateParamsMap['external-controller'] = effective;
    }

    final actionJson = json.encode(
      core_models.Action(
        id: "${ActionMethod.updateConfig.name}#${utils.id}",
        method: ActionMethod.updateConfig,
        data: json.encode(updateParamsMap),
      ),
    );
    unawaited(clashLib?.sendMessage(actionJson));
    unawaited(tile?.updateMode(mode));
  } catch (e) {
    commonPrint.log("Tile onChangeMode error: $e");
  }
}
