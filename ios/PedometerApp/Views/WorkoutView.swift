import SwiftUI
import MapKit

public struct WorkoutView: View {
    @ObservedObject var viewModel: WorkoutViewModel
    @State private var showingSaveAlert = false
    
    public var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // Top Live Map / Route View
                ZStack(alignment: .bottomTrailing) {
                    WorkoutMapView(routePoints: viewModel.currentWorkout.routePoints, isLive: viewModel.isWorkoutActive)
                        .frame(maxHeight: 280)
                        .cornerRadius(20)
                        .padding(.horizontal)
                        .padding(.top, 8)
                    
                    if viewModel.isWorkoutActive {
                        HStack(spacing: 6) {
                            Circle().fill(Color.red).frame(width: 8, height: 8)
                            Text("LIVE GPS")
                                .font(.caption2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 5)
                        .background(Color.black.opacity(0.75))
                        .cornerRadius(12)
                        .padding(24)
                    }
                }
                
                // Workout Metrics Dashboard
                ScrollView {
                    VStack(spacing: 16) {
                        // Workout Type Picker
                        if !viewModel.isWorkoutActive {
                            Picker("Workout Type", selection: $viewModel.selectedType) {
                                ForEach(WorkoutType.allCases) { type in
                                    Label(type.rawValue, systemImage: type.iconName).tag(type)
                                }
                            }
                            .pickerStyle(.segmented)
                            .padding(.horizontal)
                            .padding(.top, 8)
                        }
                        
                        // Main Timer & Pace Block
                        VStack(spacing: 4) {
                            Text(viewModel.currentWorkout.formattedDuration)
                                .font(.system(size: 48, weight: .heavy, design: .monospaced))
                                .foregroundColor(.primary)
                            
                            Text("DURATION")
                                .font(.caption)
                                .fontWeight(.bold)
                                .foregroundColor(.secondary)
                        }
                        .padding(.top, 12)
                        
                        // 4 Big Metrics
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                            MetricCard(
                                icon: "arrow.triangle.swap",
                                title: "Distance",
                                value: String(format: "%.2f km", viewModel.currentWorkout.distanceMeters / 1000.0),
                                tintColor: .green
                            )
                            
                            MetricCard(
                                icon: "speedometer",
                                title: "Avg Pace",
                                value: viewModel.currentWorkout.formattedPace,
                                tintColor: .blue
                            )
                            
                            MetricCard(
                                icon: "flame.fill",
                                title: "Burned",
                                value: "\(Int(viewModel.currentWorkout.caloriesBurned)) kcal",
                                tintColor: .orange
                            )
                            
                            MetricCard(
                                icon: "figure.walk",
                                title: "Steps",
                                value: "\(viewModel.currentWorkout.stepCount)",
                                tintColor: .purple
                            )
                        }
                        .padding(.horizontal)
                        
                        // Controls: Start / Pause / Stop
                        HStack(spacing: 20) {
                            if !viewModel.isWorkoutActive {
                                Button {
                                    viewModel.startWorkout()
                                } label: {
                                    HStack {
                                        Image(systemName: "play.fill")
                                        Text("START WORKOUT")
                                    }
                                    .font(.headline)
                                    .foregroundColor(.black)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 16)
                                    .background(Color.green)
                                    .cornerRadius(16)
                                }
                            } else {
                                Button {
                                    if viewModel.isPaused {
                                        viewModel.resumeWorkout()
                                    } else {
                                        viewModel.pauseWorkout()
                                    }
                                } label: {
                                    Image(systemName: viewModel.isPaused ? "play.fill" : "pause.fill")
                                        .font(.title2)
                                        .foregroundColor(.white)
                                        .frame(width: 70, height: 70)
                                        .background(Color.orange)
                                        .clipShape(Circle())
                                }
                                
                                Button {
                                    viewModel.stopWorkout()
                                } label: {
                                    Image(systemName: "stop.fill")
                                        .font(.title2)
                                        .foregroundColor(.white)
                                        .frame(width: 70, height: 70)
                                        .background(Color.red)
                                        .clipShape(Circle())
                                }
                            }
                        }
                        .padding(.horizontal, 24)
                        .padding(.top, 10)
                        
                        // Past Workouts History
                        if !viewModel.pastWorkouts.isEmpty {
                            VStack(alignment: .leading, spacing: 10) {
                                Text("RECENT WORKOUTS")
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(.secondary)
                                    .padding(.horizontal)
                                    .padding(.top, 14)
                                
                                ForEach(viewModel.pastWorkouts.prefix(5)) { past in
                                    HStack {
                                        Image(systemName: past.type.iconName)
                                            .font(.title3)
                                            .foregroundColor(.green)
                                            .frame(width: 36, height: 36)
                                            .background(Color.green.opacity(0.15))
                                            .clipShape(Circle())
                                        
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(past.type.rawValue)
                                                .font(.subheadline)
                                                .fontWeight(.bold)
                                            Text(past.startDate.formatted(date: .abbreviated, time: .shortened))
                                                .font(.caption2)
                                                .foregroundColor(.secondary)
                                        }
                                        
                                        Spacer()
                                        
                                        VStack(alignment: .trailing, spacing: 2) {
                                            Text(String(format: "%.2f km", past.distanceMeters / 1000.0))
                                                .font(.subheadline)
                                                .fontWeight(.heavy)
                                            Text(past.formattedDuration)
                                                .font(.caption2)
                                                .foregroundColor(.secondary)
                                        }
                                    }
                                    .padding(12)
                                    .background(Color(.secondarySystemBackground))
                                    .cornerRadius(14)
                                    .padding(.horizontal)
                                }
                            }
                        }
                    }
                    .padding(.bottom, 30)
                }
            }
            .navigationTitle("Workout Tracker")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
