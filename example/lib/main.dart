import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:flutter_intent/flutter_intent.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

  @override
  void initState() {
    super.initState();
  }

  Future<void> checkIntent() async {
    if (Platform.isAndroid) {
      FlutterIntent intent = FlutterIntent(
        action: 'action_send',
        //data: Uri.encodeFull('https://flutter.io'),
        package: 'com.skype.m2',
      );
      await intent.launch();
    }
  }

  goWithLinkToTelegram() async {
    if (Platform.isAndroid) {
      FlutterIntent intent = FlutterIntent(
          action: 'action_send',
          package: 'com.viber.voip',
          arguments: {'android.intent.extra.TEXT': "reflink"},
          type: 'text/plain'
      );
      await intent.launch();
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              Text('Running on: $_platformVersion\n'),
              FlatButton(
                onPressed: checkIntent,
                child: Text('GO'),
              ),
              FlatButton(
                onPressed: goWithLinkToTelegram,
                child: Text('GO'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
