
# Dispatch Load Balancer

A Spring Boot REST API that assigns delivery orders to vehicles based on delivery priority, package weight, vehicle capacity, and geographical distance.

## Features

- Upload and replace delivery orders and vehicles.
- Validate request data and reject duplicate IDs.
- Calculate geographical distances using the Haversine formula.
- Assign HIGH priority orders before MEDIUM and LOW priority orders.
- Process heavier orders first within the same priority.
- Enforce vehicle capacity without splitting orders.
- Optimize vehicle routes using greedy minimum-distance insertion and 2-opt.
- Return assigned and unassigned orders with reasons.
- Persist data using PostgreSQL, Spring Data JPA, and Flyway.
- Provide API documentation through Swagger UI.

## Technology Stack

- Java 17
- Spring Boot 4.1.2
- Spring Web MVC
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- JUnit

## Architecture

The application follows a layered architecture:

Controller → Service → Optimizer → Geo Calculator / Repository

Main packages:

- `controller` — REST API endpoints
- `dto` — request and response objects
- `entity` — database entities
- `enums` — priority definitions
- `exception` — centralized error handling
- `geo` — Haversine distance calculation
- `mapper` — DTO/entity conversion
- `optimizer` — dispatch assignment and route optimization
- `repository` — database operations
- `service` — application business logic
- `validation` — request ID validation

## Prerequisites

Install the following:

- Java 17
- PostgreSQL
- Git (if cloning the repository)

The project includes Maven Wrapper, so a separate Maven installation is not required.

## Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE dispatch_db;
```

Configure the database connection using environment variables.

### Windows PowerShell

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/dispatch_db"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "YOUR_POSTGRES_PASSWORD"
```

Replace the username and password with your actual PostgreSQL credentials.

Do not commit database passwords or other secrets to source control.

Flyway automatically applies the database migrations when the application starts.

## Build and Run

Open a terminal in the project root.

Run tests:

```powershell
.\mvnw.cmd clean test
```

Run the application:

```powershell
.\mvnw.cmd spring-boot:run
```

The default application URL is:

`http://localhost:8080`

## API Endpoints

Base URL:

`http://localhost:8080/api/dispatch`

### 1. Upload Orders

**POST** `/orders`

Example request:

```json
{
  "orders": [
    {
      "orderId": "ORD-001",
      "latitude": 28.6139,
      "longitude": 77.2090,
      "address": "New Delhi",
      "packageWeight": 10.5,
      "priority": "HIGH"
    },
    {
      "orderId": "ORD-002",
      "latitude": 28.5355,
      "longitude": 77.3910,
      "address": "Noida",
      "packageWeight": 5.0,
      "priority": "MEDIUM"
    }
  ]
}
```

### 2. Upload Vehicles

**POST** `/vehicles`

Example request:

```json
{
  "vehicles": [
    {
      "vehicleId": "VEH-001",
      "capacity": 100.0,
      "currentLatitude": 28.6139,
      "currentLongitude": 77.2090,
      "currentAddress": "New Delhi Depot"
    },
    {
      "vehicleId": "VEH-002",
      "capacity": 75.0,
      "currentLatitude": 28.5355,
      "currentLongitude": 77.3910,
      "currentAddress": "Noida Depot"
    }
  ]
}
```

### 3. Generate Dispatch Plan

**GET** `/plan`

Generates an optimized dispatch plan using the currently stored orders and vehicles.

The response includes:

- Total number of orders
- Assigned and unassigned order counts
- Total fleet distance in kilometers
- Vehicle-wise assigned orders and route distances
- Reasons for unassigned orders

## Optimization Rules

1. Orders are processed in HIGH, MEDIUM, LOW priority order.
2. Heavier orders are processed first within the same priority.
3. Orders are inserted into feasible vehicle routes using minimum incremental distance.
4. Vehicle capacity is strictly enforced.
5. A delivery order is never split between vehicles.
6. 2-opt is applied to improve routes containing at least three orders.
7. Distance is calculated from the vehicle's starting location through its assigned orders.
8. Return-to-depot distance is not included.

## Unassigned Order Reasons

- `EXCEEDS_MAX_VEHICLE_CAPACITY` — the order exceeds the capacity of every vehicle.
- `FLEET_CAPACITY_EXHAUSTED` — the remaining combined fleet capacity is insufficient.
- `NO_FEASIBLE_ASSIGNMENT` — no feasible assignment was found.

## API Response Format

Successful API responses use:

```json
{
  "success": true,
  "message": "Dispatch plan generated successfully",
  "data": {}
}
```

Error responses include a timestamp, HTTP status, message, request path, and validation errors where applicable.

Common HTTP status codes:

- `200` — successful request
- `400` — invalid request or duplicate IDs
- `409` — database constraint conflict or dispatch data not ready
- `500` — unexpected server error

## Swagger API Documentation

After starting the application, open:

`http://localhost:8080/swagger-ui.html`

OpenAPI JSON:

`http://localhost:8080/v3/api-docs`

## Testing

Run the complete test suite:

```powershell
.\mvnw.cmd clean test
```

The Phase 5 verification completed with:

- 39 tests
- 0 failures
- 0 errors
- 0 skipped

## Sample Assessment Dataset

The assessment target is a sample dataset of 30 delivery orders and 5 vehicles.

Use the two upload endpoints to submit the dataset, then call the dispatch plan endpoint.

## Limitations

- The optimizer uses a heuristic approach; it does not guarantee a globally optimal route.
- Distances are geographical straight-line distances, not road-network travel distances.
- Return-to-depot distance is excluded.
- Orders are not split between vehicles.
- Authentication and authorization are not included in the current API.
