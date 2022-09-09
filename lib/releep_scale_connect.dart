import 'dart:async';

import 'package:flutter/services.dart';

class ReleepScaleConnect {
  static const MethodChannel _channel = MethodChannel('releep_scale_connect');
  static const stream = EventChannel('scan_releep_scale');
  static const stream2 = EventChannel('listen_releep_scale');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> get stopScaleScan async {
    final String? version = await _channel.invokeMethod('stopScan');
    return version;
  }

  static Stream get scanReleepScale => stream.receiveBroadcastStream("scan");

  static Stream get listeningReleepScale =>
      stream2.receiveBroadcastStream("listeningdata");

  static Future<int> connectScale(macAddress) async {
    // code : 0 = OK 1 = Fail 2 = Time out
    final int code = await _channel
        .invokeMethod('connectReleepScale', {'releepScaleMac': macAddress});
    return code;
  }
}
