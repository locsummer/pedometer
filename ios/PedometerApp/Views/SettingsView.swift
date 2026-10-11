import SwiftUI

public struct SettingsView: View {
    @ObservedObject var viewModel: SettingsViewModel
    @StateObject private var healthKitManager = HealthKitManager.shared
    @State private var showingResetAlert = false
    
    public var body: some View {
        NavigationStack {
            Form {
                Section(header: Text("Daily Targets")) {
                    Stepper(value: $viewModel.settings.stepGoal, in: 1000...50000, step: 500) {
                        HStack {
                            Text("Step Goal")
                            Spacer()
                            Text("\(viewModel.settings.stepGoal.formatted()) steps")
                                .foregroundColor(.secondary)
                        }
                    }
                    
                    Stepper(value: $viewModel.settings.floorGoal, in: 1...100, step: 1) {
                        HStack {
                            Text("Floors Goal")
                            Spacer()
                            Text("\(viewModel.settings.floorGoal) flights")
                                .foregroundColor(.secondary)
                        }
                    }
                }
                
                Section(header: Text("Appearance & Theme")) {
                    Picker("Theme Accent", selection: $viewModel.settings.themeColor) {
                        ForEach(AppThemeColor.allCases) { theme in
                            HStack {
                                Circle().fill(theme.color).frame(width: 14, height: 14)
                                Text(theme.rawValue)
                            }.tag(theme)
                        }
                    }
                    
                    Picker("Color Mode", selection: $viewModel.settings.colorScheme) {
                        ForEach(ColorSchemePreference.allCases) { mode in
                            Text(mode.rawValue).tag(mode)
                        }
                    }
                }
                
                Section(header: Text("Units & Sensors")) {
                    Toggle("Use Metric Units (km/m)", isOn: $viewModel.settings.useMetricUnits)
                    
                    HStack {
                        Text("Apple HealthKit Sync")
                        Spacer()
                        if healthKitManager.isAuthorized {
                            Text("Connected")
                                .foregroundColor(.green)
                                .font(.caption)
                        } else {
                            Button("Authorize") {
                                Task {
                                    try? await healthKitManager.requestAuthorization()
                                }
                            }
                            .font(.caption)
                        }
                    }
                    
                    Toggle("Haptic Feedback", isOn: $viewModel.settings.enableHaptics)
                    Toggle("Goal Notifications", isOn: $viewModel.settings.enableNotifications)
                }
                
                Section(header: Text("About")) {
                    HStack {
                        Text("Version")
                        Spacer()
                        Text("5.0.0 (Native Swift)")
                            .foregroundColor(.secondary)
                    }
                    
                    HStack {
                        Text("Coprocessor Mode")
                        Spacer()
                        Text(PedometerService.shared.isStepCountingAvailable ? "Hardware Active (CMPedometer)" : "Simulated")
                            .foregroundColor(.secondary)
                    }
                }
                
                Section {
                    Button(role: .destructive) {
                        showingResetAlert = true
                    } label: {
                        Text("Reset All Settings to Default")
                    }
                }
            }
            .navigationTitle("Settings")
            .navigationBarTitleDisplayMode(.inline)
            .alert("Reset Settings?", isPresented: $showingResetAlert) {
                Button("Cancel", role: .cancel) {}
                Button("Reset", role: .destructive) {
                    viewModel.resetDefaults()
                }
            } message: {
                Text("This will restore default goals and appearance preferences.")
            }
        }
    }
}
