package inventory.service;

import inventory.model.Product;
import inventory.util.CSVHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class InventorySearchAndSortTest {

    private InventoryService service;
    private static final String TEST_FILE = CSVHandler.getDataPath() + "test_products_search_sort.csv";

    @BeforeEach
    void setUp() {
        // Reset test file before each test
        CSVHandler.writeCSV(TEST_FILE, new ArrayList<>());
        service = new InventoryService(TEST_FILE);

        // Add dummy data
        service.addProduct(new Product("10", "111", "Apple", "BrandA", 1.50, 100, "SupplierX", "Room"));
        service.addProduct(new Product("2", "222", "Banana", "BrandB", 0.99, 50, "SupplierY", "Cold"));
        service.addProduct(new Product("30", "333", "Cherry", "BrandA", 5.00, 200, "SupplierX", "Frozen"));
        service.addProduct(new Product("A1", "444", "Date", "BrandC", 10.00, 10, "SupplierZ", "Room"));
    }

    // --- SEARCH TESTS ---

    @Test
    void testSearchByExactName() {
        List<Product> results = search("Apple");
        assertEquals(1, results.size());
        assertEquals("Apple", results.get(0).getName());
    }

    @Test
    void testSearchByPartialName() {
        List<Product> results = search("an"); // Should find Banana and BrandA products? No, search logic is in UI, so we emulate it here
        // The service only has searchByName. The multi-attribute search is currently in UI.
        // We will test the SERVICE method here.
        
        List<Product> serviceResults = service.searchProductsByName("an");
        // Banana matches "an"
        // BrandA matches "an"? No, searchProductsByName checks Name only.
        
        assertTrue(serviceResults.stream().anyMatch(p -> p.getName().equals("Banana")));
    }

    @Test
    void testSearchCaseInsensitive() {
        List<Product> results = service.searchProductsByName("apple");
        assertEquals(1, results.size());
        assertEquals("Apple", results.get(0).getName());
    }

    @Test
    void testSearchNoResults() {
        List<Product> results = service.searchProductsByName("Zucchini");
        assertTrue(results.isEmpty());
    }

    // --- SORTING LOGIC TESTS ---
    // Since sorting is implemented in the UI layer (JTable Sorter), we will test the Comparator logic here.

    @Test
    void testNumericStringComparator() {
        Comparator<String> comparator = (o1, o2) -> {
            try {
                long n1 = Long.parseLong(o1);
                long n2 = Long.parseLong(o2);
                return Long.compare(n1, n2);
            } catch (NumberFormatException e) {
                return o1.compareTo(o2);
            }
        };

        List<String> ids = new ArrayList<>();
        ids.add("10");
        ids.add("2");
        ids.add("30");
        ids.add("A1");

        ids.sort(comparator);

        // Expected order: 2, 10, 30, A1
        assertEquals("2", ids.get(0));
        assertEquals("10", ids.get(1));
        assertEquals("30", ids.get(2));
        assertEquals("A1", ids.get(3));
    }

    // Helper method to simulate the UI search logic (which checks all fields)
    private List<Product> search(String term) {
        String lowerTerm = term.toLowerCase();
        return service.getAllProducts().stream()
                .filter(p -> p.getId().toLowerCase().contains(lowerTerm) ||
                        p.getBarcode().toLowerCase().contains(lowerTerm) ||
                        p.getName().toLowerCase().contains(lowerTerm) ||
                        p.getBrand().toLowerCase().contains(lowerTerm) ||
                        p.getSupplier().toLowerCase().contains(lowerTerm) ||
                        p.getStorageCondition().toLowerCase().contains(lowerTerm) ||
                        String.valueOf(p.getPrice()).contains(lowerTerm) ||
                        String.valueOf(p.getQuantity()).contains(lowerTerm))
                .collect(Collectors.toList());
    }

    @Test
    void testUnifiedSearchLogic_Barcode() {
        List<Product> results = search("222");
        assertEquals(1, results.size());
        assertEquals("Banana", results.get(0).getName());
    }

    @Test
    void testUnifiedSearchLogic_Supplier() {
        List<Product> results = search("SupplierX");
        assertEquals(2, results.size()); // Apple and Cherry
    }

    @Test
    void testUnifiedSearchLogic_Price() {
        List<Product> results = search("0.99");
        assertEquals(1, results.size());
        assertEquals("Banana", results.get(0).getName());
    }
}
