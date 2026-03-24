package inventory.util;

import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {

    // Base currency: CAD
    private static final Map<String, Float> rates = new HashMap<>();

    static {
        rates.put("CAD", 1.0f);

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

        float valueInCAD = value / rates.get(baseCurrency);
        return valueInCAD * rates.get(convertedCurrency);
    }

    // Currency symbol helper method
    public static String getSymbol(String currency) {
        currency = currency.toUpperCase();

        switch (currency) {
            case "CAD":
            case "USD":
                return "$";
            case "EUR":
                return "€";
            case "GBP":
                return "£";
            case "JPY":
                return "¥";
            case "CHF":
                return "CHF";
            case "INR":
                return "₹";
            case "PKR":
                return "₨";
            case "SAR":
                return "﷼";
            default:
                return currency;
        }
    }

    // Price formatter helper method
    public static String format(float amount, String currency) {
        String symbol = getSymbol(currency);

        currency = currency.toUpperCase();

        // Add code for ambiguous currencies
        if (currency.equals("CAD") || currency.equals("USD")) {
            return String.format("%s%.2f %s", symbol, amount, currency);
        }

        return String.format("%s%.2f", symbol, amount);
    }
}