# Courier Service JDBC Implementation

## Overview

This project implements a complete courier service database system using JDBC. It includes database schema creation, connection management, and all core functions mentioned in the business requirements.

## Files

1. **CourierServiceDB.java** - Main database class with all JDBC functions
2. **CourierServiceDemo.java** - Demo class to test all functions
3. **improved_courier_schema.sql** - SQL schema file
4. **README_JDBC.md** - This setup guide

## Prerequisites

### 1. MySQL Database
- Install MySQL Server (8.0 or higher)
- Create a database named `courier_service`
- Ensure MySQL is running on localhost:3306

### 2. MySQL JDBC Driver
Download the MySQL Connector/J driver:
```bash
# For Maven projects, add to pom.xml:
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

# Or download the JAR file manually and add to classpath
```

### 3. Database Setup
```sql
-- Create the database
CREATE DATABASE courier_service;
USE courier_service;
```

## Configuration

Update the database connection details in `CourierServiceDB.java`:

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/courier_service";
private static final String USER = "your_username";
private static final String PASS = "your_password";
```

## Core Functions Implemented

### 1. Shipment Management
- ✅ **Create Shipment** - `createShipment()`
- ✅ **Add Package** - `addPackageToShipment()`
- ✅ **Update Status** - `updateShipmentStatus()`

### 2. Driver Management
- ✅ **Register Driver** - `registerDriver()`
- ✅ **Assign Shipment** - `assignShipmentToDriver()`
- ✅ **Capacity Management** - Built-in limit checking

### 3. Package Management
- ✅ **Package Reassignment** - `movePackageBetweenShipments()`
- ✅ **Audit Trail** - Complete movement history

### 4. Query Functions
- ✅ **Shipment Status & Log** - `getShipmentStatusAndLog()`
- ✅ **Pending Shipments** - `getPendingShipmentsForDriver()`
- ✅ **Delayed Shipments** - `getDelayedShipments()`
- ✅ **Daily Volume** - `getDailyShipmentVolume()`

## Usage

### 1. Compile the Classes
```bash
javac -cp mysql-connector-java-8.0.33.jar CourierServiceDB.java CourierServiceDemo.java
```

### 2. Run the Demo
```bash
java -cp .:mysql-connector-java-8.0.33.jar CourierServiceDemo
```

### 3. Use in Your Application
```java
// Create database instance
CourierServiceDB db = new CourierServiceDB();

// Create schema
db.createSchema();

// Create shipment
LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(2);
boolean success = db.createShipment(1001, 1, 2, 1, 2, estimatedDelivery);

// Add package
db.addPackageToShipment(2001, 2.5, "Electronics", 1001, 1);

// Update status
db.updateShipmentStatus(1001, "in_transit", 1, "Package picked up");

// Close connection
db.close();
```

## Database Schema

The implementation uses the improved schema that supports:

### Key Features
- **Package Reassignment** - Packages can move between shipments
- **Hierarchical Locations** - Hub → sub-hub → delivery point structure
- **Driver Capacity Management** - Built-in limits and tracking
- **Complete Audit Trail** - All status changes and package movements logged
- **Performance Optimized** - Comprehensive indexing strategy

### Tables
1. **customer** - Customer information
2. **location** - Hierarchical location structure
3. **agent** - Agent information
4. **driver** - Driver details with capacity limits
5. **shipments** - Main shipment entity
6. **package** - Independent package entity
7. **package_shipment_assignment** - Many-to-many relationship
8. **driver_shipment_assignment** - Driver assignments
9. **status_logs** - Shipment status audit trail
10. **package_movement_log** - Package movement audit trail

## Business Requirements Fulfilled

✅ **Shipment Creation** - Complete sender/recipient/destination tracking
✅ **Status Progression** - pending → in_transit → delivered/returned
✅ **Package Management** - Weight, description, reassignment support
✅ **Status Logging** - Complete audit trail with timestamps
✅ **Driver Registration** - License numbers and contact details
✅ **Driver Assignment** - With capacity limits and tracking
✅ **Capacity Limits** - Built-in constraints and validation
✅ **Hierarchical Locations** - Hub structure support
✅ **Performance Metrics** - Delay detection and tracking
✅ **Automatic Flagging** - Delayed shipment detection

## Frequent Queries Supported

✅ **Current Status & Location** - `getShipmentStatusAndLog()`
✅ **Pending Shipments** - `getPendingShipmentsForDriver()`
✅ **Route Performance** - Built into daily volume queries
✅ **Delayed Shipments** - `getDelayedShipments()`
✅ **Daily Volume Reports** - `getDailyShipmentVolume()`

## Error Handling

The implementation includes comprehensive error handling:
- Database connection failures
- SQL execution errors
- Constraint violations
- Capacity limit enforcement
- Data validation

## Performance Features

- **Prepared Statements** - SQL injection prevention
- **Connection Management** - Proper resource cleanup
- **Indexed Queries** - Optimized for frequent operations
- **Batch Operations** - Efficient data processing

## Testing

The demo class (`CourierServiceDemo.java`) tests all functions:
1. Schema creation
2. Sample data insertion
3. Core function testing
4. Query function testing

Run the demo to verify everything works correctly.

## Troubleshooting

### Common Issues

1. **Connection Failed**
   - Check MySQL is running
   - Verify database exists
   - Check username/password

2. **Driver Not Found**
   - Ensure MySQL JDBC driver is in classpath
   - Check driver version compatibility

3. **Schema Creation Failed**
   - Ensure database exists
   - Check user permissions
   - Verify MySQL version compatibility

### Debug Mode

Add debug logging by modifying the connection string:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/courier_service?useSSL=false&allowPublicKeyRetrieval=true";
```

## Future Enhancements

- Connection pooling for better performance
- Transaction management
- Prepared statement caching
- Connection timeout handling
- Retry logic for failed operations 