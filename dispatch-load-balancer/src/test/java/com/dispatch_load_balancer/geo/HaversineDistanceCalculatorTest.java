package com.dispatch_load_balancer.geo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HaversineDistanceCalculatorTest {

    private final HaversineDistanceCalculator calculator =
            new HaversineDistanceCalculator();

    @Test
    void sameCoordinatesShouldReturnZero() {
        double distance = calculator.calculate(
                28.6139, 77.2090,
                28.6139, 77.2090
        );

        assertEquals(0.0, distance, 0.000001);
    }

    @Test
    void shouldCalculateDistanceBetweenDelhiAndNoida() {
        double distance = calculator.calculate(
                28.6139, 77.2090,
                28.5355, 77.3910
        );

        assertTrue(distance > 15 && distance < 25);
    }

    @Test
    void shouldRejectInvalidLatitude() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(91, 77, 28, 77)
        );
    }

    @Test
    void shouldRejectInvalidLongitude() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(28, 181, 28, 77)
        );
    }
<<<<<<< HEAD

    @Test
    void distanceShouldBeSymmetric() {
        double forward = calculator.calculate(
                28.6139, 77.2090, 28.5355, 77.3910
        );
        double reverse = calculator.calculate(
                28.5355, 77.3910, 28.6139, 77.2090
        );

        assertEquals(forward, reverse, 0.000001);
    }

    @Test
    void antipodalPointsShouldBeApproximatelyHalfEarthCircumference() {
        double distance = calculator.calculate(0, 0, 0, 180);

        assertEquals(Math.PI * 6371.0, distance, 0.001);
    }

    @Test
    void shouldRejectNonFiniteCoordinates() {
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(Double.NaN, 0, 0, 0)
        );
        assertThrows(IllegalArgumentException.class, () ->
                calculator.calculate(0, 0, 0, Double.POSITIVE_INFINITY)
        );
    }

=======
>>>>>>> f845e074c40d1f06fae4aba35239310840a672cd
}
