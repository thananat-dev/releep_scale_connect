# Releep scale SDK compatibility fork

Vendored from `releep_scale_connect` 1.0.1 (see LICENSE). Dart and iOS
interfaces are retained. Android changes:

- Accept the base Android `Activity`, including `FlutterFragmentActivity`,
  instead of casting the host to `FlutterActivity` during registration.
- Request permissions and Bluetooth activation through the app's scan flow,
  rather than during plugin registration.
- Release the previous discovery callback when a new listener starts; stop
  discovery and remove pending timers when a listener cancels.
- Report Bluetooth, permission, SDK initialization and scan-start errors
  through the discovery EventChannel.

The application uses this path dependency so fixes survive SDK cache
refreshes and CI builds. Native changes require a full Android rebuild.
