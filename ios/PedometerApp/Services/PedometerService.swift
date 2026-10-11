import Foundation
import CoreMotion
import Combine

public final class PedometerService: ObservableObject {
    public static let shared = PedometerService()
    
    private let pedometer = CMPedometer()
    private let activityManager = CMMotionActivityManager()
    
    @Published public var isStepCountingAvailable: Bool = false
    @Published public var isFloorCountingAvailable: Bool = false
    @Published public var isDistanceAvailable: Bool = false
    @Published public var isLiveTracking: Bool = false
    
    @Published public var currentCadence: Double = 0 // steps/sec
    @Published public var currentActivityType: String = "Stationary"
    
    private var cancellables = Set<AnyCancellable>()
    
    private init() {
        checkAvailability()
    }
    
    public func checkAvailability() {
        isStepCountingAvailable = CMPedometer.isStepCountingAvailable()
        isFloorCountingAvailable = CMPedometer.isFloorCountingAvailable()
        isDistanceAvailable = CMPedometer.isDistanceAvailable()
    }
    
    public func startLiveUpdates(since startOfDay: Date, onUpdate: @escaping (CMPedometerData?, Error?) -> Void) {
        guard CMPedometer.isStepCountingAvailable() else {
            // Simulator fallback
            return
        }
        
        isLiveTracking = true
        pedometer.startUpdates(from: startOfDay) { [weak self] data, error in
            DispatchQueue.main.async {
                if let cadence = data?.currentCadence?.doubleValue {
                    self?.currentCadence = cadence
                }
                onUpdate(data, error)
            }
        }
        
        if CMMotionActivityManager.isActivityAvailable() {
            activityManager.startActivityUpdates(to: .main) { [weak self] activity in
                guard let activity = activity else { return }
                if activity.running {
                    self?.currentActivityType = "Running"
                } else if activity.walking {
                    self?.currentActivityType = "Walking"
                } else if activity.automotive {
                    self?.currentActivityType = "Automotive"
                } else if activity.cycling {
                    self?.currentActivityType = "Cycling"
                } else if activity.stationary {
                    self?.currentActivityType = "Stationary"
                } else {
                    self?.currentActivityType = "Unknown"
                }
            }
        }
    }
    
    public func stopLiveUpdates() {
        pedometer.stopUpdates()
        if CMMotionActivityManager.isActivityAvailable() {
            activityManager.stopActivityUpdates()
        }
        isLiveTracking = false
    }
    
    public func fetchHistoricalData(from startDate: Date, to endDate: Date) async throws -> CMPedometerData {
        guard CMPedometer.isStepCountingAvailable() else {
            throw NSError(domain: "PedometerService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Step counting not available on this device"])
        }
        
        return try await withCheckedThrowingContinuation { continuation in
            pedometer.queryPedometerData(from: startDate, to: endDate) { data, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let data = data {
                    continuation.resume(returning: data)
                } else {
                    continuation.resume(throwing: NSError(domain: "PedometerService", code: -2, userInfo: [NSLocalizedDescriptionKey: "No data returned"]))
                }
            }
        }
    }
    
    public func fetchHourlyBreakdown(for date: Date) async -> [Int] {
        var hourlySteps = Array(repeating: 0, count: 24)
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: date)
        
        guard CMPedometer.isStepCountingAvailable() else {
            // Generate realistic mock hourly curve for previews / simulator
            for hour in 7...22 {
                let factor = sin(Double(hour - 7) / 15.0 * .pi)
                hourlySteps[hour] = Int(Double.random(in: 200...900) * factor)
            }
            return hourlySteps
        }
        
        for hour in 0..<24 {
            guard let hourStart = calendar.date(byAdding: .hour, value: hour, to: startOfDay),
                  let hourEnd = calendar.date(byAdding: .hour, value: hour + 1, to: startOfDay) else {
                continue
            }
            
            if hourStart > Date() { break }
            let queryEnd = min(hourEnd, Date())
            
            do {
                let data = try await fetchHistoricalData(from: hourStart, to: queryEnd)
                hourlySteps[hour] = data.numberOfSteps.intValue
            } catch {
                hourlySteps[hour] = 0
            }
        }
        
        return hourlySteps
    }
}
