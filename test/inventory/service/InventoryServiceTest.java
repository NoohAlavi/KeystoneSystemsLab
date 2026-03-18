package inventory.service;

import inventory.model.Product;
import inventory.util.CSVHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {

    private InventoryService service;
    private static final String TEST_FILE = "test_products.csv";

    @BeforeEach
    void setUp() {
        // Reset test file before each test
        CSVHandler.writeCSV(TEST_FILE, new ArrayList<>());

        service = new InventoryService(TEST_FILE);
    }

    @Test
    void testAddProductSuccess() {
        Product p = new Product("TEST1", "BAR1",
                "TestProduct", "Brand", 10.0, 5, "Supplier", "Room");

        assertTrue(service.addProduct(p));
        assertNotNull(service.getProductById("TEST1"));
    }

    @Test
    void testAddProductDuplicateId() {
        Product p1 = new Product("TEST2", "BAR2", "A", "B", 10, 5, "S", "Room");
        Product p2 = new Product("TEST2", "BAR3", "C", "D", 12, 5, "S", "Room");

        service.addProduct(p1);

        assertFalse(service.addProduct(p2));
    }

    @Test
    void testGetProductByIdNotFound() {
        assertNull(service.getProductById("DOES_NOT_EXIST"));
    }

    @Test
    void testUpdateProductSuccess() {
        Product p = new Product("TEST3", "BAR4",
                "Old", "OldBrand", 5, 10, "S", "Cold");

        service.addProduct(p);

        assertTrue(service.updateProduct("TEST3", "New", "NewBrand",
                20, "NewS", "Hot"));

        assertEquals("New", service.getProductById("TEST3").getName());
    }

    @Test
    void testUpdateProductFail() {
        assertFalse(service.updateProduct("BAD_ID", "X", "Y", 10, "S", "Room"));
    }

    @Test
    void testIncreaseStock() {
        Product p = new Product("TEST4", "BAR5",
                "Item", "Brand", 5, 10, "S", "Room");

        service.addProduct(p);
        service.increaseStock("TEST4", 5);

        assertEquals(15, service.getProductById("TEST4").getQuantity());
    }

    @Test
    void testDecreaseStockSuccess() {
        Product p = new Product("TEST5", "BAR6",
                "Item", "Brand", 5, 10, "S", "Room");

        service.addProduct(p);

        assertTrue(service.decreaseStock("TEST5", 5));
        assertEquals(5, service.getProductById("TEST5").getQuantity());
    }

    @Test
    void testDecreaseStockFail() {
        Product p = new Product("TEST6", "BAR7",
                "Item", "Brand", 5, 2, "S", "Room");

        service.addProduct(p);

        assertFalse(service.decreaseStock("TEST6", 10));
    }

    @Test
    void testSearchProductsByName() {
        Product p = new Product("TEST7", "BAR8",
                "Apple Juice", "Brand", 5, 10, "S", "Room");

        service.addProduct(p);

        assertFalse(service.searchProductsByName("apple").isEmpty());
    }

    @Test
    void testProductCount() {
        int before = service.getProductCount();

        Product p = new Product("TEST8", "BAR9",
                "Item", "Brand", 5, 10, "S", "Room");

        service.addProduct(p);

        assertEquals(before + 1, service.getProductCount());
    }
}