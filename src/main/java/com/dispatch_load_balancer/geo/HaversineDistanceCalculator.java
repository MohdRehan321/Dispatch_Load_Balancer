package com.dispatch_load_balancer.geo;

import org.springframework.stereotype.Component;

@Component
public class HaversineDistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    public double calculate(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2) {

        validateCoordinates(latitude1, longitude1);
        validateCoordinates(latitude2, longitude2);

        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);

        double deltaLat = Math.toRadians(latitude2 - latitude1);
        double deltaLon = Math.toRadians(longitude2 - longitude1);

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(deltaLon / 2), 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return EARTH_RADIUS_KM * c;
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid geographical coordinates");
        }
    }
}

