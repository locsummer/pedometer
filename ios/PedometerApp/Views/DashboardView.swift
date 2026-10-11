import SwiftUI

public struct DashboardView: View {
    @ObservedObject var viewModel: DashboardViewModel
    @State private var showingDatePicker = false
    
    private let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        return f
    }()
    
    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Top Date Header & Streak Badge
                    HStack {
                        VStack(alignment: .leading, spacing: 4) {
                            Text(isToday(viewModel.selectedDate) ? "Today" : dateFormatter.string(from: viewModel.selectedDate))
                                .font(.title2)
                                .fontWeight(.bold)
                            
                            if !isToday(viewModel.selectedDate) {
                                Button("Back to Today") {
                                    withAnimation {
                                        viewModel.selectDate(Date())
                                    }
                                }
                                .font(.caption)
                                .foregroundColor(viewModel.settings.themeColor.color)
                            }
                        }
                        
                        Spacer()
                        
                        // Streak Pill
                        HStack(spacing: 6) {
                            Image(systemName: "flame.fill")
                                .foregroundColor(.orange)
                            Text("\(viewModel.currentStreak) Days")
                                .font(.subheadline)
                                .fontWeight(.bold)
                        }
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .background(Color.orange.opacity(0.15))
                        .clipShape(Capsule())
                    }
                    .padding(.horizontal)
                    
                    // Main Circular Step Progress Ring
                    StepRingView(
                        currentSteps: viewModel.currentRecord.stepCount,
                        goalSteps: viewModel.settings.stepGoal,
                        themeColor: viewModel.settings.themeColor.color
                    )
                    
                    // 4 Metric Grid Cards
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                        MetricCard(
                            icon: "location.north.line.fill",
                            title: "Distance",
                            value: viewModel.currentRecord.formattedDistance(useMetric: viewModel.settings.useMetricUnits),
                            tintColor: .blue
                        )
                        
                        MetricCard(
                            icon: "stairs",
                            title: "Floors",
                            value: "\(viewModel.currentRecord.floorsAscended) Flights",
                            tintColor: .purple
                        )
                        
                        MetricCard(
                            icon: "flame.fill",
                            title: "Active Calories",
                            value: "\(Int(viewModel.currentRecord.activeCalories)) kcal",
                            tintColor: .orange
                        )
                        
                        MetricCard(
                            icon: "clock.fill",
                            title: "Active Time",
                            value: "\(viewModel.currentRecord.activeMinutes) mins",
                            tintColor: .green
                        )
                    }
                    .padding(.horizontal)
                    
                    // 7-Day Interactive Historical Bar Chart
                    DailyBarChart(
                        records: viewModel.last7Days,
                        goalSteps: viewModel.settings.stepGoal,
                        selectedDate: viewModel.selectedDate,
                        themeColor: viewModel.settings.themeColor.color,
                        onSelectDate: { date in
                            withAnimation {
                                viewModel.selectDate(date)
                            }
                        }
                    )
                    .padding(.horizontal)
                    
                    // Hourly Step Activity Breakdown (Canvas / Bar)
                    VStack(alignment: .leading, spacing: 12) {
                        Text("HOURLY ACTIVITY")
                            .font(.caption)
                            .fontWeight(.bold)
                            .foregroundColor(.secondary)
                            .tracking(0.5)
                        
                        HStack(alignment: .bottom, spacing: 3) {
                            let maxHour = max(1, viewModel.currentRecord.hourlySteps.max() ?? 1)
                            ForEach(0..<24, id: \.self) { hour in
                                let count = viewModel.currentRecord.hourlySteps[hour]
                                VStack {
                                    Spacer()
                                    RoundedRectangle(cornerRadius: 2)
                                        .fill(count > 0 ? viewModel.settings.themeColor.color : Color.gray.opacity(0.2))
                                        .frame(height: max(3, CGFloat(count) / CGFloat(maxHour) * 60))
                                }
                            }
                        }
                        .frame(height: 70)
                        
                        HStack {
                            Text("12 AM").font(.caption2).foregroundColor(.secondary)
                            Spacer()
                            Text("12 PM").font(.caption2).foregroundColor(.secondary)
                            Spacer()
                            Text("11 PM").font(.caption2).foregroundColor(.secondary)
                        }
                    }
                    .padding(16)
                    .background(Color(.secondarySystemBackground))
                    .cornerRadius(18)
                    .padding(.horizontal)
                }
                .padding(.bottom, 30)
            }
            .navigationTitle("Pedometer++")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        Task {
                            await viewModel.refreshHistoricalData()
                        }
                    } label: {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
        }
    }
    
    private func isToday(_ date: Date) -> Bool {
        Calendar.current.isDateInToday(date)
    }
}
