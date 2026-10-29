package vn.edu.sgu.damsenguide.service;

import org.springframework.stereotype.Component;

@Component
public class GeoDistanceCalculator {

    private static final double EARTH_RADIUS_M = 6_371_008.8; // Bán kính trung bình Trái Đất (m)

    public double calculateHaversineMeters(double lat1, double lon1, double lat2, double lon2) {
        if (!Double.isFinite(lat1) || !Double.isFinite(lon1) || !Double.isFinite(lat2) || !Double.isFinite(lon2)) {
            throw new IllegalArgumentException("INVALID_LOCATION");
        }
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double rLat1 = Math.toRadians(lat1);
        double rLat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                 + Math.cos(rLat1) * Math.cos(rLat2) * Math.pow(Math.sin(dLon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_M * c;
    }
}
