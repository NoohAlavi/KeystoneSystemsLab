package inventory.util;

import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CurrencyConverter {

    private static final String API_URL = "https://open.er-api.com/v6/latest/CAD";
    private static Map<String, Float> rates = new HashMap<>();
    private static String[] availableCurrencies;

    static {
        // Default CAD rates in case API fails
        rates.put("CAD", 1.0f);
        rates.put("USD", 0.73f);
        rates.put("EUR", 0.63f);
        rates.put("GBP", 0.55f);
        rates.put("JPY", 116.30f);
        rates.put("CHF", 0.57f);
        rates.put("INR", 67.76f);
        rates.put("PKR", 203.87f);
        rates.put("SAR", 2.74f);

        updateAvailableCurrencies();

        // Try to update rates from API
        updateRates();
    }

    private static void updateAvailableCurrencies() {
        availableCurrencies = rates.keySet().stream().sorted().toArray(String[]::new);
    }

    public static float convert(float value, String from, String to) {
        from = from.toUpperCase();
        to = to.toUpperCase();

        if (!rates.containsKey(from) || !rates.containsKey(to)) {
            return value; // Fallback to original value if currency missing
        }

        float valueInCAD = value / rates.get(from);
        return valueInCAD * rates.get(to);
    }

    public static String[] getAvailableCurrencies() {
        return availableCurrencies;
    }

    public static String getSymbol(String currency) {
        currency = currency.toUpperCase();
        switch (currency) {
            case "CAD":
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            case "JPY": return "¥";
            case "CHF": return "CHF";
            case "INR": return "₹";
            case "PKR": return "₨";
            case "SAR": return "﷼";
            default: return currency;
        }
    }

    public static String format(float amount, String currency) {
        String symbol = getSymbol(currency);
        currency = currency.toUpperCase();
        if (currency.equals("CAD") || currency.equals("USD")) {
            return String.format("%s%.2f %s", symbol, amount, currency);
        }
        return String.format("%s%.2f", symbol, amount);
    }

    private static void updateRates() {
        new Thread(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                if (conn.getResponseCode() == 200) {
                    Scanner sc = new Scanner(url.openStream());
                    StringBuilder sb = new StringBuilder();
                    while (sc.hasNext()) sb.append(sc.nextLine());
                    sc.close();

                    JSONObject obj = new JSONObject(sb.toString());
                    JSONObject fetchedRates = obj.getJSONObject("rates");

                    for (String key : fetchedRates.keySet()) {
                        rates.put(key.toUpperCase(), (float) fetchedRates.getDouble(key));
                    }
                    updateAvailableCurrencies();
                }
            } catch (Exception e) {
                System.out.println("Using static exchange rates (API unreachable).");
            }
        }).start();
    }
}