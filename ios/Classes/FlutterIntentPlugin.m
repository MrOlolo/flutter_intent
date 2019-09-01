#import "FlutterIntentPlugin.h"
#import <flutter_intent/flutter_intent-Swift.h>

@implementation FlutterIntentPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterIntentPlugin registerWithRegistrar:registrar];
}
@end
