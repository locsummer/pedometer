import Foundation
import CoreLocation

public enum WorkoutType: String, Codable, CaseIterable, Identifiable {
    case walk = "Walk"
    case run = "Run"
    case hike = "Hike"
    
    public var id: String { rawValue }
    
    public var iconName: String {
        switch self {
        case .walk: return "figure.walk"
        case .run: return "figure.run"
        case .hike: return "figure.hiking"
        }
    }
}

public struct LocationPoint: Codable, Equatable {
    public var latitude: Double
    public var longitude: Double
    public var altitude: Double
    public var timestamp: Date
    public var speed: Double
    
    public init(coordinate: CLLocationCoordinate2D, altitude: Double, timestamp: Date, speed: Double) {
        self.latitude = coordinate.latitude
        self.longitude = coordinate.longitude
        self.altitude = altitude
        self.timestamp = timestamp
        self.speed = speed
    }
    
    public var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}

public struct Workout: Identifiable, Codable {
    public var id: UUID
    public var type: WorkoutType
    public var startDate: Date
    public var endDate: Date?
    public var durationSeconds: TimeInterval
    public var distanceMeters: Double
    public var stepCount: Int
    public var caloriesBurned: Double
    public var elevationGainMeters: Double
    public var routePoints: [LocationPoint]
    
    public init(
        id: UUID = UUID(),
        type: WorkoutType = .walk,
        startDate: Date = Date(),
        endDate: Date? = nil,
        durationSeconds: TimeInterval = 0,
        distanceMeters: Double = 0,
        stepCount: Int = 0,
        caloriesBurned: Double = 0,
        elevationGainMeters: Double = 0,
        routePoints: [LocationPoint] = []
    ) {
        self.id = id
        self.type = type
        self.startDate = startDate
        self.endDate = endDate
        self.durationSeconds = durationSeconds
        self.distanceMeters = distanceMeters
        self.stepCount = stepCount
        self.caloriesBurned = caloriesBurned
        self.elevationGainMeters = elevationGainMeters
        self.routePoints = routePoints
    }
    
    public var averagePaceSecondsPerKm: Double {
        guard distanceMeters > 50 else { return 0 }
        let km = distanceMeters / 1000.0
        return durationSeconds / km
    }
    
    public var formattedPace: String {
        guard averagePaceSecondsPerKm > 0 else { return "--'--\"" }
        let mins = Int(averagePaceSecondsPerKm) / 60
        let secs = Int(averagePaceSecondsPerKm) % 60
        return String(format: "%d'%02d\"/km", mins, secs)
    }
    
    public var formattedDuration: String {
        let hours = Int(durationSeconds) / 3600
        let mins = (Int(durationSeconds) % 3600) / 60
        let secs = Int(durationSeconds) % 60
        if hours > 0 {
            return String(format: "%02d:%02d:%02d", hours, mins, secs)
        } else {
            return String(format: "%02d:%02d", mins, secs)
        }
    }
}
