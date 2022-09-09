import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:releep_scale_connect/releep_scale_connect.dart';

void main() {
  const MethodChannel channel = MethodChannel('releep_scale_connect');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await ReleepScaleConnect.platformVersion, '42');
  });
}
