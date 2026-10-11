import SwiftUI

public struct MainTabView: View {
    @StateObject private var dashboardVM = DashboardViewModel()
    @StateObject private var workoutVM = WorkoutViewModel()
    @StateObject private var badgesVM = BadgesViewModel()
    @StateObject private var settingsVM = SettingsViewModel()
    
    @State private var selectedTab: Int = 0
    
    public var body: some View {
        TabView(selection: $selectedTab) {
            DashboardView(viewModel: dashboardVM)
                .tabItem {
                    Label("Today", systemImage: "figure.walk")
                }
                .tag(0)
            
            WorkoutView(viewModel: workoutVM)
                .tabItem {
                    Label("Workouts", systemImage: "figure.run")
                }
                .tag(1)
            
            TrendsView(dashboardVM: dashboardVM)
                .tabItem {
                    Label("Trends", systemImage: "chart.bar.xaxis")
                }
                .tag(2)
            
            BadgesView(viewModel: badgesVM)
                .tabItem {
                    Label("Badges", systemImage: "medal.fill")
                }
                .tag(3)
            
            SettingsView(viewModel: settingsVM)
                .tabItem {
                    Label("Settings", systemImage: "gearshape.fill")
                }
                .tag(4)
        }
        .tint(settingsVM.settings.themeColor.color)
        .preferredColorScheme(preferredColorScheme)
    }
    
    private var preferredColorScheme: ColorScheme? {
        switch settingsVM.settings.colorScheme {
        case .dark: return .dark
        case .light: return .light
        case .system: return nil
        }
    }
}
