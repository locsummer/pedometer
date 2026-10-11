import Foundation
import SwiftUI
import Combine
import CoreLocation

public final class WorkoutViewModel: ObservableObject {
    @Published public var isWorkoutActive: Bool = false
    @Published public var isPaused: Bool = false
    @Published public var selectedType: WorkoutType = .walk
    @Published public var currentWorkout: Workout = Workout()
    @Published public var pastWorkouts: [Workout] = []
    
    private var timer: Timer?
    private let locationTracker = LocationTracker.shared
    private let storageService = StorageService.shared
    private var cancellables = Set<AnyCancellable>()
    
    public init() {
        self.pastWorkouts = storageService.loadWorkouts()
        bindLocationTracker()
    }
    
    private func bindLocationTracker() {
        locationTracker.$routeLocations
            .sink { [weak self] points in
                guard let self = self, self.isWorkoutActive, !self.isPaused else { return }
                self.currentWorkout.routePoints = points
                self.currentWorkout.distanceMeters = self.locationTracker.totalDistanceMeters
                self.currentWorkout.elevationGainMeters = self.locationTracker.elevationGainMeters
                
                // Estimate steps & calories
                let stepsPerMeter = (self.selectedType == .run) ? 0.9 : 1.3
                self.currentWorkout.stepCount = Int(self.currentWorkout.distanceMeters * stepsPerMeter)
                
                let calMultiplier = (self.selectedType == .run) ? 0.065 : 0.045
                self.currentWorkout.caloriesBurned = Double(self.currentWorkout.stepCount) * calMultiplier
            }
            .store(in: &cancellables)
    }
    
    public func startWorkout() {
        locationTracker.startWorkout()
        currentWorkout = Workout(type: selectedType, startDate: Date())
        isWorkoutActive = true
        isPaused = false
        
        timer?.invalidate()
        timer = Timer.scheduledTimer(withTimeInterval: 1.0, repeats: true) { [weak self] _ in
            guard let self = self, self.isWorkoutActive, !self.isPaused else { return }
            self.currentWorkout.durationSeconds += 1
        }
    }
    
    public func pauseWorkout() {
        isPaused = true
    }
    
    public func resumeWorkout() {
        isPaused = false
    }
    
    public func stopWorkout() {
        timer?.invalidate()
        timer = nil
        locationTracker.stopWorkout()
        
        currentWorkout.endDate = Date()
        isWorkoutActive = false
        isPaused = false
        
        if currentWorkout.durationSeconds > 10 || currentWorkout.distanceMeters > 50 {
            storageService.addWorkout(currentWorkout)
            pastWorkouts.insert(currentWorkout, at: 0)
        }
    }
}
