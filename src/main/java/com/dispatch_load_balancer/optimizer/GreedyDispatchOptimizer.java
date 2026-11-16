package com.dispatch_load_balancer.optimizer;

import com.dispatch_load_balancer.entity.DeliveryOrder;
import com.dispatch_load_balancer.entity.Vehicle;
import com.dispatch_load_balancer.enums.Priority;
import com.dispatch_load_balancer.geo.HaversineDistanceCalculator;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class GreedyDispatchOptimizer implements DispatchOptimizer {

    private static final double EPSILON = 0.0000001;

    private final HaversineDistanceCalculator distanceCalculator;

    public GreedyDispatchOptimizer(
            HaversineDistanceCalculator distanceCalculator) {
        this.distanceCalculator = distanceCalculator;
    }

    @Override
    public OptimizationResult optimize(
            List<DeliveryOrder> orders,
            List<Vehicle> vehicles) {

        List<Vehicle> sortedVehicles = vehicles.stream()
                .sorted(Comparator.comparing(Vehicle::getVehicleId))
                .toList();

        List<DeliveryOrder> sortedOrders = orders.stream()
                .sorted(
                        Comparator.comparingInt(
                                        (DeliveryOrder o) -> priorityRank(o.getPriority())
                                )
                                .thenComparing(
                                        DeliveryOrder::getPackageWeight,
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(DeliveryOrder::getOrderId)
                )
                .toList();

        List<RouteState> states = new ArrayList<>();

        for (Vehicle vehicle : sortedVehicles) {
            states.add(new RouteState(vehicle));
        }

        List<UnassignedOrder> unassigned = new ArrayList<>();

        for (DeliveryOrder order : sortedOrders) {

            RouteState bestState = null;
            int bestPosition = -1;
            double bestIncrement = Double.POSITIVE_INFINITY;

            for (RouteState state : states) {

                if (!hasCapacity(state, order)) {
                    continue;
                }

                double currentDistance = routeDistance(
                        state.vehicle, state.orders
                );

                for (int position = 0;
                     position <= state.orders.size();
                     position++) {

                    List<DeliveryOrder> candidate =
                            new ArrayList<>(state.orders);

                    candidate.add(position, order);

                    double candidateDistance = routeDistance(
                            state.vehicle, candidate
                    );

                    double increment =
                            candidateDistance - currentDistance;

                    if (increment < bestIncrement - EPSILON) {
                        bestIncrement = increment;
                        bestState = state;
                        bestPosition = position;
                    }
                }
            }

            if (bestState == null) {
                unassigned.add(new UnassignedOrder(
                        order,
                        determineReason(order, states)
                ));
            } else {
                bestState.orders.add(bestPosition, order);
                bestState.totalLoad =
                        bestState.totalLoad.add(order.getPackageWeight());
            }
        }

        List<VehicleRoute> routes = new ArrayList<>();

        for (RouteState state : states) {
            improveRouteWithTwoOpt(state);

            routes.add(new VehicleRoute(
                    state.vehicle,
                    List.copyOf(state.orders),
                    state.totalLoad,
                    routeDistance(state.vehicle, state.orders)
            ));
        }

        return new OptimizationResult(
                List.copyOf(routes),
                List.copyOf(unassigned)
        );
    }

    private boolean hasCapacity(
            RouteState state,
            DeliveryOrder order) {

        return state.totalLoad
                .add(order.getPackageWeight())
                .compareTo(state.vehicle.getCapacity()) <= 0;
    }

    private UnassignedReason determineReason(
            DeliveryOrder order,
            List<RouteState> states) {

        BigDecimal weight = order.getPackageWeight();

        if (states.isEmpty()) {
            return UnassignedReason.NO_FEASIBLE_ASSIGNMENT;
        }

        BigDecimal maxCapacity = states.stream()
                .map(s -> s.vehicle.getCapacity())
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        if (weight.compareTo(maxCapacity) > 0) {
            return UnassignedReason.EXCEEDS_MAX_VEHICLE_CAPACITY;
        }

        BigDecimal remainingCapacity = states.stream()
                .map(s -> s.vehicle.getCapacity().subtract(s.totalLoad))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (remainingCapacity.compareTo(weight) < 0) {
            return UnassignedReason.FLEET_CAPACITY_EXHAUSTED;
        }

        return UnassignedReason.NO_FEASIBLE_ASSIGNMENT;
    }

    private void improveRouteWithTwoOpt(RouteState state) {

        List<DeliveryOrder> route = state.orders;

        if (route.size() < 3) {
            return;
        }

        boolean improved;

        do {
            improved = false;
            double bestDistance = routeDistance(state.vehicle, route);

            for (int i = 0; i < route.size() - 1; i++) {
                for (int k = i + 1; k < route.size(); k++) {

                    List<DeliveryOrder> candidate =
                            new ArrayList<>(route);

                    reverseSegment(candidate, i, k);

                    double candidateDistance =
                            routeDistance(state.vehicle, candidate);

                    if (candidateDistance < bestDistance - EPSILON) {
                        route.clear();
                        route.addAll(candidate);

                        bestDistance = candidateDistance;
                        improved = true;
                    }
                }
            }

        } while (improved);
    }

    private void reverseSegment(
            List<DeliveryOrder> route,
            int start,
            int end) {

        while (start < end) {
            DeliveryOrder temp = route.get(start);
            route.set(start, route.get(end));
            route.set(end, temp);

            start++;
            end--;
        }
    }

    private double routeDistance(
            Vehicle vehicle,
            List<DeliveryOrder> orders) {

        if (orders.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;

        double previousLatitude = vehicle.getCurrentLatitude();
        double previousLongitude = vehicle.getCurrentLongitude();

        for (DeliveryOrder order : orders) {

            total += distanceCalculator.calculate(
                    previousLatitude,
                    previousLongitude,
                    order.getLatitude(),
                    order.getLongitude()
            );

            previousLatitude = order.getLatitude();
            previousLongitude = order.getLongitude();
        }

        return total;
    }

    private int priorityRank(Priority priority) {
        return switch (priority) {
            case HIGH -> 0;
            case MEDIUM -> 1;
            case LOW -> 2;
        };
    }

    private static class RouteState {

        private final Vehicle vehicle;
        private final List<DeliveryOrder> orders = new ArrayList<>();
        private BigDecimal totalLoad = BigDecimal.ZERO;

        private RouteState(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
    }
}

