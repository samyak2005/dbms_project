import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CourierServiceDemo {
    public static void main(String[] args) {
        CourierServiceDB db = new CourierServiceDB();
        
        try {
            // Create database schema
            System.out.println("=== Creating Database Schema ===");
            db.createSchema();
            
            // Insert sample data
            System.out.println("\n=== Inserting Sample Data ===");
            insertSampleData(db);
            
            // Test all functions
            System.out.println("\n=== Testing Core Functions ===");
            testCoreFunctions(db);
            
            // Test query functions
            System.out.println("\n=== Testing Query Functions ===");
            testQueryFunctions(db);
            
        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
    
    private static void insertSampleData(CourierServiceDB db) {
        try {
            // Insert customers
            System.out.println("Inserting customers...");
            insertCustomer(db, 1, "John Doe", "john@example.com");
            insertCustomer(db, 2, "Jane Smith", "jane@example.com");
            insertCustomer(db, 3, "Bob Wilson", "bob@example.com");
            
            // Insert locations
            System.out.println("Inserting locations...");
            insertLocation(db, 1, "Main Hub", null, "10001");
            insertLocation(db, 2, "Sub Hub A", 1L, "10002");
            insertLocation(db, 3, "Sub Hub B", 1L, "10003");
            
            // Insert agents
            System.out.println("Inserting agents...");
            insertAgent(db, 1, "Agent Alice", "alice@courier.com");
            insertAgent(db, 2, "Agent Bob", "bob@courier.com");
            
            // Insert drivers
            System.out.println("Inserting drivers...");
            db.registerDriver(1, "Driver Dave", "DL123456", "dave@courier.com", 5);
            db.registerDriver(2, "Driver Emma", "DL789012", "emma@courier.com", 3);
            
            System.out.println("Sample data inserted successfully!");
            
        } catch (Exception e) {
            System.err.println("Error inserting sample data: " + e.getMessage());
        }
    }
    
    private static void insertCustomer(CourierServiceDB db, long id, String name, String contact) {
        try {
            String sql = "INSERT INTO customer (customer_id, name, contact) VALUES (?, ?, ?)";
            java.sql.PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setLong(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, contact);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error inserting customer: " + e.getMessage());
        }
    }
    
    private static void insertLocation(CourierServiceDB db, long id, String name, Long parentId, String pinCode) {
        try {
            String sql = "INSERT INTO location (location_id, name, parent_location_id, pinCode) VALUES (?, ?, ?, ?)";
            java.sql.PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setLong(1, id);
            pstmt.setString(2, name);
            if (parentId != null) {
                pstmt.setLong(3, parentId);
            } else {
                pstmt.setNull(3, java.sql.Types.BIGINT);
            }
            pstmt.setString(4, pinCode);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error inserting location: " + e.getMessage());
        }
    }
    
    private static void insertAgent(CourierServiceDB db, long id, String name, String contacts) {
        try {
            String sql = "INSERT INTO agent (agent_id, name, contacts) VALUES (?, ?, ?)";
            java.sql.PreparedStatement pstmt = db.getConnection().prepareStatement(sql);
            pstmt.setLong(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, contacts);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Error inserting agent: " + e.getMessage());
        }
    }
    
    private static void testCoreFunctions(CourierServiceDB db) {
        try {
            // Test 1: Create shipment
            System.out.println("1. Creating shipment...");
            LocalDateTime estimatedDelivery = LocalDateTime.now().plusDays(2);
            boolean shipmentCreated = db.createShipment(1001, 1, 2, 1, 2, estimatedDelivery);
            System.out.println("Shipment created: " + shipmentCreated);
            
            // Test 2: Add package to shipment
            System.out.println("2. Adding package to shipment...");
            boolean packageAdded = db.addPackageToShipment(2001, 2.5, "Electronics package", 1001, 1);
            System.out.println("Package added: " + packageAdded);
            
            // Test 3: Update shipment status
            System.out.println("3. Updating shipment status...");
            boolean statusUpdated = db.updateShipmentStatus(1001, "in_transit", 1, "Package picked up");
            System.out.println("Status updated: " + statusUpdated);
            
            // Test 4: Assign shipment to driver
            System.out.println("4. Assigning shipment to driver...");
            LocalDateTime pickupTime = LocalDateTime.now().plusHours(1);
            LocalDateTime deliveryTime = LocalDateTime.now().plusDays(1);
            boolean assigned = db.assignShipmentToDriver(1, 1001, 1, 2, pickupTime, deliveryTime);
            System.out.println("Shipment assigned: " + assigned);
            
            // Test 5: Move package between shipments
            System.out.println("5. Moving package between shipments...");
            // First create another shipment
            db.createShipment(1002, 2, 3, 1, 3, LocalDateTime.now().plusDays(3));
            boolean moved = db.movePackageBetweenShipments(2001, 1001, 1002, 1, "reassignment", "Customer request");
            System.out.println("Package moved: " + moved);
            
        } catch (Exception e) {
            System.err.println("Error testing core functions: " + e.getMessage());
        }
    }
    
    private static void testQueryFunctions(CourierServiceDB db) {
        try {
            // Test 6: Get shipment status and log
            System.out.println("6. Getting shipment status and log...");
            List<Map<String, Object>> statusLog = db.getShipmentStatusAndLog(1001);
            System.out.println("Status log entries: " + statusLog.size());
            for (Map<String, Object> entry : statusLog) {
                System.out.println("  - " + entry.get("current_status") + " at " + entry.get("log_timestamp"));
            }
            
            // Test 7: Get pending shipments for driver
            System.out.println("7. Getting pending shipments for driver...");
            List<Map<String, Object>> pendingShipments = db.getPendingShipmentsForDriver(1);
            System.out.println("Pending shipments: " + pendingShipments.size());
            for (Map<String, Object> shipment : pendingShipments) {
                System.out.println("  - Shipment " + shipment.get("shipment_id") + 
                                 " from " + shipment.get("sender_name") + 
                                 " to " + shipment.get("recipient_name"));
            }
            
            // Test 8: Get delayed shipments
            System.out.println("8. Getting delayed shipments...");
            List<Map<String, Object>> delayedShipments = db.getDelayedShipments();
            System.out.println("Delayed shipments: " + delayedShipments.size());
            for (Map<String, Object> shipment : delayedShipments) {
                System.out.println("  - Shipment " + shipment.get("shipment_id") + 
                                 " delayed by " + shipment.get("delay_hours") + " hours");
            }
            
            // Test 9: Get daily shipment volume
            System.out.println("9. Getting daily shipment volume...");
            List<Map<String, Object>> dailyVolume = db.getDailyShipmentVolume();
            System.out.println("Daily volume entries: " + dailyVolume.size());
            for (Map<String, Object> volume : dailyVolume) {
                System.out.println("  - " + volume.get("origin_location") + 
                                 ": " + volume.get("total_shipments") + " shipments");
            }
            
        } catch (Exception e) {
            System.err.println("Error testing query functions: " + e.getMessage());
        }
    }
} 