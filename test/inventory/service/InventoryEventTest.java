package inventory.service;

import inventory.model.Product;
import inventory.util.CSVHandler;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryEventFeatureTest {

    private static final String TEST_PRODUCTS_FILE = "src/inventory/data/test_event_products.csv";
    private static final String EVENTS_FILE = CSVHandler.getDataPath() + "inventory_events.csv";
    private static final String EVENTS_BACKUP_FILE = CSVHandler.getDataPath() + "inventory_events_backup.csv";

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() throws IOException {
        backupEventsFile();
        resetEventsFile();
        createTestProductsFile();
        inventoryService = new InventoryService(TEST_PRODUCTS_FILE);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(TEST_PRODUCTS_FILE));
        restoreEventsFile();
    }

    @Test
    void testRecordDamagedEventSuccessfully() {
        Product before = inventoryService.getProductById("1");
        assertNotNull(before);
        assertEquals(10, before.getQuantity());

        boolean result = inventoryService.recordProductEvent("1", "DAMAGED", 3, "Box torn");

        assertTrue(result);

        Product after = inventoryService.getProductById("1");
        assertEquals(7, after.getQuantity());

        List<String> lines = readAllEventLines();
        assertEquals(2, lines.size());
        assertTrue(lines.get(1).contains("1"));
        assertTrue(lines.get(1).contains("DAMAGED"));
        assertTrue(lines.get(1).contains("3"));
        assertTrue(lines.get(1).contains("Box torn"));
    }

    @Test
    void testRecordReturnedEventSuccessfully() {
        boolean result = inventoryService.recordProductEvent("1", "RETURNED", 2, "Customer return");

        assertTrue(result);
        assertEquals(8, inventoryService.getProductById("1").getQuantity());

        List<String> lines = readAllEventLines();
        assertEquals(2, lines.size());
        assertTrue(lines.get(1).contains("RETURNED"));
    }

    @Test
    void testRecordExpiredEventSuccessfully() {
        boolean result = inventoryService.recordProductEvent("1", "EXPIRED", 4, "Expired on shelf");

        assertTrue(result);
        assertEquals(6, inventoryService.getProductById("1").getQuantity());

        List<String> lines = readAllEventLines();
        assertEquals(2, lines.size());
        assertTrue(lines.get(1).contains("EXPIRED"));
    }

    @Test
    void testRecordProductEventFailsForInvalidType() {
        boolean result = inventoryService.recordProductEvent("1", "LOST", 2, "Invalid type");

        assertFalse(result);
        assertEquals(10, inventoryService.getProductById("1").getQuantity());

        List<String> lines = readAllEventLines();
        assertEquals(1, lines.size());
    }

    @Test
    void testRecordProductEventFailsForInsufficientStock() {
        boolean result = inventoryService.recordProductEvent("1", "DAMAGED", 50, "Too many");

        assertFalse(result);
        assertEquals(10, inventoryService.getProductById("1").getQuantity());

        List<String> lines = readAllEventLines();
        assertEquals(1, lines.size());
    }

    @Test
    void testRecordProductEventFailsForMissingProduct() {
        boolean result = inventoryService.recordProductEvent("999", "DAMAGED", 1, "Not found");

        assertFalse(result);

        List<String> lines = readAllEventLines();
        assertEquals(1, lines.size());
    }

    @Test
    void testRecordProductEventFailsForZeroOrNegativeQuantity() {
        assertFalse(inventoryService.recordProductEvent("1", "DAMAGED", 0, "Zero"));
        assertFalse(inventoryService.recordProductEvent("1", "DAMAGED", -2, "Negative"));

        assertEquals(10, inventoryService.getProductById("1").getQuantity());

        List<String> lines = readAllEventLines();
        assertEquals(1, lines.size());
    }

    private void createTestProductsFile() throws IOException {
        List<String> lines = List.of(
                "id,barcode,name,brand,price,quantity,supplier,storageCondition",
                "1,111111,Milk,Sealtest,4.99,10,DairyFarm,Cold",
                "2,222222,Bread,Wonder,2.49,20,BakeryCo,RoomTemp"
        );
        Files.write(Paths.get(TEST_PRODUCTS_FILE), lines);
    }

    private void resetEventsFile() throws IOException {
        List<String> lines = List.of(
                "timestamp,productId,eventType,quantity,notes"
        );
        Files.write(Paths.get(EVENTS_FILE), lines);
    }

    private List<String> readAllEventLines() {
        try {
            return Files.readAllLines(Paths.get(EVENTS_FILE));
        } catch (IOException e) {
            fail("Could not read inventory_events.csv");
            return List.of();
        }
    }

    private void backupEventsFile() throws IOException {
        Path eventsPath = Paths.get(EVENTS_FILE);
        Path backupPath = Paths.get(EVENTS_BACKUP_FILE);

        if (Files.exists(eventsPath)) {
            Files.copy(eventsPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void restoreEventsFile() throws IOException {
        Path eventsPath = Paths.get(EVENTS_FILE);
        Path backupPath = Paths.get(EVENTS_BACKUP_FILE);

        if (Files.exists(backupPath)) {
            Files.copy(backupPath, eventsPath, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(backupPath);
        } else {
            Files.deleteIfExists(eventsPath);
        }
    }
}