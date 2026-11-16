# Test Guide

## Test coverage added

- Optimizer: priority and weight ordering, exact capacity, fleet capacity exhaustion,
  no-vehicle handling, deterministic vehicle order, and route-distance calculation.
- Geography: symmetry, antipodal coordinates, and non-finite coordinate rejection.
- Services: active-batch assignment, replacement behavior, repository reads, plan mapping,
  rounding, and missing-data handling.
- Controller: order/vehicle upload responses, duplicate ID rejection, and plan response.
- Validation: invalid order/vehicle fields, empty request lists, and nested DTO validation.
- PostgreSQL integration: order and vehicle replacement and read-back in a transaction
  that is rolled back after the test.

## Run tests on Windows PowerShell

Use a separate local test database so the persistence test cannot overwrite development data.
Create it once in PostgreSQL:

```sql
CREATE DATABASE dispatch_db_test;
```

Set the connection values in the PowerShell window used to run Maven:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/dispatch_db_test"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "YOUR_ROTATED_DATABASE_PASSWORD"
.\mvnw.cmd clean test
```

Flyway applies the schema migration to the test database when the Spring test context starts.
The persistence integration test is transactional and rolls back its test data.

To run only the unit tests without starting the Spring/PostgreSQL context:

```powershell
.\\mvnw.cmd "-Dtest=GreedyDispatchOptimizerTest,HaversineDistanceCalculatorTest,OrderServiceTest,VehicleServiceTest,DispatchPlanServiceTest,DispatchControllerTest,RequestIdValidatorTest,RequestDtoValidationTest" test
```

Do not run the persistence integration test against a production database.
