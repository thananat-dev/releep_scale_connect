# releep_scale_connect

Flutter plugin for discovering and connecting to Releep/Aifit Bluetooth scales on Android and iOS.

## Installation

Add the package to your app:

```yaml
dependencies:
  releep_scale_connect: ^1.0.1
```

Then run `flutter pub get`.

## Usage

Start listening for nearby scales, connect to one using its Bluetooth address, and subscribe to the measurement stream. Stream values are JSON strings.

```dart
import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:releep_scale_connect/releep_scale_connect.dart';

Future<void> connectToScale() async {
  // Required on Android before subscribing to its streams.
  if (Platform.isAndroid) {
    await ReleepScaleConnect.initStreamChannel;
  }

  final StreamSubscription scanSubscription =
      ReleepScaleConnect.scanReleepScale.listen((event) async {
    final devices = jsonDecode(event as String) as List<dynamic>;
    if (devices.isEmpty) return;

    final address = devices.first['address'] as String;
    await ReleepScaleConnect.connectScale(
      releepScaleMac: address,
      height: 175, // cm
      sex: 1,
      age: 20,
    );
  });

  final measurementStream = Platform.isIOS
      ? ReleepScaleConnect.listeningReleepScaleIos
      : ReleepScaleConnect.listeningReleepScale;

  final StreamSubscription measurementSubscription =
      measurementStream.listen((event) {
    final measurement = jsonDecode(event as String) as Map<String, dynamic>;
    // Android weight uses `weight`; iOS uses `weightsum`.
    final rawWeight = measurement[Platform.isIOS ? 'weightsum' : 'weight'];
    print('Raw scale weight: $rawWeight');
  });

  // Keep and cancel both subscriptions from your widget/service dispose method.
  // await scanSubscription.cancel();
  // await measurementSubscription.cancel();
}
```

`connectScale` returns `0` for success, `1` for failure, and `2` for timeout.

Use `await ReleepScaleConnect.stopScaleScan` to stop discovery and `await ReleepScaleConnect.disconnectScale` to disconnect the current scale.

## Platform configuration

The host app must declare and request the Bluetooth permissions required by its Android and iOS deployment targets. On Android, this normally includes the Bluetooth scan/connect permissions for Android 12+ and the appropriate location permission for older Android versions. Add the corresponding Bluetooth usage description keys to the iOS app's `Info.plist`.

See the [example app](example/) for a complete integration.
