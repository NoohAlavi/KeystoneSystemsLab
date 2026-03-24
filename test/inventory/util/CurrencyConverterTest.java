package inventory.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CurrencyConverterTest {

    private static final float DELTA = 0.01f; // tolerance for float comparisons

    @Test
    void testConvert_CADtoUSD() {
        float result = CurrencyConverter.convert(100f, "CAD", "USD");
        assertEquals(73.0f, result, DELTA);
    }

    @Test
    void testConvert_USDtoCAD() {
        float result = CurrencyConverter.convert(73f, "USD", "CAD");
        assertEquals(100f, result, DELTA);
    }

    @Test
    void testConvert_CADtoEUR() {
        float result = CurrencyConverter.convert(100f, "CAD", "EUR");
        assertEquals(63.0f, result, DELTA);
    }

    @Test
    void testConvert_SameCurrency() {
        float result = CurrencyConverter.convert(50f, "CAD", "CAD");
        assertEquals(50f, result, DELTA);
    }

    @Test
    void testConvert_InvalidCurrency() {
        assertThrows(IllegalArgumentException.class, () -> {
            CurrencyConverter.convert(100f, "CAD", "ABC");
        });
    }

    @Test
    void testGetSymbol() {
        assertEquals("$", CurrencyConverter.getSymbol("CAD"));
        assertEquals("€", CurrencyConverter.getSymbol("EUR"));
        assertEquals("£", CurrencyConverter.getSymbol("GBP"));
        assertEquals("¥", CurrencyConverter.getSymbol("JPY"));
    }

    @Test
    void testFormat_CAD() {
        String result = CurrencyConverter.format(12.5f, "CAD");
        assertEquals("$12.50 CAD", result);
    }

    @Test
    void testFormat_USD() {
        String result = CurrencyConverter.format(12.5f, "USD");
        assertEquals("$12.50 USD", result);
    }

    @Test
    void testFormat_EUR() {
        String result = CurrencyConverter.format(12.5f, "EUR");
        assertEquals("€12.50", result);
    }
}