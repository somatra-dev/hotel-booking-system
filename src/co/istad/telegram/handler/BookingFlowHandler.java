package co.istad.telegram.handler;

import co.istad.domain.RoomType;
import co.istad.dto.request.BookingCreateRequest;
import co.istad.dto.response.BookingResponse;
import co.istad.dto.response.RoomResponse;
import co.istad.service.BookingService;
import co.istad.service.RoomService;
import co.istad.telegram.state.ConversationState;
import co.istad.telegram.state.UserState;
import co.istad.telegram.util.KeyboardFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

public class BookingFlowHandler {

    private final TelegramLongPollingBot bot;
    private final BookingService bookingService;
    private final RoomService roomService;
    private final Map<Long, UserState> userStates;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public BookingFlowHandler(TelegramLongPollingBot bot, BookingService bookingService,
                              RoomService roomService, Map<Long, UserState> userStates) {
        this.bot = bot;
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.userStates = userStates;
    }

    public void handleBookingFlow(Update update, UserState userState) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        switch (userState.getConversationState()) {
            case BOOKING_SELECT_ROOM_TYPE:
                handleRoomTypeSelection(chatId, text, userState);
                break;

            case BOOKING_ENTER_CHECK_IN:
                handleCheckInDate(chatId, text, userState);
                break;

            case BOOKING_ENTER_CHECK_OUT:
                handleCheckOutDate(chatId, text, userState);
                break;

            case BOOKING_ENTER_NAME:
                handleGuestName(chatId, text, userState);
                break;

            case BOOKING_ENTER_EMAIL:
                handleGuestEmail(chatId, text, userState);
                break;

            case BOOKING_ENTER_PHONE:
                handleGuestPhone(chatId, text, userState);
                break;

            case BOOKING_CONFIRM:
                handleConfirmation(chatId, text, userState);
                break;
        }
    }

    private void handleRoomTypeSelection(Long chatId, String text, UserState userState) {
        try {
            RoomType roomType = RoomType.fromString(text);
            userState.setSelectedRoomType(roomType);
            userState.setConversationState(ConversationState.BOOKING_ENTER_CHECK_IN);

            sendMessage(chatId, String.format(
                    "You selected: *%s* ($%s/night)\n\nPlease enter check-in date (YYYY-MM-DD):",
                    roomType.name(), roomType.getPrice()
            ));
        } catch (IllegalArgumentException e) {
            sendMessage(chatId, "Invalid room type. Please select: SINGLE, DOUBLE, FAMILY, or VIP");
        }
    }

    private void handleCheckInDate(Long chatId, String text, UserState userState) {
        try {
            LocalDate checkIn = LocalDate.parse(text, DATE_FORMATTER);

            if (checkIn.isBefore(LocalDate.now())) {
                sendMessage(chatId, "Check-in date cannot be in the past. Please enter a valid date:");
                return;
            }

            userState.setCheckInDate(checkIn);
            userState.setConversationState(ConversationState.BOOKING_ENTER_CHECK_OUT);

            sendMessage(chatId, "Please enter check-out date (YYYY-MM-DD):");
        } catch (DateTimeParseException e) {
            sendMessage(chatId, "Invalid date format. Please use YYYY-MM-DD (e.g., 2024-12-25):");
        }
    }

    private void handleCheckOutDate(Long chatId, String text, UserState userState) {
        try {
            LocalDate checkOut = LocalDate.parse(text, DATE_FORMATTER);

            if (!checkOut.isAfter(userState.getCheckInDate())) {
                sendMessage(chatId, "Check-out date must be after check-in date. Please enter a valid date:");
                return;
            }

            // Check room availability
            List<RoomResponse> availableRooms = roomService.getAvailableRoomsByTypeAndDateRange(
                    userState.getSelectedRoomType(),
                    userState.getCheckInDate(),
                    checkOut
            );

            if (availableRooms.isEmpty()) {
                sendMessage(chatId, String.format(
                        "Sorry, no %s rooms available for the selected dates.\n\nUse /book to try different dates.",
                        userState.getSelectedRoomType().name()
                ));
                userStates.remove(chatId);
                return;
            }

            userState.setCheckOutDate(checkOut);
            userState.setConversationState(ConversationState.BOOKING_ENTER_NAME);

            sendMessage(chatId, "Please enter guest name:");
        } catch (DateTimeParseException e) {
            sendMessage(chatId, "Invalid date format. Please use YYYY-MM-DD:");
        }
    }

    private void handleGuestName(Long chatId, String text, UserState userState) {
        if (text.length() < 2) {
            sendMessage(chatId, "Name too short. Please enter a valid name:");
            return;
        }

        userState.setGuestName(text);
        userState.setConversationState(ConversationState.BOOKING_ENTER_EMAIL);

        sendMessage(chatId, "Please enter email address (or type 'skip' to skip):");
    }

    private void handleGuestEmail(Long chatId, String text, UserState userState) {
        if (!text.equalsIgnoreCase("skip")) {
            if (!text.contains("@") || !text.contains(".")) {
                sendMessage(chatId, "Invalid email format. Please enter a valid email or type 'skip':");
                return;
            }
            userState.setGuestEmail(text);
        }

        userState.setConversationState(ConversationState.BOOKING_ENTER_PHONE);
        sendMessage(chatId, "Please enter phone number:");
    }

    private void handleGuestPhone(Long chatId, String text, UserState userState) {
        if (text.length() < 8) {
            sendMessage(chatId, "Invalid phone number. Please enter a valid phone number:");
            return;
        }

        userState.setGuestPhone(text);
        userState.setConversationState(ConversationState.BOOKING_CONFIRM);

        BigDecimal totalPrice = bookingService.calculateTotalPrice(
                userState.getSelectedRoomType(),
                userState.getCheckInDate(),
                userState.getCheckOutDate()
        );

        String confirmMessage = String.format("""
                *Booking Summary:*

                Room Type: %s
                Check-in: %s
                Check-out: %s
                Guest Name: %s
                Email: %s
                Phone: %s

                *Total Price: $%s*

                Reply *YES* to confirm or *NO* to cancel:
                """,
                userState.getSelectedRoomType().name(),
                userState.getCheckInDate().format(DATE_FORMATTER),
                userState.getCheckOutDate().format(DATE_FORMATTER),
                userState.getGuestName(),
                userState.getGuestEmail() != null ? userState.getGuestEmail() : "Not provided",
                userState.getGuestPhone(),
                totalPrice
        );

        sendMessageWithKeyboard(chatId, confirmMessage, KeyboardFactory.createConfirmKeyboard());
    }

    private void handleConfirmation(Long chatId, String text, UserState userState) {
        if (text.equalsIgnoreCase("YES")) {
            try {
                BookingCreateRequest request = BookingCreateRequest.builder()
                        .guestName(userState.getGuestName())
                        .guestEmail(userState.getGuestEmail())
                        .guestPhone(userState.getGuestPhone())
                        .roomType(userState.getSelectedRoomType())
                        .checkInDate(userState.getCheckInDate())
                        .checkOutDate(userState.getCheckOutDate())
                        .telegramChatId(chatId)
                        .build();

                BookingResponse booking = bookingService.createBooking(request);

                String successMessage = String.format("""
                        *Booking Confirmed!*

                        Booking ID: #%d
                        Room: %s
                        Check-in: %s
                        Check-out: %s
                        Total: $%s

                        Please save your Booking ID for future reference.
                        Use /mybookings to view your bookings.
                        """,
                        booking.getId(),
                        booking.getRoomNumber() != null ? booking.getRoomNumber() : "TBD",
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getTotalPrice()
                );

                sendMessage(chatId, successMessage);
            } catch (RuntimeException e) {
                sendMessage(chatId, "Sorry, booking failed: " + e.getMessage() + "\n\nPlease try again with /book");
            }
        } else {
            sendMessage(chatId, "Booking cancelled. Use /book to start a new booking.");
        }

        userStates.remove(chatId);
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
