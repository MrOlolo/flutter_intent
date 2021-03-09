import 'dart:async';
import 'package:flutter/services.dart';
import 'package:meta/meta.dart';
import 'package:platform/platform.dart';

const String kChannelName = 'com.mrololo/flutter_intent';

class FlutterIntent {
  /// Builds an Android intent with the following parameters
  /// [action] refers to the action parameter of the intent.
  /// [flags] is the list of int that will be converted to native flags.
  /// [category] refers to the category of the intent, can be null.
  /// [data] refers to the string format of the URI that will be passed to
  /// intent.
  /// [arguments] is the map that will be converted into an extras bundle and
  /// passed to the intent.
  /// [package] refers to the package parameter of the intent, can be null.
  /// [componentName] refers to the component name of the intent, can be null.
  /// If not null, then [package] but also be provided.
  FlutterIntent({
    required this.action,
    this.flags,
    this.category,
    this.data,
    this.arguments,
    this.package,
    this.componentName,
    this.type,
    Platform? platform,
  })  : _channel = const MethodChannel(kChannelName),
        _platform = platform ?? const LocalPlatform();

  @visibleForTesting
  FlutterIntent.private({
    required this.action,
    required Platform platform,
    required MethodChannel channel,
    this.flags,
    this.category,
    this.data,
    this.arguments,
    this.package,
    this.componentName,
    this.type,
  })  : _channel = channel,
        _platform = platform;

  final String action;
  final List<int>? flags;
  final String? category;
  final String? data;
  final Map<String, dynamic>? arguments;
  final String? package;
  final String? componentName;
  final MethodChannel _channel;
  final Platform _platform;
  final String? type;

  bool _isPowerOfTwo(int x) {
    /* First x in the below expression is for the case when x is 0 */
    return x != 0 && ((x & (x - 1)) == 0);
  }

  @visibleForTesting
  int convertFlags(List<int> flags) {
    int finalValue = 0;
    for (int i = 0; i < flags.length; i++) {
      if (!_isPowerOfTwo(flags[i])) {
        throw ArgumentError.value(flags[i], 'flag\'s value must be power of 2');
      }
      finalValue |= flags[i];
    }
    return finalValue;
  }

  /// Launch the intent.
  ///
  /// This works only on Android platforms.
  Future<void> startActivity() async {
    if (!_platform.isAndroid) {
      return;
    }
    if (action == null) {
      return;
    }
    final Map<String, dynamic> args = <String, dynamic>{'action': action};
    if (flags != null) {
      args['flags'] = convertFlags(flags!);
    }
    if (category != null) {
      args['category'] = category;
    }
    if (data != null) {
      args['data'] = data;
    }
    if (arguments != null) {
      args['arguments'] = arguments;
    }
    if (package != null) {
      args['package'] = package;
      if (componentName != null) {
        args['componentName'] = componentName;
      }
    }
    if (type != null) {
      args['type'] = type;
    }
    await _channel.invokeMethod<void>('startActivity', args);
  }

  Future<void> startShareActivity({String? name}) async {
    if (!_platform.isAndroid) {
      return;
    }
    final Map<String, dynamic> args = <String, dynamic>{
      'action': 'action_send'
    };
    if (flags != null) {
      args['flags'] = convertFlags(flags!);
    }
    if (category != null) {
      args['category'] = category;
    }
    if (data != null) {
      args['data'] = data;
    }
    if (arguments != null) {
      args['arguments'] = arguments;
    }
    if (type != null) {
      args['type'] = type;
    }
    if (name == null) {
      args['name'] = 'Share';
    } else {
      args['name'] = name;
    }

    await _channel.invokeMethod<void>('startShareActivity', args);
  }
}
