import Foundation

public struct StepRecord: Identifiable, Codable, Equatable {
    public var id: UUID
    public var date: Date
    public var stepCount: Int
    public var distanceMeters: Double
    public var floorsAscended: Int
    public var floorsDescended: Int
    public var activeCalories: Double
    public var activeMinutes: Int
    public var hourlySteps: [Int] // 24 hours
    
    public init(
        id: UUID = UUID(),
        date: Date,
        stepCount: Int = 0,
        distanceMeters: Double = 0,
        floorsAscended: Int = 0,
        floorsDescended: Int = 0,
        activeCalories: Double = 0,
        activeMinutes: Int = 0,
        hourlySteps: [Int] = Array(repeating: 0, count: 24)
    ) {
        self.id = id
        self.date = date
        self.stepCount = stepCount
        self.distanceMeters = distanceMeters
        self.floorsAscended = floorsAscended
        self.floorsDescended = floorsDescended
        self.activeCalories = activeCalories
        self.activeMinutes = activeMinutes
        self.hourlySteps = hourlySteps
    }
    
    public var distanceKilometers: Double {
        return distanceMeters / 1000.0
    }
    
    public var distanceMiles: Double {
        return distanceMeters * 0.000621371
    }
    
    public func formattedDistance(useMetric: Bool) -> String {
        if useMetric {
            return String(format: "%.2f km", distanceKilometers)
        } else {
            return String(format: "%.2f mi", distanceMiles)
        }
    }
}
