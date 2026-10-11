import SwiftUI

public struct StepRingView: View {
    public let currentSteps: Int
    public let goalSteps: Int
    public let themeColor: Color
    
    public init(currentSteps: Int, goalSteps: Int, themeColor: Color = .green) {
        self.currentSteps = currentSteps
        self.goalSteps = max(1, goalSteps)
        self.themeColor = themeColor
    }
    
    private var progress: Double {
        return min(1.0, Double(currentSteps) / Double(goalSteps))
    }
    
    private var isGoalReached: Bool {
        return currentSteps >= goalSteps
    }
    
    public var body: some View {
        ZStack {
            // Background ring track
            Circle()
                .stroke(
                    Color.gray.opacity(0.2),
                    style: StrokeStyle(lineWidth: 22, lineCap: .round)
                )
            
            // Progress gradient ring
            Circle()
                .trim(from: 0.0, to: CGFloat(progress))
                .stroke(
                    LinearGradient(
                        colors: [themeColor.opacity(0.8), themeColor],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    ),
                    style: StrokeStyle(lineWidth: 22, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
                .animation(.spring(response: 0.8, dampingFraction: 0.75), value: progress)
                .shadow(color: isGoalReached ? themeColor.opacity(0.6) : .clear, radius: 12, x: 0, y: 0)
            
            // Inner content
            VStack(spacing: 4) {
                Image(systemName: isGoalReached ? "checkmark.seal.fill" : "figure.walk")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(isGoalReached ? themeColor : .secondary)
                
                Text("\(currentSteps.formatted())")
                    .font(.system(size: 42, weight: .heavy, design: .rounded))
                    .foregroundColor(.primary)
                
                Text("GOAL: \(goalSteps.formatted())")
                    .font(.system(size: 13, weight: .semibold, design: .rounded))
                    .foregroundColor(.secondary)
                    .tracking(0.5)
                
                if isGoalReached {
                    Text("\(Int(Double(currentSteps) / Double(goalSteps) * 100))%")
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(themeColor)
                        .padding(.horizontal, 10)
                        .padding(.vertical, 3)
                        .background(themeColor.opacity(0.15))
                        .clipShape(Capsule())
                }
            }
        }
        .frame(width: 240, height: 240)
        .padding()
    }
}
