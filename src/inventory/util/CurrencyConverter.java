package inventory.util;

import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {

    // Base currency: CAD
    private static final Map<String, Float> rates = new HashMap<>();

    static {
        // Base currency
        rates.put("CAD", 1.0f);

        // Rates relative to CAD
        rates.put("USD", 0.73f);
        rates.put("EUR", 0.63f);
        rates.put("GBP", 0.55f);
        rates.put("JPY", 116.30f);
        rates.put("CHF", 0.57f);
        rates.put("INR", 67.76f);
        rates.put("PKR", 203.87f);
        rates.put("SAR", 2.74f);
    }

    public static float convert(float value, String baseCurrency, String convertedCurrency) {
        baseCurrency = baseCurrency.toUpperCase();
        convertedCurrency = convertedCurrency.toUpperCase();

        if (!rates.containsKey(baseCurrency) || !rates.containsKey(convertedCurrency)) {
            throw new IllegalArgumentException("Unsupported currency!");
        }

        // Step 1: convert base → CAD
        float valueInCAD = value / rates.get(baseCurrency);

        // Step 2: convert CAD → target
        return valueInCAD * rates.get(convertedCurrency);
    }
}