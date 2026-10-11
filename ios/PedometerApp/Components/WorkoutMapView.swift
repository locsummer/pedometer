import SwiftUI
import MapKit

public struct WorkoutMapView: UIViewRepresentable {
    public let routePoints: [LocationPoint]
    public let isLive: Bool
    
    public init(routePoints: [LocationPoint], isLive: Bool = true) {
        self.routePoints = routePoints
        self.isLive = isLive
    }
    
    public func makeUIView(context: Context) -> MKMapView {
        let mapView = MKMapView()
        mapView.delegate = context.coordinator
        mapView.showsUserLocation = isLive
        mapView.userTrackingMode = isLive ? .followWithHeading : .none
        mapView.mapType = .standard
        return mapView
    }
    
    public func updateUIView(_ uiView: MKMapView, context: Context) {
        let coordinates = routePoints.map { $0.coordinate }
        
        // Remove old overlays
        uiView.removeOverlays(uiView.overlays)
        
        if coordinates.count > 1 {
            let polyline = MKPolyline(coordinates: coordinates, count: coordinates.count)
            uiView.addOverlay(polyline)
            
            if !isLive {
                uiView.setVisibleMapRect(
                    polyline.boundingMapRect,
                    edgePadding: UIEdgeInsets(top: 40, left: 40, bottom: 40, right: 40),
                    animated: true
                )
            }
        }
    }
    
    public func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    public class Coordinator: NSObject, MKMapViewDelegate {
        var parent: WorkoutMapView
        
        init(_ parent: WorkoutMapView) {
            self.parent = parent
        }
        
        public func mapView(_ mapView: MKMapView, rendererFor overlay: MKOverlay) -> MKOverlayRenderer {
            if let polyline = overlay as? MKPolyline {
                let renderer = MKPolylineRenderer(polyline: polyline)
                renderer.strokeColor = UIColor.systemGreen
                renderer.lineWidth = 5
                renderer.lineCap = .round
                renderer.lineJoin = .round
                return renderer
            }
            return MKOverlayRenderer(overlay: overlay)
        }
    }
}
