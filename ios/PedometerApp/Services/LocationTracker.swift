import Foundation
import CoreLocation
import Combine

public final class LocationTracker: NSObject, ObservableObject, CLLocationManagerDelegate {
    public static let shared = LocationTracker()
    
    private let locationManager = CLLocationManager()
    
    @Published public var authorizationStatus: CLAuthorizationStatus = .notDetermined
    @Published public var currentLocation: CLLocation?
    @Published public var routeLocations: [LocationPoint] = []
    @Published public var currentSpeedMps: Double = 0
    @Published public var totalDistanceMeters: Double = 0
    @Published public var elevationGainMeters: Double = 0
    @Published public var isTracking: Bool = false
    
    private var lastValidLocation: CLLocation?
    
    override private init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation
        locationManager.activityType = .fitness
        locationManager.distanceFilter = 2.0 // meters
        locationManager.allowsBackgroundLocationUpdates = false
        authorizationStatus = locationManager.authorizationStatus
    }
    
    public func requestPermission() {
        locationManager.requestWhenInUseAuthorization()
    }
    
    public func startWorkout() {
        guard authorizationStatus == .authorizedWhenInUse || authorizationStatus == .authorizedAlways else {
            requestPermission()
            return
        }
        
        routeLocations.removeAll()
        totalDistanceMeters = 0
        elevationGainMeters = 0
        lastValidLocation = nil
        isTracking = true
        
        locationManager.startUpdatingLocation()
    }
    
    public func stopWorkout() {
        isTracking = false
        locationManager.stopUpdatingLocation()
        currentSpeedMps = 0
    }
    
    public func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        DispatchQueue.main.async {
            self.authorizationStatus = manager.authorizationStatus
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard isTracking, let newLocation = locations.last, newLocation.horizontalAccuracy >= 0 && newLocation.horizontalAccuracy < 30 else {
            return
        }
        
        DispatchQueue.main.async {
            self.currentLocation = newLocation
            self.currentSpeedMps = max(0, newLocation.speed)
            
            if let lastLoc = self.lastValidLocation {
                let deltaDist = newLocation.distance(from: lastLoc)
                if deltaDist > 1.0 { // filter jitter
                    self.totalDistanceMeters += deltaDist
                    
                    let deltaAlt = newLocation.altitude - lastLoc.altitude
                    if deltaAlt > 0 {
                        self.elevationGainMeters += deltaAlt
                    }
                }
            }
            
            self.lastValidLocation = newLocation
            
            let point = LocationPoint(
                coordinate: newLocation.coordinate,
                altitude: newLocation.altitude,
                timestamp: newLocation.timestamp,
                speed: max(0, newLocation.speed)
            )
            self.routeLocations.append(point)
        }
    }
}
