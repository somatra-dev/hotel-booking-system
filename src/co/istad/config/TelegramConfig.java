package co.istad.config;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class TelegramConfig {

    @Getter
    private static String botToken;
    @Getter
    private static String botUsername;

    public static void init() {
        Properties props = new Properties();
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("=") && !line.startsWith("#")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        props.setProperty(parts[0].trim(), parts[1].trim());
                    }
                }
            }
            botToken = props.getProperty("TLG_TOKEN");
            botUsername = props.getProperty("TLG_USERNAME", "HotelBookingBot");

            if (botToken == null || botToken.isEmpty()) {
                throw new RuntimeException("TLG_TOKEN not found in .env file");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load .env file", e);
        }
    }

}
