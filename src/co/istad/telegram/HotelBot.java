package co.istad.telegram;

import co.istad.service.BookingService;
import co.istad.service.RoomService;
import co.istad.service.StaffService;
import co.istad.service.impl.BookingServiceImpl;
import co.istad.service.impl.RoomServiceImpl;
import co.istad.service.impl.StaffServiceImpl;
import co.istad.telegram.handler.*;
import co.istad.telegram.session.SessionManager;
import co.istad.telegram.state.ConversationState;
import co.istad.telegram.state.UserState;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HotelBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;

    // User conversation states
    private final Map<Long, UserState> userStates;

    // Handlers
    private final GuestCommandHandler guestHandler;
    private final StaffCommandHandler staffHandler;
    private final BookingFlowHandler bookingFlowHandler;
    private final LoginFlowHandler loginFlowHandler;

    public HotelBot(String botToken, String botUsername) {
        this.botToken = botToken;
        this.botUsername = botUsername;

        // Initialize services
        // Services
        BookingService bookingService = new BookingServiceImpl();
        RoomService roomService = new RoomServiceImpl();
        StaffService staffService = new StaffServiceImpl();

        // Initialize session manager
        // Session management
        SessionManager sessionManager = new SessionManager();

        // Initialize user states map
        this.userStates = new ConcurrentHashMap<>();

        // Initialize handlers
        this.guestHandler = new GuestCommandHandler(this, bookingService, roomService, userStates);
        this.staffHandler = new StaffCommandHandler(this, bookingService, roomService, sessionManager, userStates);
        this.bookingFlowHandler = new BookingFlowHandler(this, bookingService, roomService, userStates);
        this.loginFlowHandler = new LoginFlowHandler(this, staffService, sessionManager, userStates);
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        // Check if user has an active conversation state
        UserState userState = userStates.get(chatId);

        // Handle conversation flows
        if (userState != null && userState.getConversationState() != ConversationState.NONE) {
            handleConversationFlow(update, userState);
            return;
        }

        // Handle commands
        if (text.startsWith("/")) {
            handleCommand(update, text, chatId);
        }
    }

    private void handleCommand(Update update, String text, Long chatId) {
        String command = text.split(" ")[0].toLowerCase();

        switch (command) {
            // Guest commands (no authentication required)
            case "/start":
                guestHandler.handleStart(update);
                break;
            case "/book":
                guestHandler.handleBook(update);
                break;
            case "/mybookings":
                guestHandler.handleMyBookings(update);
                break;
            case "/cancel":
                guestHandler.handleCancel(update);
                break;
            case "/help":
                guestHandler.handleHelp(update);
                break;

            // Staff commands (authentication required)
            case "/login":
                loginFlowHandler.handleLogin(update);
                break;
            case "/logout":
                loginFlowHandler.handleLogout(update);
                break;
            case "/today":
                staffHandler.handleToday(update);
                break;
            case "/search":
                staffHandler.handleSearch(update);
                break;
            case "/rooms":
                staffHandler.handleRooms(update);
                break;

            default:
                sendMessage(chatId);
        }
    }

    private void handleConversationFlow(Update update, UserState userState) {
        switch (userState.getConversationState()) {
            case BOOKING_SELECT_ROOM_TYPE:
            case BOOKING_ENTER_CHECK_IN:
            case BOOKING_ENTER_CHECK_OUT:
            case BOOKING_ENTER_NAME:
            case BOOKING_ENTER_EMAIL:
            case BOOKING_ENTER_PHONE:
            case BOOKING_CONFIRM:
                bookingFlowHandler.handleBookingFlow(update, userState);
                break;

            case MY_BOOKINGS_ENTER_PHONE:
                guestHandler.handleMyBookingsPhoneInput(update, userState);
                break;

            case CANCEL_ENTER_BOOKING_ID:
            case CANCEL_ENTER_PHONE:
                guestHandler.handleCancelFlow(update, userState);
                break;

            case LOGIN_ENTER_EMAIL:
            case LOGIN_ENTER_PASSWORD:
                loginFlowHandler.handleLoginFlow(update, userState);
                break;

            case STAFF_SEARCH_ENTER_QUERY:
                staffHandler.handleSearchQueryInput(update, userState);
                break;

            default:
                userStates.remove(update.getMessage().getChatId());
        }
    }

    private void sendMessage(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Unknown command. Use /help to see available commands.")
                .parseMode("Markdown")
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}
