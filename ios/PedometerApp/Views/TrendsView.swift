import SwiftUI

public struct TrendsView: View {
    @ObservedObject var dashboardVM: DashboardViewModel
    @State private var selectedPeriod: Int = 0 // 0: 7 Days, 1: 30 Days, 2: All Time
    
    private var allRecords: [StepRecord] {
        dashboardVM.recentDaysRecords
    }
    
    private var averageSteps: Int {
        guard !allRecords.isEmpty else { return 0 }
        let sum = allRecords.reduce(0) { $0 + $1.stepCount }
        return sum / allRecords.count
    }
    
    private var totalDistanceKm: Double {
        allRecords.reduce(0.0) { $0 + $1.distanceKilometers }
    }
    
    private var totalFloors: Int {
        allRecords.reduce(0) { $0 + $1.floorsAscended }
    }
    
    private var bestDayRecord: StepRecord? {
        allRecords.max(by: { $0.stepCount < $1.stepCount })
    }
    
    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Summary Lifetime Cards
                    VStack(alignment: .leading, spacing: 14) {
                        Text("LIFETIME TOTALS")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.secondary)
                            .tracking(0.5)
                        
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                            MetricCard(
                                icon: "figure.walk",
                                title: "Daily Average",
                                value: "\(averageSteps.formatted())",
                                subtitle: "steps / day",
                                tintColor: .green
                            )
                            
                            MetricCard(
                                icon: "map.fill",
                                title: "Total Distance",
                                value: String(format: "%.1f km", totalDistanceKm),
                                subtitle: "walked & run",
                                tintColor: .blue
                            )
                            
                            MetricCard(
                                icon: "building.2.fill",
                                title: "Total Floors",
                                value: "\(totalFloors)",
                                subtitle: "flights climbed",
                                tintColor: .purple
                            )
                            
                            MetricCard(
                                icon: "trophy.fill",
                                title: "Best Day",
                                value: "\(bestDayRecord?.stepCount.formatted() ?? "0")",
                                subtitle: bestDayRecord?.date.formatted(date: .abbreviated, time: .omitted),
                                tintColor: .orange
                            )
                        }
                    }
                    .padding(.horizontal)
                    
                    // Monthly Trend History Bars
                    VStack(alignment: .leading, spacing: 14) {
                        Text("30-DAY STEP HISTORY")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.secondary)
                            .tracking(0.5)
                        
                        HStack(alignment: .bottom, spacing: 4) {
                            let maxSteps = max(dashboardVM.settings.stepGoal, allRecords.map { $0.stepCount }.max() ?? 10000)
                            
                            ForEach(allRecords.suffix(30)) { record in
                                let isMet = record.stepCount >= dashboardVM.settings.stepGoal
                                GeometryReader { geo in
                                    VStack {
                                        Spacer()
                                        RoundedRectangle(cornerRadius: 3)
                                            .fill(isMet ? Color.green : Color.orange.opacity(0.8))
                                            .frame(height: max(4, geo.size.height * (CGFloat(record.stepCount) / CGFloat(maxSteps))))
                                    }
                                }
                            }
                        }
                        .frame(height: 140)
                        
                        HStack {
                            Text("30 days ago").font(.caption2).foregroundColor(.secondary)
                            Spacer()
                            Text("Today").font(.caption2).foregroundColor(.secondary)
                        }
                    }
                    .padding(16)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(18)
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .navigationTitle("Trends & Stats")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
