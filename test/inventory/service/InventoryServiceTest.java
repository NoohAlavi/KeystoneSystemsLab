package inventory.service;

import inventory.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService();
    }

    @AfterEach
    void tearDown() {
        // Remove all test products added during tests
        String[] testIds = {
                "TESTPRODUCT00000001",
                "TESTPRODUCT00000002",
                "TESTPRODUCT00000003",
                "TESTPRODUCT00000004",
                "TESTPRODUCT00000005",
                "TESTPRODUCT00000006",
                "TESTPRODUCT00000007",
                "TESTPRODUCT00000008"
        };
        for (String id : testIds) {
            Product p = service.getProductById(id);
            if (p != null) {
                try {
                    java.lang.reflect.Field byId = InventoryService.class.getDeclaredField("productsById");
                    java.lang.reflect.Field byBarcode = InventoryService.class.getDeclaredField("productsByBarcode");
                    byId.setAccessible(true);
                    byBarcode.setAccessible(true);
                    ((Map<?, ?>)byId.get(service)).remove(p.getId());
                    ((Map<?, ?>)byBarcode.get(service)).remove(p.getBarcode());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    void testAddProductSuccess() {
        Product p = new Product("TESTPRODUCT00000001", "TESTBARCODE00000001",
                "TestProduct", "TestBrand", 10.0, 5, "TestSupplier", "Room");
        boolean result = service.addProduct(p);

        assertTrue(result);
        assertEquals("TESTPRODUCT00000001", service.getProductById("TESTPRODUCT00000001").getId());
    }

    @Test
    void testAddProductDuplicateId() {
        Product p1 = new Product("TESTPRODUCT00000002", "TESTBARCODE00000002",
                "A", "B", 10, 5, "S", "Room");
        Product p2 = new Product("TESTPRODUCT00000002", "TESTBARCODE00000003",
                "C", "D", 12, 5, "S", "Room");

        service.addProduct(p1);
        boolean result = service.addProduct(p2);

        assertFalse(result);
    }

    @Test
    void testGetProductByIdNotFound() {
        assertNull(service.getProductById("TESTPRODUCT00009999"));
    }

    @Test
    void testUpdateProductSuccess() {
        Product p = new Product("TESTPRODUCT00000003", "TESTBARCODE00000004",
                "Old", "OldBrand", 5, 10, "S", "Cold");
        service.addProduct(p);

        boolean result = service.updateProduct("TESTPRODUCT00000003", "New", "NewBrand",
                20, "NewS", "Hot");

        assertTrue(result);
        assertEquals("New", service.getProductById("TESTPRODUCT00000003").getName());
    }

    @Test
    void testUpdateProductFail() {
        boolean result = service.updateProduct("TESTPRODUCT00009999", "X", "Y", 10, "S", "Room");

        assertFalse(result);
    }

    @Test
    void testIncreaseStock() {
        Product p = new Product("TESTPRODUCT00000004", "TESTBARCODE00000005",
                "Item", "Brand", 5, 10, "S", "Room");
        service.addProduct(p);

        service.increaseStock("TESTPRODUCT00000004", 5,"test reason");

        assertEquals(15, service.getProductById("TESTPRODUCT00000004").getQuantity());
    }

    @Test
    void testDecreaseStockSuccess() {
        Product p = new Product("TESTPRODUCT00000005", "TESTBARCODE00000006",
                "Item", "Brand", 5, 10, "S", "Room");
        service.addProduct(p);

        boolean result = service.decreaseStock("TESTPRODUCT00000005", 5);

        assertTrue(result);
        assertEquals(5, service.getProductById("TESTPRODUCT00000005").getQuantity());
    }

    @Test
    void testDecreaseStockFail() {
        Product p = new Product("TESTPRODUCT00000006", "TESTBARCODE00000007",
                "Item", "Brand", 5, 2, "S", "Room");
        service.addProduct(p);

        boolean result = service.decreaseStock("TESTPRODUCT00000006", 10);

        assertFalse(result);
    }

    @Test
    void testSearchProductsByName() {
        Product p = new Product("TESTPRODUCT00000007", "TESTBARCODE00000008",
                "Apple Juice", "Brand", 5, 10, "S", "Room");
        service.addProduct(p);

        assertFalse(service.searchProductsByName("apple").isEmpty());
    }

    @Test
    void testProductCount() {
        int before = service.getProductCount();

        Product p = new Product("TESTPRODUCT00000008", "TESTBARCODE00000009",
                "Item", "Brand", 5, 10, "S", "Room");
        service.addProduct(p);

        assertEquals(before + 1, service.getProductCount());
    }
}