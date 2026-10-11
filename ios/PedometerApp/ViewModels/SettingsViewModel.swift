import Foundation
import SwiftUI

public final class SettingsViewModel: ObservableObject {
    @Published public var settings: AppSettings {
        didSet {
            storageService.saveSettings(settings)
        }
    }
    
    private let storageService = StorageService.shared
    
    public init() {
        self.settings = storageService.loadSettings()
    }
    
    public func resetDefaults() {
        self.settings = AppSettings.default
    }
}
