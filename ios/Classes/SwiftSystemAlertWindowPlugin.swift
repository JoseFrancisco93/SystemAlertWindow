import Flutter
import UIKit

public class SwiftSystemAlertWindowPlugin: NSObject, FlutterPlugin {
    var pipViewController: PipViewController?
  
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "system_alert_window", binaryMessenger: registrar.messenger())
        let instance = SwiftSystemAlertWindowPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }

    // Call Method
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if (call.method == "getPlatformVersion") {
            result("iOS " + UIDevice.current.systemVersion)
        } else if (call.method == "showPIP") {
            showPIP(call, result: result)
        } else if (call.method == "closePIP") {
            closePIP()
            result(nil)
        }
    }

    // Show PIP (send arguments from flutter)
    private func showPIP(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        guard let arguments = call.arguments as? [String: Any],
              let imageFilePath = arguments["imageFilePath"] as? String,
              let image = UIImage(contentsOfFile: imageFilePath) else {
            result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments for showPIP", details: nil))
            return
        }
      
        pipViewController = PipViewController(image: image)
      
        if let pipViewController = pipViewController {
            DispatchQueue.main.async {
                if let topViewController = UIApplication.shared.windows.first?.rootViewController?.topmostViewController() {
                    topViewController.present(pipViewController, animated: true, completion: nil)
                    result(nil)
                } else {
                    result(FlutterError(code: "UNABLE_TO_PRESENT_PIP", message: "Unable to present PIP", details: nil))
                }
            }
        } else {
            result(FlutterError(code: "UNABLE_TO_CREATE_PIP", message: "Unable to create PIP view controller", details: nil))
        }
    }

    // Close PIP
    private func closePIP() {
        pipViewController?.dismiss(animated: true, completion: nil)
        pipViewController = nil
    }
}

extension UIViewController {
    func topmostViewController() -> UIViewController {
        if let presentedViewController = presentedViewController {
            return presentedViewController.topmostViewController()
        }
        if let navigationController = self as? UINavigationController {
            return navigationController.visibleViewController?.topmostViewController() ?? navigationController
        }
        if let tabBarController = self as? UITabBarController {
            return tabBarController.selectedViewController?.topmostViewController() ?? tabBarController
        }
        return self
    }
}

class PipViewController: UIViewController {
    let imageView: UIImageView
    
    init(image: UIImage) {
        imageView = UIImageView(image: image)
        imageView.contentMode = .scaleAspectFit
        
        super.init(nibName: nil, bundle: nil)
        
        modalPresentationStyle = .overFullScreen
        modalTransitionStyle = .crossDissolve
    }
  
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
  
    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .clear
        view.addSubview(imageView)
    }
  
    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        
        imageView.frame = view.bounds
    }
}