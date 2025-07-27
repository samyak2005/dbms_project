import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CourierServiceDB {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/courier_service";
    private static final String USER = "root";
    private static final String PASS = "password";
    
    private Connection connection;
    
    public CourierServiceDB() {
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Database connected successfully!");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }
    
    // Create database schema
    public void createSchema() {
        try {
            Statement stmt = connection.createStatement();
            
            // Create tables
            String[] createTables = {
                // Customer table
                "CREATE TABLE IF NOT EXISTS customer (" +
                "customer_id BIGINT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "contact VARCHAR(50) NOT NULL" +
                ")",
                
                // Location table
                "CREATE TABLE IF NOT EXISTS location (" +
                "location_id BIGINT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "parent_location_id BIGINT NULL," +
                "pinCode VARCHAR(20) NOT NULL," +
                "FOREIGN KEY (parent_location_id) REFERENCES location(location_id)" +
                ")",
                
                // Agent table
                "CREATE TABLE IF NOT EXISTS agent (" +
                "agent_id BIGINT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "contacts VARCHAR(100) NOT NULL" +
                ")",
                
                // Driver table
                "CREATE TABLE IF NOT EXISTS driver (" +
                "driver_id BIGINT PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "license_number VARCHAR(50) UNIQUE NOT NULL," +
                "contact VARCHAR(50) NOT NULL," +
                "`limit` BIGINT DEFAULT 5" +
                ")",
                
                // Shipments table
                "CREATE TABLE IF NOT EXISTS shipments (" +
                "shipment_id BIGINT PRIMARY KEY," +
                "sender_id BIGINT NOT NULL," +
                "recipient_id BIGINT NOT NULL," +
                "origin_id BIGINT NOT NULL," +
                "destination_id BIGINT NOT NULL," +
                "status ENUM('pending', 'in_transit', 'delivered', 'returned') DEFAULT 'pending'," +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                "estimated_delivery_time DATETIME," +
                "actual_delivery DATETIME NULL," +
                "FOREIGN KEY (sender_id) REFERENCES customer(customer_id)," +
                "FOREIGN KEY (recipient_id) REFERENCES customer(customer_id)," +
                "FOREIGN KEY (origin_id) REFERENCES location(location_id)," +
                "FOREIGN KEY (destination_id) REFERENCES location(location_id)" +
                ")",
                
                // Package table
                "CREATE TABLE IF NOT EXISTS package (" +
                "package_id BIGINT PRIMARY KEY," +
                "weight DECIMAL(8,2) NOT NULL," +
                "description VARCHAR(500) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "is_active BOOLEAN DEFAULT TRUE" +
                ")",
                
                // Package shipment assignment table
                "CREATE TABLE IF NOT EXISTS package_shipment_assignment (" +
                "assignment_id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "package_id BIGINT NOT NULL," +
                "shipment_id BIGINT NOT NULL," +
                "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "removed_at TIMESTAMP NULL," +
                "assigned_by_agent_id BIGINT," +
                "removal_reason ENUM('reassigned', 'damaged', 'lost', 'returned', 'other') NULL," +
                "notes TEXT," +
                "FOREIGN KEY (package_id) REFERENCES package(package_id)," +
                "FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id)," +
                "FOREIGN KEY (assigned_by_agent_id) REFERENCES agent(agent_id)" +
                ")",
                
                // Driver shipment assignment table
                "CREATE TABLE IF NOT EXISTS driver_shipment_assignment (" +
                "assignment_id BIGINT PRIMARY KEY," +
                "driver_id BIGINT NOT NULL," +
                "shipment_id BIGINT NOT NULL," +
                "start_location_id BIGINT NOT NULL," +
                "end_location_id BIGINT NOT NULL," +
                "delivered BOOLEAN DEFAULT FALSE," +
                "assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "estimated_pickup_time DATETIME," +
                "actual_pickup_time DATETIME NULL," +
                "estimated_delivery_time DATETIME," +
                "actual_delivery_time DATETIME NULL," +
                "FOREIGN KEY (driver_id) REFERENCES driver(driver_id)," +
                "FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id)," +
                "FOREIGN KEY (start_location_id) REFERENCES location(location_id)," +
                "FOREIGN KEY (end_location_id) REFERENCES location(location_id)" +
                ")",
                
                // Status logs table
                "CREATE TABLE IF NOT EXISTS status_logs (" +
                "log_id BIGINT PRIMARY KEY," +
                "shipment_id BIGINT NOT NULL," +
                "location_id BIGINT," +
                "agent_id BIGINT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "status ENUM('pending', 'in_transit', 'delivered', 'returned') NOT NULL," +
                "notes TEXT," +
                "FOREIGN KEY (shipment_id) REFERENCES shipments(shipment_id) ON DELETE CASCADE," +
                "FOREIGN KEY (location_id) REFERENCES location(location_id)," +
                "FOREIGN KEY (agent_id) REFERENCES agent(agent_id)" +
                ")",
                
                // Package movement log table
                "CREATE TABLE IF NOT EXISTS package_movement_log (" +
                "log_id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "package_id BIGINT NOT NULL," +
                "from_shipment_id BIGINT NULL," +
                "to_shipment_id BIGINT NULL," +
                "moved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "moved_by_agent_id BIGINT," +
                "movement_reason ENUM('reassignment', 'consolidation', 'split_shipment', 'damage', 'customer_request', 'other')," +
                "notes TEXT," +
                "FOREIGN KEY (package_id) REFERENCES package(package_id)," +
                "FOREIGN KEY (from_shipment_id) REFERENCES shipments(shipment_id)," +
                "FOREIGN KEY (to_shipment_id) REFERENCES shipments(shipment_id)," +
                "FOREIGN KEY (moved_by_agent_id) REFERENCES agent(agent_id)" +
                ")"
            };
            
            for (String sql : createTables) {
                stmt.execute(sql);
            }
            
            // Create indexes
            String[] createIndexes = {
                "CREATE INDEX IF NOT EXISTS idx_package_shipment_assignment_package ON package_shipment_assignment(package_id)",
                "CREATE INDEX IF NOT EXISTS idx_package_shipment_assignment_shipment ON package_shipment_assignment(shipment_id)",
                "CREATE INDEX IF NOT EXISTS idx_package_shipment_assignment_active ON package_shipment_assignment(package_id, removed_at)",
                "CREATE INDEX IF NOT EXISTS idx_package_movement_log_package ON package_movement_log(package_id)",
                "CREATE INDEX IF NOT EXISTS idx_package_movement_log_timestamp ON package_movement_log(moved_at)",
                "CREATE INDEX IF NOT EXISTS idx_shipments_status ON shipments(status)",
                "CREATE INDEX IF NOT EXISTS idx_shipments_estimated_delivery ON shipments(estimated_delivery_time)",
                "CREATE INDEX IF NOT EXISTS idx_driver_assignment_driver_delivered ON driver_shipment_assignment(driver_id, delivered)"
            };
            
            for (String sql : createIndexes) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    // Index might already exist, continue
                }
            }
            
            System.out.println("Database schema created successfully!");
            
        } catch (SQLException e) {
            System.err.println("Error creating schema: " + e.getMessage());
        }
    }
    
    // Function 1: Create shipment with sender, recipient, origin, destination
    public boolean createShipment(long shipmentId, long senderId, long recipientId, 
                                 long originId, long destinationId, LocalDateTime estimatedDelivery) {
        try {
            String sql = "INSERT INTO shipments (shipment_id, sender_id, recipient_id, origin_id, destination_id, estimated_delivery_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, shipmentId);
            pstmt.setLong(2, senderId);
            pstmt.setLong(3, recipientId);
            pstmt.setLong(4, originId);
            pstmt.setLong(5, destinationId);
            pstmt.setTimestamp(6, Timestamp.valueOf(estimatedDelivery));
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error creating shipment: " + e.getMessage());
            return false;
        }
    }
    
    // Function 2: Add package to shipment
    public boolean addPackageToShipment(long packageId, double weight, String description, 
                                       long shipmentId, long agentId) {
        try {
            // First create the package
            String packageSql = "INSERT INTO package (package_id, weight, description) VALUES (?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(packageSql);
            pstmt.setLong(1, packageId);
            pstmt.setDouble(2, weight);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            
            // Then assign it to shipment
            String assignmentSql = "INSERT INTO package_shipment_assignment (package_id, shipment_id, assigned_by_agent_id) " +
                                 "VALUES (?, ?, ?)";
            pstmt = connection.prepareStatement(assignmentSql);
            pstmt.setLong(1, packageId);
            pstmt.setLong(2, shipmentId);
            pstmt.setLong(3, agentId);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error adding package to shipment: " + e.getMessage());
            return false;
        }
    }
    
    // Function 3: Update shipment status
    public boolean updateShipmentStatus(long shipmentId, String status, long agentId, String notes) {
        try {
            // Update shipment status
            String updateSql = "UPDATE shipments SET status = ? WHERE shipment_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(updateSql);
            pstmt.setString(1, status);
            pstmt.setLong(2, shipmentId);
            pstmt.executeUpdate();
            
            // Log the status change
            String logSql = "INSERT INTO status_logs (log_id, shipment_id, agent_id, status, notes) " +
                           "VALUES (?, ?, ?, ?, ?)";
            pstmt = connection.prepareStatement(logSql);
            pstmt.setLong(1, System.currentTimeMillis()); // Simple ID generation
            pstmt.setLong(2, shipmentId);
            pstmt.setLong(3, agentId);
            pstmt.setString(4, status);
            pstmt.setString(5, notes);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error updating shipment status: " + e.getMessage());
            return false;
        }
    }
    
    // Function 4: Register driver
    public boolean registerDriver(long driverId, String name, String licenseNumber, String contact, int limit) {
        try {
            String sql = "INSERT INTO driver (driver_id, name, license_number, contact, `limit`) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, driverId);
            pstmt.setString(2, name);
            pstmt.setString(3, licenseNumber);
            pstmt.setString(4, contact);
            pstmt.setInt(5, limit);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error registering driver: " + e.getMessage());
            return false;
        }
    }
    
    // Function 5: Assign shipment to driver
    public boolean assignShipmentToDriver(long driverId, long shipmentId, long startLocationId, 
                                        long endLocationId, LocalDateTime estimatedPickup, 
                                        LocalDateTime estimatedDelivery) {
        try {
            // Check driver capacity
            String capacitySql = "SELECT COUNT(*) FROM driver_shipment_assignment dsa " +
                               "JOIN driver d ON dsa.driver_id = d.driver_id " +
                               "WHERE dsa.driver_id = ? AND dsa.delivered = FALSE";
            PreparedStatement pstmt = connection.prepareStatement(capacitySql);
            pstmt.setLong(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int activeAssignments = rs.getInt(1);
                String limitSql = "SELECT `limit` FROM driver WHERE driver_id = ?";
                pstmt = connection.prepareStatement(limitSql);
                pstmt.setLong(1, driverId);
                rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    int limit = rs.getInt(1);
                    if (activeAssignments >= limit) {
                        System.err.println("Driver has reached maximum capacity");
                        return false;
                    }
                }
            }
            
            // Assign shipment to driver
            String assignSql = "INSERT INTO driver_shipment_assignment (assignment_id, driver_id, shipment_id, " +
                             "start_location_id, end_location_id, estimated_pickup_time, estimated_delivery_time) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmt = connection.prepareStatement(assignSql);
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setLong(2, driverId);
            pstmt.setLong(3, shipmentId);
            pstmt.setLong(4, startLocationId);
            pstmt.setLong(5, endLocationId);
            pstmt.setTimestamp(6, Timestamp.valueOf(estimatedPickup));
            pstmt.setTimestamp(7, Timestamp.valueOf(estimatedDelivery));
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error assigning shipment to driver: " + e.getMessage());
            return false;
        }
    }
    
    // Function 6: Move package between shipments
    public boolean movePackageBetweenShipments(long packageId, long fromShipmentId, long toShipmentId, 
                                             long agentId, String reason, String notes) {
        try {
            // Mark current assignment as removed
            String removeSql = "UPDATE package_shipment_assignment SET removed_at = NOW(), removal_reason = ? " +
                             "WHERE package_id = ? AND shipment_id = ? AND removed_at IS NULL";
            PreparedStatement pstmt = connection.prepareStatement(removeSql);
            pstmt.setString(1, reason);
            pstmt.setLong(2, packageId);
            pstmt.setLong(3, fromShipmentId);
            pstmt.executeUpdate();
            
            // Create new assignment
            String assignSql = "INSERT INTO package_shipment_assignment (package_id, shipment_id, assigned_by_agent_id) " +
                             "VALUES (?, ?, ?)";
            pstmt = connection.prepareStatement(assignSql);
            pstmt.setLong(1, packageId);
            pstmt.setLong(2, toShipmentId);
            pstmt.setLong(3, agentId);
            
            // Log the movement
            String logSql = "INSERT INTO package_movement_log (package_id, from_shipment_id, to_shipment_id, " +
                           "moved_by_agent_id, movement_reason, notes) VALUES (?, ?, ?, ?, ?, ?)";
            pstmt = connection.prepareStatement(logSql);
            pstmt.setLong(1, packageId);
            pstmt.setLong(2, fromShipmentId);
            pstmt.setLong(3, toShipmentId);
            pstmt.setLong(4, agentId);
            pstmt.setString(5, reason);
            pstmt.setString(6, notes);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("Error moving package: " + e.getMessage());
            return false;
        }
    }
    
    // Function 7: Get current status and location log for shipment
    public List<Map<String, Object>> getShipmentStatusAndLog(long shipmentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String sql = "SELECT s.shipment_id, s.status as current_status, s.estimated_delivery_time, " +
                        "s.actual_delivery, sl.status as log_status, sl.timestamp as log_timestamp, " +
                        "l.name as location_name, l.pinCode, a.name as agent_name, sl.notes " +
                        "FROM shipments s " +
                        "LEFT JOIN status_logs sl ON s.shipment_id = sl.shipment_id " +
                        "LEFT JOIN location l ON sl.location_id = l.location_id " +
                        "LEFT JOIN agent a ON sl.agent_id = a.agent_id " +
                        "WHERE s.shipment_id = ? " +
                        "ORDER BY sl.timestamp DESC";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, shipmentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("shipment_id", rs.getLong("shipment_id"));
                row.put("current_status", rs.getString("current_status"));
                row.put("estimated_delivery_time", rs.getTimestamp("estimated_delivery_time"));
                row.put("actual_delivery", rs.getTimestamp("actual_delivery"));
                row.put("log_status", rs.getString("log_status"));
                row.put("log_timestamp", rs.getTimestamp("log_timestamp"));
                row.put("location_name", rs.getString("location_name"));
                row.put("pinCode", rs.getString("pinCode"));
                row.put("agent_name", rs.getString("agent_name"));
                row.put("notes", rs.getString("notes"));
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error getting shipment status: " + e.getMessage());
        }
        return result;
    }
    
    // Function 8: Get pending shipments for driver
    public List<Map<String, Object>> getPendingShipmentsForDriver(long driverId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String sql = "SELECT s.shipment_id, c1.name as sender_name, c2.name as recipient_name, " +
                        "l1.name as origin_location, l2.name as destination_location, " +
                        "s.estimated_delivery_time, dsa.assigned_at, dsa.estimated_pickup_time " +
                        "FROM shipments s " +
                        "JOIN driver_shipment_assignment dsa ON s.shipment_id = dsa.shipment_id " +
                        "JOIN customer c1 ON s.sender_id = c1.customer_id " +
                        "JOIN customer c2 ON s.recipient_id = c2.customer_id " +
                        "JOIN location l1 ON s.origin_id = l1.location_id " +
                        "JOIN location l2 ON s.destination_id = l2.location_id " +
                        "WHERE dsa.driver_id = ? AND s.status = 'pending' AND dsa.delivered = FALSE " +
                        "ORDER BY dsa.assigned_at";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setLong(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("shipment_id", rs.getLong("shipment_id"));
                row.put("sender_name", rs.getString("sender_name"));
                row.put("recipient_name", rs.getString("recipient_name"));
                row.put("origin_location", rs.getString("origin_location"));
                row.put("destination_location", rs.getString("destination_location"));
                row.put("estimated_delivery_time", rs.getTimestamp("estimated_delivery_time"));
                row.put("assigned_at", rs.getTimestamp("assigned_at"));
                row.put("estimated_pickup_time", rs.getTimestamp("estimated_pickup_time"));
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error getting pending shipments: " + e.getMessage());
        }
        return result;
    }
    
    // Function 9: Get delayed shipments
    public List<Map<String, Object>> getDelayedShipments() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String sql = "SELECT s.shipment_id, c1.name as sender_name, c2.name as recipient_name, " +
                        "s.estimated_delivery_time, s.actual_delivery, " +
                        "TIMESTAMPDIFF(HOUR, s.estimated_delivery_time, NOW()) as delay_hours, " +
                        "d.name as driver_name, d.contact as driver_contact " +
                        "FROM shipments s " +
                        "JOIN customer c1 ON s.sender_id = c1.customer_id " +
                        "JOIN customer c2 ON s.recipient_id = c2.customer_id " +
                        "LEFT JOIN driver_shipment_assignment dsa ON s.shipment_id = dsa.shipment_id " +
                        "LEFT JOIN driver d ON dsa.driver_id = d.driver_id " +
                        "WHERE s.estimated_delivery_time < NOW() " +
                        "AND s.status IN ('pending', 'in_transit') " +
                        "AND (s.actual_delivery IS NULL OR s.actual_delivery > s.estimated_delivery_time) " +
                        "ORDER BY delay_hours DESC";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("shipment_id", rs.getLong("shipment_id"));
                row.put("sender_name", rs.getString("sender_name"));
                row.put("recipient_name", rs.getString("recipient_name"));
                row.put("estimated_delivery_time", rs.getTimestamp("estimated_delivery_time"));
                row.put("actual_delivery", rs.getTimestamp("actual_delivery"));
                row.put("delay_hours", rs.getLong("delay_hours"));
                row.put("driver_name", rs.getString("driver_name"));
                row.put("driver_contact", rs.getString("driver_contact"));
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error getting delayed shipments: " + e.getMessage());
        }
        return result;
    }
    
    // Function 10: Get daily shipment volume by origin
    public List<Map<String, Object>> getDailyShipmentVolume() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String sql = "SELECT DATE(s.created_time) as shipment_date, l.name as origin_location, " +
                        "l.pinCode as origin_pincode, COUNT(s.shipment_id) as total_shipments, " +
                        "COUNT(CASE WHEN s.status = 'delivered' THEN 1 END) as delivered_shipments, " +
                        "COUNT(CASE WHEN s.status = 'in_transit' THEN 1 END) as in_transit_shipments, " +
                        "COUNT(CASE WHEN s.status = 'pending' THEN 1 END) as pending_shipments, " +
                        "SUM(p.weight) as total_weight_kg " +
                        "FROM shipments s " +
                        "JOIN location l ON s.origin_id = l.location_id " +
                        "LEFT JOIN package_shipment_assignment psa ON s.shipment_id = psa.shipment_id " +
                        "LEFT JOIN package p ON psa.package_id = p.package_id " +
                        "WHERE s.created_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                        "AND (psa.removed_at IS NULL OR psa.removed_at IS NULL) " +
                        "GROUP BY DATE(s.created_time), l.location_id, l.name, l.pinCode " +
                        "ORDER BY shipment_date DESC, total_shipments DESC";
            
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("shipment_date", rs.getDate("shipment_date"));
                row.put("origin_location", rs.getString("origin_location"));
                row.put("origin_pincode", rs.getString("origin_pincode"));
                row.put("total_shipments", rs.getLong("total_shipments"));
                row.put("delivered_shipments", rs.getLong("delivered_shipments"));
                row.put("in_transit_shipments", rs.getLong("in_transit_shipments"));
                row.put("pending_shipments", rs.getLong("pending_shipments"));
                row.put("total_weight_kg", rs.getDouble("total_weight_kg"));
                result.add(row);
            }
        } catch (SQLException e) {
            System.err.println("Error getting daily shipment volume: " + e.getMessage());
        }
        return result;
    }
    
    // Get database connection (for demo purposes)
    public Connection getConnection() {
        return connection;
    }
    
    // Close database connection
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
} 