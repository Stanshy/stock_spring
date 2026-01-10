# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

```bash
# Build
./mvnw clean compile                    # Compile source
./mvnw clean package                    # Build JAR artifact
./mvnw clean install                    # Full build with tests

# Run Application
./mvnw spring-boot:run                  # Run with default (dev) profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod  # Run with prod profile

# Testing
./mvnw test                             # Run all tests
./mvnw test -Dtest=ClassName            # Run specific test class
./mvnw test -Dtest=ClassName#methodName # Run specific test method

# Windows: use mvnw.cmd instead of ./mvnw
```

## Technology Stack

- **Java 21** with **Spring Boot 3.5.9**
- **PostgreSQL** (production) / **H2** (testing)
- **JPA + MyBatis** dual data access layer
- **Quartz** for job scheduling
- **Redis** for caching
- **MapStruct** for DTO conversions
- **OpenAPI/Swagger** at `/swagger-ui.html`

## Architecture Overview

### Module Structure

The codebase is organized into domain modules under `src/main/java/com/chris/fin_shark/`:

```
common/     - Shared infrastructure (config, exceptions, DTOs, utilities, aspects)
client/     - External API clients (TWSE, FinMind, Yahoo Finance)
m06/        - Data Management: stock data sync, job management
m07/        - Technical Analysis: 71 indicators (RSI, MACD, Bollinger, etc.)
m08/        - Fundamental Analysis: 75 financial calculators (P/E, ROE, etc.)
```

### Layered Architecture

Each module follows: `Controller → Service → Repository/Mapper → Domain → Database`

### Calculation Engine Pattern (M07 & M08)

Both analysis modules share the same architecture:
- **Engine Interface** (`IndicatorEngine`/`FundamentalEngine`): Defines calculation contract
- **Default Implementation**: Orchestrates multiple calculators
- **Calculator Registry**: Auto-discovers and registers calculators via Spring
- **Calculation Plan**: Strategy for selecting which indicator categories to calculate
- **Diagnostics Model**: Tracks success/failure with detailed error reporting

Calculators are organized by category:
- M07: `trend/`, `momentum/`, `volatility/`, `volume/`, `oscillator/`
- M08: `valuation/`, `profitability/`, `solvency/`, `structure/`, `cashflow/`

### Data Access Strategy

- **JPA Repositories**: Standard CRUD operations
- **MyBatis Mappers**: Complex analytical queries (XML in `src/main/resources/mapper/`)
- MyBatis configured with underscore-to-camelCase auto-mapping

### API Response Format

All REST APIs return standardized responses:
```json
{
  "code": 200,
  "message": "Success",
  "data": { ... },
  "timestamp": "2024-12-22T13:30:00+08:00",
  "trace_id": "req_abc123xyz"
}
```

## Configuration Profiles

- **dev** (default): localhost PostgreSQL, DEBUG logging, all actuator endpoints
- **test**: H2 in-memory, create-drop schema, SQL logging
- **prod**: Environment variables for secrets, WARN logging, restricted actuator

Production environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

## Project Documentation

Comprehensive documentation exists in `docs/`:
- `docs/specs/` - Functional and technical specifications
- `docs/design/` - Architecture and database design
- `docs/00-README-文件導航總覽.md` - Navigation guide
