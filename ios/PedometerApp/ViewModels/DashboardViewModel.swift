import Foundation
import SwiftUI
import Combine

public final class DashboardViewModel: ObservableObject {
    @Published public var selectedDate: Date = Date()
    @Published public var currentRecord: StepRecord = StepRecord(date: Date())
    @Published public var recentDaysRecords: [StepRecord] = []
    @Published public var currentStreak: Int = 0
    @Published public var bestStreak: Int = 0
    @Published public var isLiveActive: Bool = false
    @Published public var settings: AppSettings = AppSettings.default
    
    private let pedometerService = PedometerService.shared
    private let storageService = StorageService.shared
    private var cancellables = Set<AnyCancellable>()
    
    public init() {
        loadData()
        startLiveTracking()
    }
    
    public func loadData() {
        self.settings = storageService.loadSettings()
        self.recentDaysRecords = storageService.loadDailyRecords()
        
        let calendar = Calendar.current
        if let todayRecord = recentDaysRecords.first(where: { calendar.isDate($0.date, inSameDayAs: Date()) }) {
            self.currentRecord = todayRecord
        } else {
            let newToday = StepRecord(date: Date())
            self.currentRecord = newToday
            self.recentDaysRecords.append(newToday)
        }
        
        calculateStreaks()
    }
    
    public func selectDate(_ date: Date) {
        self.selectedDate = date
        let calendar = Calendar.current
        if let record = recentDaysRecords.first(where: { calendar.isDate($0.date, inSameDayAs: date) }) {
            self.currentRecord = record
        } else {
            self.currentRecord = StepRecord(date: date)
        }
    }
    
    public func startLiveTracking() {
        let calendar = Calendar.current
        let startOfDay = calendar.startOfDay(for: Date())
        
        pedometerService.startLiveUpdates(since: startOfDay) { [weak self] data, error in
            guard let self = self, let data = data else { return }
            
            DispatchQueue.main.async {
                let steps = data.numberOfSteps.intValue
                let distance = data.distance?.doubleValue ?? (Double(steps) * (self.settings.strideLengthCm / 100.0))
                let floorsAsc = data.floorsAscended?.intValue ?? 0
                let floorsDesc = data.floorsDescended?.intValue ?? 0
                let kcal = Double(steps) * 0.042
                let mins = steps / 100
                
                if calendar.isDate(self.selectedDate, inSameDayAs: Date()) {
                    self.currentRecord.stepCount = steps
                    self.currentRecord.distanceMeters = distance
                    self.currentRecord.floorsAscended = floorsAsc
                    self.currentRecord.floorsDescended = floorsDesc
                    self.currentRecord.activeCalories = kcal
                    self.currentRecord.activeMinutes = mins
                }
                
                // Update in records array
                if let index = self.recentDaysRecords.firstIndex(where: { calendar.isDate($0.date, inSameDayAs: Date()) }) {
                    self.recentDaysRecords[index].stepCount = steps
                    self.recentDaysRecords[index].distanceMeters = distance
                    self.recentDaysRecords[index].floorsAscended = floorsAsc
                    self.recentDaysRecords[index].floorsDescended = floorsDesc
                    self.recentDaysRecords[index].activeCalories = kcal
                    self.recentDaysRecords[index].activeMinutes = mins
                    self.storageService.saveDailyRecords(self.recentDaysRecords)
                }
                
                self.calculateStreaks()
                self.isLiveActive = true
            }
        }
    }
    
    public func refreshHistoricalData() async {
        let calendar = Calendar.current
        let hourly = await pedometerService.fetchHourlyBreakdown(for: selectedDate)
        
        await MainActor.run {
            self.currentRecord.hourlySteps = hourly
            if let index = self.recentDaysRecords.firstIndex(where: { calendar.isDate($0.date, inSameDayAs: self.selectedDate) }) {
                self.recentDaysRecords[index].hourlySteps = hourly
                self.storageService.saveDailyRecords(self.recentDaysRecords)
            }
        }
    }
    
    private func calculateStreaks() {
        let goal = settings.stepGoal
        var current = 0
        var best = 0
        var tempStreak = 0
        
        // Sort chronologically
        let sorted = recentDaysRecords.sorted(by: { $0.date < $1.date })
        
        for record in sorted {
            if record.stepCount >= goal {
                tempStreak += 1
                best = max(best, tempStreak)
            } else {
                tempStreak = 0
            }
        }
        
        // Calculate backwards from today
        let calendar = Calendar.current
        var checkDate = Date()
        current = 0
        
        while true {
            if let record = recentDaysRecords.first(where: { calendar.isDate($0.date, inSameDayAs: checkDate) }) {
                if record.stepCount >= goal {
                    current += 1
                    guard let prevDay = calendar.date(byAdding: .day, value: -1, to: checkDate) else { break }
                    checkDate = prevDay
                } else if calendar.isDateInToday(checkDate) {
                    // Today might still be in progress
                    guard let prevDay = calendar.date(byAdding: .day, value: -1, to: checkDate) else { break }
                    checkDate = prevDay
                } else {
                    break
                }
            } else {
                break
            }
        }
        
        self.currentStreak = current
        self.bestStreak = max(best, current)
    }
    
    public var goalProgress: Double {
        guard settings.stepGoal > 0 else { return 0 }
        return min(1.0, Double(currentRecord.stepCount) / Double(settings.stepGoal))
    }
    
    public var last7Days: [StepRecord] {
        let calendar = Calendar.current
        let today = Date()
        var list: [StepRecord] = []
        
        for i in (0..<7).reversed() {
            guard let date = calendar.date(byAdding: .day, value: -i, to: today) else { continue }
            if let found = recentDaysRecords.first(where: { calendar.isDate($0.date, inSameDayAs: date) }) {
                list.append(found)
            } else {
                list.append(StepRecord(date: date))
            }
        }
        return list
    }
}
