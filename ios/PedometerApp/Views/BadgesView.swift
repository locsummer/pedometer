import SwiftUI

public struct BadgesView: View {
    @ObservedObject var viewModel: BadgesViewModel
    
    private let columns = [
        GridItem(.flexible()),
        GridItem(.flexible())
    ]
    
    public var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    ForEach(BadgeCategory.allCases, id: \.self) { category in
                        let categoryBadges = viewModel.badges.filter { $0.category == category }
                        if !categoryBadges.isEmpty {
                            VStack(alignment: .leading, spacing: 12) {
                                Text(category.rawValue.uppercased())
                                    .font(.caption)
                                    .fontWeight(.bold)
                                    .foregroundColor(.secondary)
                                    .tracking(0.5)
                                    .padding(.horizontal)
                                
                                LazyVGrid(columns: columns, spacing: 14) {
                                    ForEach(categoryBadges) { badge in
                                        BadgeCard(badge: badge)
                                    }
                                }
                                .padding(.horizontal)
                            }
                        }
                    }
                }
                .padding(.vertical)
            }
            .navigationTitle("Achievements")
            .navigationBarTitleDisplayMode(.inline)
            .onAppear {
                viewModel.refreshBadges()
            }
        }
    }
}

public struct BadgeCard: View {
    public let badge: Badge
    
    public var body: some View {
        VStack(spacing: 10) {
            ZStack {
                Circle()
                    .fill(badge.isUnlocked ? Color.orange.opacity(0.2) : Color.gray.opacity(0.1))
                    .frame(width: 64, height: 64)
                
                Image(systemName: badge.iconName)
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(badge.isUnlocked ? .orange : .gray.opacity(0.4))
                
                if badge.isUnlocked {
                    Circle()
                        .stroke(Color.orange, lineWidth: 2)
                        .frame(width: 68, height: 68)
                }
            }
            .padding(.top, 4)
            
            VStack(spacing: 2) {
                Text(badge.title)
                    .font(.subheadline)
                    .fontWeight(.bold)
                    .multilineTextAlignment(.center)
                    .foregroundColor(badge.isUnlocked ? .primary : .secondary)
                
                Text(badge.description)
                    .font(.caption2)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
            
            // Progress Bar
            if !badge.isUnlocked {
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(Color.gray.opacity(0.2))
                            .frame(height: 5)
                        Capsule()
                            .fill(Color.orange)
                            .frame(width: geo.size.width * CGFloat(badge.progressRatio), height: 5)
                    }
                }
                .frame(height: 5)
                .padding(.horizontal, 10)
            } else {
                Text("UNLOCKED")
                    .font(.system(size: 9, weight: .heavy))
                    .foregroundColor(.orange)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(Color.orange.opacity(0.15))
                    .clipShape(Capsule())
            }
        }
        .padding(14)
        .frame(maxWidth: .infinity, minHeight: 160)
        .background(Color(.secondarySystemBackground))
        .cornerRadius(16)
        .opacity(badge.isUnlocked ? 1.0 : 0.7)
    }
}
