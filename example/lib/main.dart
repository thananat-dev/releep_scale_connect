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
  String _responseData = 'Unknown';

  late StreamSubscription _ReleepScaleScanSubscription;
  StreamSubscription? _ReleepScaleListenDataSubscription;

  var _listScale = [];
  final TextEditingController _resReleepScale = TextEditingController();
  var scaleWeight = 0.0;

  @override
  void initState() {
    super.initState();
    initStreamChannel();
    _resReleepScale.text = "no response";
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initStreamChannel() async {
    String responseData;
    try {
      responseData = await ReleepScaleConnect.initStreamChannel ??
          'Unknown platform version';
    } on PlatformException {
      responseData = 'Failed to get platform version.';
    }
    if (!mounted) return;

    setState(() {
      _responseData = responseData;
    });
  }

  Future<void> stopScanScale() async {
    String responseData;
    try {
      responseData = await ReleepScaleConnect.stopScaleScan ??
          'Unknown stopScanScale Method';
      _listScale = [];
    } on PlatformException {
      responseData = 'Failed to stopScanScale.';
    }
    if (!mounted) return;

    setState(() {
      _responseData = responseData;
    });
  }

  Future<void> disconnect() async {
    String responseData;
    try {
      responseData = await ReleepScaleConnect.disconnectScale ??
          'Unknown disconnectScale Method';
      // _listScale = [];
    } on PlatformException {
      responseData = 'Failed to disconnectScale.';
    }
    if (!mounted) return;

    setState(() {
      _responseData = responseData;
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
    if (_ReleepScaleListenDataSubscription != null) {
      _ReleepScaleListenDataSubscription?.cancel();
    }
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

  Future<void> _connectReleepScale(scaleMac) async {
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
                        onPressed: disconnect, child: const Text("disconnect")),
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
