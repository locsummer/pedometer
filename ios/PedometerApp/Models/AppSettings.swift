import SwiftUI

public enum AppThemeColor: String, Codable, CaseIterable, Identifiable {
    case green = "Classic Green"
    case orange = "Sunset Orange"
    case blue = "Electric Blue"
    case purple = "Neon Purple"
    case red = "Fire Red"
    
    public var id: String { rawValue }
    
    public var color: Color {
        switch self {
        case .green: return Color(red: 0.15, green: 0.85, blue: 0.40)
        case .orange: return Color(red: 1.0, green: 0.55, blue: 0.0)
        case .blue: return Color(red: 0.0, green: 0.65, blue: 1.0)
        case .purple: return Color(red: 0.75, green: 0.35, blue: 1.0)
        case .red: return Color(red: 1.0, green: 0.25, blue: 0.35)
        }
    }
}

public enum ColorSchemePreference: String, Codable, CaseIterable, Identifiable {
    case system = "System Default"
    case dark = "Dark Mode"
    case light = "Light Mode"
    
    public var id: String { rawValue }
}

public struct AppSettings: Codable {
    public var stepGoal: Int
    public var floorGoal: Int
    public var distanceGoalKm: Double
    public var useMetricUnits: Bool
    public var themeColor: AppThemeColor
    public var colorScheme: ColorSchemePreference
    public var enableNotifications: Bool
    public var enableHaptics: Bool
    public var strideLengthCm: Double
    
    public static let `default` = AppSettings(
        stepGoal: 10000,
        floorGoal: 10,
        distanceGoalKm: 8.0,
        useMetricUnits: true,
        themeColor: .green,
        colorScheme: .dark,
        enableNotifications: true,
        enableHaptics: true,
        strideLengthCm: 75.0
    )
}
