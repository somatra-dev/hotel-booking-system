package co.istad;

import co.istad.config.DataSourceConfig;
import co.istad.config.TelegramConfig;
import co.istad.telegram.HotelBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting Hotel Booking Bot...");

        // Initialize configurations
        System.out.println("Loading configurations...");
        TelegramConfig.init();
        DataSourceConfig.init();

        System.out.println("Database connection established.");
        System.out.println("Bot token loaded.");

        try {
            // Create and register bot
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            HotelBot hotelBot = new HotelBot(
                    TelegramConfig.getBotToken(),
                    TelegramConfig.getBotUsername()
            );

            botsApi.registerBot(hotelBot);
            System.out.println("Hotel Booking Bot is running!");
            System.out.println("Username: @" + TelegramConfig.getBotUsername());

        } catch (TelegramApiException e) {
            System.err.println("Failed to start bot: " + e.getMessage());
        }
    }
}
