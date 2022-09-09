import 'dart:convert';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:releep_scale_connect/releep_scale_connect.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  late StreamSubscription _ReleepScaleScanSubscription;
  late StreamSubscription _ReleepScaleListenDataSubscription;

  var _listScale = [];
  final TextEditingController _resReleepScale = TextEditingController();
  var scaleWeight = 0.0;

  @override
  void initState() {
    super.initState();
    initPlatformState();
    _resReleepScale.text = "no response";
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await ReleepScaleConnect.platformVersion ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future<void> stopScanScale() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion = await ReleepScaleConnect.stopScaleScan ??
          'Unknown stopScanScale Method';
      _listScale = [];
    } on PlatformException {
      platformVersion = 'Failed to stopScanScale.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  void _startScaleScan() {
    setState(() {
      _listScale = [];
    });
    debugPrint("_startWatchScan");
    _ReleepScaleScanSubscription =
        ReleepScaleConnect.scanReleepScale.listen((event) => {
              debugPrint(event),
              setState(() {
                var json = jsonDecode(event);
                _listScale = json;
              })
            });
  }

  void _listenScaleScan() {
    debugPrint("_listenScaleScan");
    _ReleepScaleListenDataSubscription =
        ReleepScaleConnect.listeningReleepScale.listen((event) => {
              debugPrint(event),
              setState(() {
                _resReleepScale.text = event.toString();
                var json = jsonDecode(event.toString());
                scaleWeight = (json["weight"] ?? 0.0) / 10;
              }),
            });
  }

  Future<Null> _connectReleepScale(scaleMac) async {
    int code = await ReleepScaleConnect.connectScale(scaleMac);
    _cancelWatchScan();
    debugPrint("connect Res ${code}");
    setState(() {
      _resReleepScale.text = code.toString();
    });
  }

  void _cancelWatchScan() {
    _ReleepScaleScanSubscription.cancel();
    // _ReleepScaleListenDataSubscription.cancel();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
          appBar: AppBar(
            title: const Text('Plugin example app'),
          ),
          body: SingleChildScrollView(
            physics: const ScrollPhysics(),
            child: Column(
              children: [
                Wrap(
                  spacing: 10.0,
                  children: <Widget>[
                    ElevatedButton(
                        onPressed: stopScanScale,
                        child: const Text("Stop Scan")),
                    ElevatedButton(
                        onPressed: _listenScaleScan,
                        child: const Text("_listenScaleScan")),
                  ],
                ),
                Text(
                  scaleWeight.toString(),
                  style: Theme.of(context).textTheme.headline1,
                ),
                Wrap(
                  children: [
                    Text("res :"),
                  ],
                ),
                TextField(
                  maxLines: null,
                  keyboardType: TextInputType.multiline,
                  controller: _resReleepScale,
                ),
                ListView.builder(
                  shrinkWrap: true,
                  itemCount: _listScale.length,
                  itemBuilder: (context, index) {
                    return ListTile(
                      title: Text('${_listScale[index]['name']}' +
                          ' | ' +
                          '${_listScale[index]['address']}'),
                      onTap: () => {
                        _connectReleepScale('${_listScale[index]['address']}'),
                      },
                    );
                  },
                ),
              ],
            ),
          ),
          floatingActionButton: FloatingActionButton(
            backgroundColor: Colors.green,
            onPressed: _startScaleScan,
            child: const Icon(Icons.search),
          )),
    );
  }
}
