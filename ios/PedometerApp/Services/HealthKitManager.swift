import Foundation
import HealthKit

public final class HealthKitManager: ObservableObject {
    public static let shared = HealthKitManager()
    
    public let healthStore = HKHealthStore()
    @Published public var isAuthorized: Bool = false
    
    private init() {
        checkAuthorizationStatus()
    }
    
    public func isHealthDataAvailable() -> Bool {
        return HKHealthStore.isHealthDataAvailable()
    }
    
    public func requestAuthorization() async throws {
        guard HKHealthStore.isHealthDataAvailable() else { return }
        
        let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
        let distanceType = HKQuantityType.quantityType(forIdentifier: .distanceWalkingRunning)!
        let flightsType = HKQuantityType.quantityType(forIdentifier: .flightsClimbed)!
        let caloriesType = HKQuantityType.quantityType(forIdentifier: .activeEnergyBurned)!
        
        let readTypes: Set<HKObjectType> = [stepType, distanceType, flightsType, caloriesType]
        
        try await healthStore.requestAuthorization(toShare: [], read: readTypes)
        
        DispatchQueue.main.async {
            self.checkAuthorizationStatus()
        }
    }
    
    public func checkAuthorizationStatus() {
        guard HKHealthStore.isHealthDataAvailable() else {
            self.isAuthorized = false
            return
        }
        
        if let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount) {
            let status = healthStore.authorizationStatus(for: stepType)
            self.isAuthorized = (status == .sharingAuthorized)
        }
    }
    
    public func fetchDailySteps(for date: Date) async throws -> Double {
        guard HKHealthStore.isHealthDataAvailable() else { return 0 }
        
        let stepType = HKQuantityType.quantityType(forIdentifier: .stepCount)!
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: date)
        guard let endOfDay = calendar.date(byAdding: .day, value: 1, to: startOfDay) else { return 0 }
        
        let predicate = HKQuery.predicateForSamples(withStart: startOfDay, end: endOfDay, options: .strictStartDate)
        
        return try await withCheckedThrowingContinuation { continuation in
            let query = HKStatisticsQuery(quantityType: stepType, quantitySamplePredicate: predicate, options: .cumulativeSum) { _, statistics, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let sum = statistics?.sumQuantity() {
                    let steps = sum.doubleValue(for: HKUnit.count())
                    continuation.resume(returning: steps)
                } else {
                    continuation.resume(returning: 0)
                }
            }
            healthStore.execute(query)
        }
    }
}
