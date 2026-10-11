import Foundation

public enum BadgeCategory: String, Codable, CaseIterable {
    case streak = "Streaks"
    case dailySteps = "Daily Records"
    case lifetimeDistance = "Lifetime Distance"
    case elevation = "Climbing"
}

public struct Badge: Identifiable, Codable, Equatable {
    public var id: String
    public var title: String
    public var description: String
    public var iconName: String
    public var category: BadgeCategory
    public var requirement: Double
    public var currentProgress: Double
    public var isUnlocked: Bool
    public var unlockedDate: Date?
    
    public init(
        id: String,
        title: String,
        description: String,
        iconName: String,
        category: BadgeCategory,
        requirement: Double,
        currentProgress: Double = 0,
        isUnlocked: Bool = false,
        unlockedDate: Date? = nil
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.iconName = iconName
        self.category = category
        self.requirement = requirement
        self.currentProgress = currentProgress
        self.isUnlocked = isUnlocked
        self.unlockedDate = unlockedDate
    }
    
    public var progressRatio: Double {
        guard requirement > 0 else { return 0 }
        return min(1.0, currentProgress / requirement)
    }
}
