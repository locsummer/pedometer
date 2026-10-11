import Foundation

public final class StorageService {
    public static let shared = StorageService()
    
    private let recordsKey = "pedometer_daily_records_v1"
    private let workoutsKey = "pedometer_workouts_v1"
    private let badgesKey = "pedometer_badges_v1"
    private let settingsKey = "pedometer_app_settings_v1"
    
    private init() {}
    
    // MARK: - Step Records
    public func saveDailyRecords(_ records: [StepRecord]) {
        if let data = try? JSONEncoder().encode(records) {
            UserDefaults.standard.set(data, forKey: recordsKey)
        }
    }
    
    public func loadDailyRecords() -> [StepRecord] {
        guard let data = UserDefaults.standard.data(forKey: recordsKey),
              let records = try? JSONDecoder().decode([StepRecord].self, from: data) else {
            return generateMockHistoricalRecords()
        }
        return records
    }
    
    public func saveOrUpdateRecord(_ record: StepRecord) {
        var records = loadDailyRecords()
        let calendar = Calendar.current
        if let index = records.firstIndex(where: { calendar.isDate($0.date, inSameDayAs: record.date) }) {
            records[index] = record
        } else {
            records.append(record)
        }
        saveDailyRecords(records)
    }
    
    // MARK: - Workouts
    public func saveWorkouts(_ workouts: [Workout]) {
        if let data = try? JSONEncoder().encode(workouts) {
            UserDefaults.standard.set(data, forKey: workoutsKey)
        }
    }
    
    public func loadWorkouts() -> [Workout] {
        guard let data = UserDefaults.standard.data(forKey: workoutsKey),
              let workouts = try? JSONDecoder().decode([Workout].self, from: data) else {
            return []
        }
        return workouts
    }
    
    public func addWorkout(_ workout: Workout) {
        var workouts = loadWorkouts()
        workouts.insert(workout, at: 0)
        saveWorkouts(workouts)
    }
    
    // MARK: - Badges
    public func saveBadges(_ badges: [Badge]) {
        if let data = try? JSONEncoder().encode(badges) {
            UserDefaults.standard.set(data, forKey: badgesKey)
        }
    }
    
    public func loadBadges() -> [Badge] {
        guard let data = UserDefaults.standard.data(forKey: badgesKey),
              let badges = try? JSONDecoder().decode([Badge].self, from: data) else {
            return defaultBadges
        }
        return badges
    }
    
    // MARK: - Settings
    public func saveSettings(_ settings: AppSettings) {
        if let data = try? JSONEncoder().encode(settings) {
            UserDefaults.standard.set(data, forKey: settingsKey)
        }
    }
    
    public func loadSettings() -> AppSettings {
        guard let data = UserDefaults.standard.data(forKey: settingsKey),
              let settings = try? JSONDecoder().decode(AppSettings.self, from: data) else {
            return AppSettings.default
        }
        return settings
    }
    
    // MARK: - Initial Mock Data (for demo & simulator)
    public var defaultBadges: [Badge] {
        return [
            Badge(id: "streak_3", title: "3-Day Streak", description: "Hit your daily step goal 3 days in a row", iconName: "flame.fill", category: .streak, requirement: 3),
            Badge(id: "streak_7", title: "7-Day Warrior", description: "Hit your daily step goal 7 days in a row", iconName: "flame.circle.fill", category: .streak, requirement: 7),
            Badge(id: "streak_30", title: "Monthly Master", description: "Hit your daily step goal 30 days in a row", iconName: "crown.fill", category: .streak, requirement: 30),
            
            Badge(id: "step_10k", title: "10K Club", description: "Walk 10,000 steps in a single day", iconName: "figure.walk.diamond.fill", category: .dailySteps, requirement: 10000),
            Badge(id: "step_15k", title: "15K Explorer", description: "Walk 15,000 steps in a single day", iconName: "figure.walk.motion", category: .dailySteps, requirement: 15000),
            Badge(id: "step_20k", title: "20K Ultra", description: "Walk 20,000 steps in a single day", iconName: "star.circle.fill", category: .dailySteps, requirement: 20000),
            
            Badge(id: "dist_marathon", title: "Marathon", description: "Cover a total distance of 42.195 km", iconName: "medal.fill", category: .lifetimeDistance, requirement: 42.2),
            Badge(id: "dist_100k", title: "Centurion (100 km)", description: "Cover 100 kilometers on foot", iconName: "road.lanes", category: .lifetimeDistance, requirement: 100.0),
            
            Badge(id: "climb_tower", title: "Tower Climber", description: "Climb 50 floors in total", iconName: "building.2.fill", category: .elevation, requirement: 50),
            Badge(id: "climb_everest", title: "Mount Everest", description: "Climb 1,000 floors of elevation", iconName: "mountain.2.fill", category: .elevation, requirement: 1000)
        ]
    }
    
    private func generateMockHistoricalRecords() -> [StepRecord] {
        var records: [StepRecord] = []
        let calendar = Calendar.current
        let today = Date()
        
        let sampleSteps = [11450, 8920, 10320, 12890, 9500, 14200, 10870, 7650, 11200, 13400, 10100, 9800, 12500, 11050]
        
        for (i, steps) in sampleSteps.enumerated() {
            guard let date = calendar.date(byAdding: .day, value: -i, to: today) else { continue }
            let distMeters = Double(steps) * 0.75
            let floors = Int.random(in: 4...18)
            let kcal = Double(steps) * 0.042
            let mins = steps / 105
            
            var hourly = Array(repeating: 0, count: 24)
            for h in 7...21 {
                hourly[h] = steps / 15 + Int.random(in: -80...80)
            }
            
            records.append(StepRecord(
                date: date,
                stepCount: steps,
                distanceMeters: distMeters,
                floorsAscended: floors,
                floorsDescended: max(1, floors - 2),
                activeCalories: kcal,
                activeMinutes: mins,
                hourlySteps: hourly
            ))
        }
        
        return records.reversed()
    }
}
