import 'dart:async';

import 'package:flutter/services.dart';

class ReleepScaleConnect {
  static const MethodChannel _channel = MethodChannel('releep_scale_connect');
  static const stream = EventChannel('scan_releep_scale');

  static Future<String?> get initStreamChannel async {
    final String? version = await _channel.invokeMethod('initStreamChannel');
    return version;
  }

  static Future<String?> get stopScaleScan async {
    final String? version = await _channel.invokeMethod('stopScan');
    return version;
  }

  static Future<String?> get disconnectScale async {
    final String? res = await _channel.invokeMethod('disconnect');
    return res;
  }

  static Stream get scanReleepScale => stream.receiveBroadcastStream("scan");
  static Stream get listeningReleepScaleIos =>
      stream.receiveBroadcastStream("listeningdataIOS");

  // static Stream get stopScanReleepScale => stream.receiveBroadcastStream("scan");

  static Stream get listeningReleepScale {
    const stream2 = EventChannel('listen_releep_scale');

    return stream2.receiveBroadcastStream("listeningdata");
  }

  static Future<int> connectScale({
    required String releepScaleMac,
    required int height,
    required int sex,
    required int age,
  }) async {
    // code : 0 = OK 1 = Fail 2 = Time out
    final int code = await _channel.invokeMethod('connectReleepScale', {
      'releepScaleMac': releepScaleMac,
      'height': height,
      'sex': sex,
      'age': age,
    });
    return code;
  }
}
