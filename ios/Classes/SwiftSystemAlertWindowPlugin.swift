import Flutter
import UIKit

public class SwiftSystemAlertWindowPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "system_alert_window", binaryMessenger: registrar.messenger())
    let instance = SwiftSystemAlertWindowPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    if (call.method == "getPlatformVersion") {
               result("iOS " + UIDevice.current.systemVersion)
           }
  }
}
