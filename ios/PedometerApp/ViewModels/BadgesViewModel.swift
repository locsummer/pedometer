import Foundation
import SwiftUI

public final class BadgesViewModel: ObservableObject {
    @Published public var badges: [Badge] = []
    
    private let storageService = StorageService.shared
    
    public init() {
        refreshBadges()
    }
    
    public func refreshBadges() {
        var currentBadges = storageService.loadBadges()
        let records = storageService.loadDailyRecords()
        let settings = storageService.loadSettings()
        
        let totalSteps = records.reduce(0) { $0 + $1.stepCount }
        let totalDistanceKm = records.reduce(0.0) { $0 + $1.distanceKilometers }
        let totalFloors = records.reduce(0) { $0 + $1.floorsAscended }
        let maxSingleDaySteps = records.map { $0.stepCount }.max() ?? 0
        
        // Calculate max streak
        var streak = 0
        var maxStreak = 0
        for r in records.sorted(by: { $0.date < $1.date }) {
            if r.stepCount >= settings.stepGoal {
                streak += 1
                maxStreak = max(maxStreak, streak)
            } else {
                streak = 0
            }
        }
        
        for i in 0..<currentBadges.count {
            switch currentBadges[i].id {
            case "streak_3", "streak_7", "streak_30":
                currentBadges[i].currentProgress = Double(maxStreak)
                if Double(maxStreak) >= currentBadges[i].requirement && !currentBadges[i].isUnlocked {
                    currentBadges[i].isUnlocked = true
                    currentBadges[i].unlockedDate = Date()
                }
            case "step_10k", "step_15k", "step_20k":
                currentBadges[i].currentProgress = Double(maxSingleDaySteps)
                if Double(maxSingleDaySteps) >= currentBadges[i].requirement && !currentBadges[i].isUnlocked {
                    currentBadges[i].isUnlocked = true
                    currentBadges[i].unlockedDate = Date()
                }
            case "dist_marathon", "dist_100k":
                currentBadges[i].currentProgress = totalDistanceKm
                if totalDistanceKm >= currentBadges[i].requirement && !currentBadges[i].isUnlocked {
                    currentBadges[i].isUnlocked = true
                    currentBadges[i].unlockedDate = Date()
                }
            case "climb_tower", "climb_everest":
                currentBadges[i].currentProgress = Double(totalFloors)
                if Double(totalFloors) >= currentBadges[i].requirement && !currentBadges[i].isUnlocked {
                    currentBadges[i].isUnlocked = true
                    currentBadges[i].unlockedDate = Date()
                }
            default:
                break
            }
        }
        
        self.badges = currentBadges
        storageService.saveBadges(currentBadges)
    }
}
