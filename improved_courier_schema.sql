-- Improved Courier Service Database Schema
-- Handles package reassignment between shipments

-- 1. CUSTOMER table
CREATE TABLE customer (
    customer_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50) NOT NULL
);

-- 2. LOCATION table (Hierarchical structure)
CREATE TABLE location (
    location_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_location_id BIGINT NULL,
    pinCode VARCHAR(20) NOT NULL,
    FOREIGN KEY (parent_location_id) REFERENCES location(location_id)
);

-- 3. AGENT table
CREATE TABLE agent (
    agent_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contacts VARCHAR(100) NOT NULL
);

-- 4. DRIVER table
CREATE TABLE driver (
    driver_id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) UNIQUE NOT NULL,
    contact VARCHAR(50) NOT NULL,
    `limit` BIGINT DEFAULT 5
);

-- 5. SHIPMENTS table (Core entity)
CREATE TABLE shipments (
    shipment_id BIGINT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    recipient_id BIGINT NOT NULL,
    origin_id BIGINT NOT NULL,
    destination_id BIGINT NOT NULL,
    status ENUM('pending', 'in_transit', 'delivered', 'returned') DEFAULT 'pending',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    estimated_delivery_time DATETIME,
    actual_delivery DATETIME NULL,
    FOREIGN KEY (sender_id) REFERENCES customer(customer_id),
    FOREIGN KEY (recipient_id) REFERENCES customer(customer_id),
    FOREIGN KEY (origin_id) REFERENCES location(location_id),
    FOREIGN KEY (destination_id) REFERENCES location(location_id)
);

-- 6. PACKAGE table (Independent entity - can be reassigned)
CREATE TABLE package (
    package_id BIGINT PRIMARY KEY,
    weight DECIMAL(8,2) NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 7. PACKAGE_SHIPMENT_ASSIGNMENT table (Many-to-many relationship)
CREATE TABLE package_shipment_assignment (
    assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    shipment_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    removed_at TIMESTAMP NULL,
    assigned_by_agent_id BIGINT,
    removal_reason ENUM('reassigned', 'damaged', 'lost', 'returned', 'other') NULL,
    notes TEXT,
    FOREIGN KEY (package_id) REFERENCES package(package_id),
    FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id),
    FOREIGN KEY (assigned_by_agent_id) REFERENCES agent(agent_id),
    UNIQUE KEY unique_active_assignment (package_id, shipment_id, removed_at)
);

-- 8. DRIVER_SHIPMENT_ASSIGNMENT table
CREATE TABLE driver_shipment_assignment (
    assignment_id BIGINT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    shipment_id BIGINT NOT NULL,
    start_location_id BIGINT NOT NULL,
    end_location_id BIGINT NOT NULL,
    delivered BOOLEAN DEFAULT FALSE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_pickup_time DATETIME,
    actual_pickup_time DATETIME NULL,
    estimated_delivery_time DATETIME,
    actual_delivery_time DATETIME NULL,
    FOREIGN KEY (driver_id) REFERENCES driver(driver_id),
    FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id),
    FOREIGN KEY (start_location_id) REFERENCES location(location_id),
    FOREIGN KEY (end_location_id) REFERENCES location(location_id),
    UNIQUE KEY unique_active_assignment (driver_id, shipment_id)
);

-- 9. STATUS_LOGS table
CREATE TABLE status_logs (
    log_id BIGINT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    location_id BIGINT,
    agent_id BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'in_transit', 'delivered', 'returned') NOT NULL,
    notes TEXT,
    FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES location(location_id),
    FOREIGN KEY (agent_id) REFERENCES agent(agent_id)
);

-- 10. PACKAGE_MOVEMENT_LOG table (Audit trail for package movements)
CREATE TABLE package_movement_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    from_shipment_id BIGINT NULL,
    to_shipment_id BIGINT NULL,
    moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    moved_by_agent_id BIGINT,
    movement_reason ENUM('reassignment', 'consolidation', 'split_shipment', 'damage', 'customer_request', 'other'),
    notes TEXT,
    FOREIGN KEY (package_id) REFERENCES package(package_id),
    FOREIGN KEY (from_shipment_id) REFERENCES shipments(shipment_id),
    FOREIGN KEY (to_shipment_id) REFERENCES shipments(shipment_id),
    FOREIGN KEY (moved_by_agent_id) REFERENCES agent(agent_id)
);

-- Indexes
CREATE INDEX idx_package_shipment_assignment_package ON package_shipment_assignment(package_id);
CREATE INDEX idx_package_shipment_assignment_shipment ON package_shipment_assignment(shipment_id);
CREATE INDEX idx_package_shipment_assignment_active ON package_shipment_assignment(package_id, removed_at);
CREATE INDEX idx_package_movement_log_package ON package_movement_log(package_id);
CREATE INDEX idx_package_movement_log_timestamp ON package_movement_log(moved_at);

