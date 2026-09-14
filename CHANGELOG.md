## 1.0.2

* Improve Android compatibility by accepting the host `Activity`, including `FlutterFragmentActivity`.
* Move Bluetooth permission and availability checks into the scan flow instead of plugin registration.
* Improve scan lifecycle handling by resetting stale callbacks, cancelling pending timers, and ignoring results from cancelled scans.
* Report Bluetooth, permission, SDK initialization, and scan-start failures through the discovery EventChannel.
* Update native SDK integration and Android build configuration.

## 1.0.1

* Fix Android plugin teardown to safely clear the method channel when the Flutter engine detaches, preventing a potential crash.

## 1.0.0

* Update native scale SDKs.

## 0.0.10

* Update SDK version.

## 0.0.9

* Add iOS scale body-fat and weight data.

## 0.0.8

* Fix scale body-fat data and weight.

## 0.0.7

* Fix scale scanning.

## 0.0.6

* Update SDK version.

## 0.0.5

* Add `initStreamChannel`.

## 0.0.4

* Add `unregisterBoardcast`.

## 0.0.3

* Add disconnect support.

## 0.0.2

* Fix stopping a scan.

## 0.0.1

* Initial release.
