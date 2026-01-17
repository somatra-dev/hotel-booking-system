package co.istad.telegram.handler;

import co.istad.domain.RoomType;
import co.istad.dto.response.BookingResponse;
import co.istad.dto.response.RoomAvailabilityResponse;
import co.istad.service.BookingService;
import co.istad.service.RoomService;
import co.istad.telegram.state.ConversationState;
import co.istad.telegram.state.UserState;
import co.istad.telegram.util.KeyboardFactory;
import co.istad.telegram.util.MessageFormatter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

public class GuestCommandHandler {

    private final TelegramLongPollingBot bot;
    private final BookingService bookingService;
    private final RoomService roomService;
    private final Map<Long, UserState> userStates;

    public GuestCommandHandler(TelegramLongPollingBot bot, BookingService bookingService,
                               RoomService roomService, Map<Long, UserState> userStates) {
        this.bot = bot;
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.userStates = userStates;
    }

    public void handleStart(Update update) {
        Long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getFrom().getFirstName();

        String welcomeMessage = String.format("""
                Welcome to *Hotel Booking Bot*, %s!

                I can help you book a room at our hotel.

                *Guest Commands:*
                /book - Book a room
                /mybookings - View your bookings
                /cancel - Cancel a booking
                /help - Show help

                *Room Types & Prices:*
                SINGLE - $40/night
                DOUBLE - $60/night
                FAMILY - $100/night
                VIP - $200/night

                Use /book to start booking!
                """, firstName);

        sendMessage(chatId, welcomeMessage);
    }

    public void handleBook(Update update) {
        Long chatId = update.getMessage().getChatId();

        UserState state = new UserState();
        state.setConversationState(ConversationState.BOOKING_SELECT_ROOM_TYPE);
        userStates.put(chatId, state);

        RoomAvailabilityResponse availability = roomService.getRoomAvailabilitySummary();

        String message = String.format("""
                *Select Room Type:*

                Available rooms:
                SINGLE ($40/night) - %d available
                DOUBLE ($60/night) - %d available
                FAMILY ($100/night) - %d available
                VIP ($200/night) - %d available

                Reply with room type (SINGLE, DOUBLE, FAMILY, VIP):
                """,
                availability.getAvailableCount().getOrDefault(RoomType.SINGLE, 0),
                availability.getAvailableCount().getOrDefault(RoomType.DOUBLE, 0),
                availability.getAvailableCount().getOrDefault(RoomType.FAMILY, 0),
                availability.getAvailableCount().getOrDefault(RoomType.VIP, 0)
        );

        sendMessageWithKeyboard(chatId, message, KeyboardFactory.createRoomTypeKeyboard());
    }

    public void handleMyBookings(Update update) {
        Long chatId = update.getMessage().getChatId();

        UserState state = new UserState();
        state.setConversationState(ConversationState.MY_BOOKINGS_ENTER_PHONE);
        userStates.put(chatId, state);

        sendMessage(chatId, "Please enter your phone number to view your bookings:");
    }

    public void handleMyBookingsPhoneInput(Update update, UserState userState) {
        Long chatId = update.getMessage().getChatId();
        String phone = update.getMessage().getText().trim();

        List<BookingResponse> bookings = bookingService.getBookingsByPhone(phone);

        if (bookings.isEmpty()) {
            sendMessage(chatId, "No bookings found for phone number: " + phone);
        } else {
            String message = "*Your Bookings:*\n\n" + MessageFormatter.formatBookingList(bookings);
            sendMessage(chatId, message);
        }

        userStates.remove(chatId);
    }

    public void handleCancel(Update update) {
        Long chatId = update.getMessage().getChatId();

        UserState state = new UserState();
        state.setConversationState(ConversationState.CANCEL_ENTER_BOOKING_ID);
        userStates.put(chatId, state);

        sendMessage(chatId, "Please enter the Booking ID you want to cancel:");
    }

    public void handleCancelFlow(Update update, UserState userState) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        switch (userState.getConversationState()) {
            case CANCEL_ENTER_BOOKING_ID:
                try {
                    Integer bookingId = Integer.parseInt(text);
                    userState.setBookingIdToCancel(bookingId);
                    userState.setConversationState(ConversationState.CANCEL_ENTER_PHONE);
                    sendMessage(chatId, "Please enter your phone number to confirm cancellation:");
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Invalid booking ID. Please enter a valid number:");
                }
                break;

            case CANCEL_ENTER_PHONE:
                handleCancelConfirmation(chatId, text, userState);
                break;

            default:
                userStates.remove(chatId);
        }
    }

    private void handleCancelConfirmation(Long chatId, String phone, UserState userState) {
        co.istad.dto.request.BookingCancelRequest request = co.istad.dto.request.BookingCancelRequest.builder()
                .bookingId(userState.getBookingIdToCancel())
                .guestPhone(phone)
                .build();

        boolean cancelled = bookingService.cancelBooking(request);

        if (cancelled) {
            sendMessage(chatId, "Booking #" + userState.getBookingIdToCancel() + " has been cancelled successfully.");
        } else {
            sendMessage(chatId, "Failed to cancel booking. Please check your booking ID and phone number.");
        }

        userStates.remove(chatId);
    }

    public void handleHelp(Update update) {
        Long chatId = update.getMessage().getChatId();

        String helpMessage = """
                *Hotel Booking Bot - Help*

                *Guest Commands:*
                /start - Welcome message and menu
                /book - Start booking process
                /mybookings - View your bookings by phone
                /cancel - Cancel a booking
                /help - Show this help

                *Staff Commands:* (Login required)
                /login - Staff login
                /logout - Staff logout
                /today - View today's check-ins/outs
                /search - Search bookings
                /rooms - View room status

                *Room Types & Prices:*
                SINGLE - $40/night
                DOUBLE - $60/night
                FAMILY - $100/night
                VIP - $200/night
                """;

        sendMessage(chatId, helpMessage);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("Markdown")
                .build();

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void sendMessageWithKeyboard(Long chatId, String text,
                                         org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(keyboard)
                .build();

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message with keyboard: " + e.getMessage());
        }
    }
}
