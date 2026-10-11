import SwiftUI

public struct DailyBarChart: View {
    public let records: [StepRecord]
    public let goalSteps: Int
    public let selectedDate: Date
    public let themeColor: Color
    public let onSelectDate: (Date) -> Void
    
    public init(
        records: [StepRecord],
        goalSteps: Int,
        selectedDate: Date,
        themeColor: Color = .green,
        onSelectDate: @escaping (Date) -> Void
    ) {
        self.records = records
        self.goalSteps = max(1, goalSteps)
        self.selectedDate = selectedDate
        self.themeColor = themeColor
        self.onSelectDate = onSelectDate
    }
    
    private var maxSteps: Int {
        let recordMax = records.map { $0.stepCount }.max() ?? 0
        return max(goalSteps, recordMax, 10000)
    }
    
    private let dayFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "E"
        return f
    }()
    
    private let calendar = Calendar.current
    
    public var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Text("PAST 7 DAYS")
                    .font(.caption)
                    .fontWeight(.bold)
                    .foregroundColor(.secondary)
                    .tracking(0.5)
                
                Spacer()
                
                HStack(spacing: 12) {
                    HStack(spacing: 4) {
                        Circle().fill(themeColor).frame(width: 8, height: 8)
                        Text("Goal Met").font(.caption2).foregroundColor(.secondary)
                    }
                    HStack(spacing: 4) {
                        Circle().fill(Color.orange).frame(width: 8, height: 8)
                        Text("In Progress").font(.caption2).foregroundColor(.secondary)
                    }
                }
            }
            
            // Bar chart container
            ZStack(alignment: .top) {
                // Goal dashed line
                GeometryReader { geo in
                    let yPos = geo.size.height * (1.0 - CGFloat(goalSteps) / CGFloat(maxSteps))
                    Path { path in
                        path.move(to: CGPoint(x: 0, y: yPos))
                        path.addLine(to: CGPoint(x: geo.size.width, y: yPos))
                    }
                    .stroke(style: StrokeStyle(lineWidth: 1, dash: [4, 4]))
                    .foregroundColor(Color.secondary.opacity(0.4))
                }
                
                HStack(alignment: .bottom, spacing: 8) {
                    ForEach(records) { record in
                        let isSelected = calendar.isDate(record.date, inSameDayAs: selectedDate)
                        let isMet = record.stepCount >= goalSteps
                        let barColor = isMet ? themeColor : (record.stepCount > 0 ? Color.orange : Color.gray.opacity(0.3))
                        
                        VStack(spacing: 6) {
                            // Step bar
                            GeometryReader { geo in
                                let heightFraction = CGFloat(record.stepCount) / CGFloat(maxSteps)
                                let barHeight = max(4, geo.size.height * heightFraction)
                                
                                VStack {
                                    Spacer()
                                    RoundedRectangle(cornerRadius: 6)
                                        .fill(
                                            LinearGradient(
                                                colors: [barColor, barColor.opacity(0.75)],
                                                startPoint: .top,
                                                endPoint: .bottom
                                            )
                                        )
                                        .frame(height: barHeight)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 6)
                                                .stroke(isSelected ? Color.white : Color.clear, lineWidth: 2)
                                        )
                                        .shadow(color: isSelected ? barColor.opacity(0.5) : .clear, radius: 6)
                                }
                            }
                            
                            // Day label
                            Text(dayFormatter.string(from: record.date))
                                .font(.system(size: 11, weight: isSelected ? .bold : .medium))
                                .foregroundColor(isSelected ? .primary : .secondary)
                        }
                        .contentShape(Rectangle())
                        .onTapGesture {
                            onSelectDate(record.date)
                        }
                    }
                }
                .frame(height: 160)
            }
        }
        .padding(16)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(18)
    }
}
