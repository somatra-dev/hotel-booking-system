package co.istad.telegram.handler;

import co.istad.domain.RoomType;
import co.istad.dto.request.SearchRequest;
import co.istad.dto.response.BookingResponse;
import co.istad.dto.response.RoomAvailabilityResponse;
import co.istad.dto.response.RoomResponse;
import co.istad.dto.response.TodayScheduleResponse;
import co.istad.service.BookingService;
import co.istad.service.RoomService;
import co.istad.telegram.session.SessionManager;
import co.istad.telegram.state.ConversationState;
import co.istad.telegram.state.UserState;
import co.istad.telegram.util.MessageFormatter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

public class StaffCommandHandler {

    private final TelegramLongPollingBot bot;
    private final BookingService bookingService;
    private final RoomService roomService;
    private final SessionManager sessionManager;
    private final Map<Long, UserState> userStates;

    public StaffCommandHandler(TelegramLongPollingBot bot, BookingService bookingService,
                               RoomService roomService, SessionManager sessionManager,
                               Map<Long, UserState> userStates) {
        this.bot = bot;
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.sessionManager = sessionManager;
        this.userStates = userStates;
    }

    private boolean checkAuthentication(Long chatId) {
        if (!sessionManager.isAuthenticated(chatId)) {
            sendMessage(chatId, "You must be logged in to use this command. Use /login first.");
            return false;
        }
        return true;
    }

    public void handleToday(Update update) {
        Long chatId = update.getMessage().getChatId();
        if (!checkAuthentication(chatId)) return;

        TodayScheduleResponse schedule = bookingService.getTodaySchedule();

        StringBuilder message = new StringBuilder("*Today's Schedule:*\n\n");

        message.append(String.format("*Check-ins (%d):*\n", schedule.getTotalCheckIns()));
        if (schedule.getCheckIns().isEmpty()) {
            message.append("No check-ins today.\n");
        } else {
            for (BookingResponse b : schedule.getCheckIns()) {
                message.append(String.format("- #%d | %s | Room %s | %s\n",
                        b.getId(), b.getGuestName(),
                        b.getRoomNumber() != null ? b.getRoomNumber() : "TBD",
                        b.getGuestPhone()));
            }
        }

        message.append(String.format("\n*Check-outs (%d):*\n", schedule.getTotalCheckOuts()));
        if (schedule.getCheckOuts().isEmpty()) {
            message.append("No check-outs today.\n");
        } else {
            for (BookingResponse b : schedule.getCheckOuts()) {
                message.append(String.format("- #%d | %s | Room %s | %s\n",
                        b.getId(), b.getGuestName(),
                        b.getRoomNumber() != null ? b.getRoomNumber() : "N/A",
                        b.getGuestPhone()));
            }
        }

        sendMessage(chatId, message.toString());
    }

    public void handleSearch(Update update) {
        Long chatId = update.getMessage().getChatId();
        if (!checkAuthentication(chatId)) return;

        UserState state = new UserState();
        state.setConversationState(ConversationState.STAFF_SEARCH_ENTER_QUERY);
        userStates.put(chatId, state);

        sendMessage(chatId, "Enter guest name or phone number to search:");
    }

    public void handleSearchQueryInput(Update update, UserState userState) {
        Long chatId = update.getMessage().getChatId();
        if (!checkAuthentication(chatId)) {
            userStates.remove(chatId);
            return;
        }

        String query = update.getMessage().getText().trim();
        SearchRequest request = SearchRequest.builder().query(query).build();

        List<BookingResponse> results = bookingService.searchBookings(request);

        if (results.isEmpty()) {
            sendMessage(chatId, "No bookings found for: " + query);
        } else {
            String message = "*Search Results:*\n\n" + MessageFormatter.formatBookingList(results);
            sendMessage(chatId, message);
        }

        userStates.remove(chatId);
    }

    public void handleRooms(Update update) {
        Long chatId = update.getMessage().getChatId();
        if (!checkAuthentication(chatId)) return;

        RoomAvailabilityResponse availability = roomService.getRoomAvailabilitySummary();

        StringBuilder message = new StringBuilder("*Room Status Overview:*\n\n");

        message.append("*Summary:*\n");
        for (Map.Entry<RoomType, Integer> entry : availability.getAvailableCount().entrySet()) {
            message.append(String.format("%s: %d available\n", entry.getKey().name(), entry.getValue()));
        }

        message.append("\n*All Rooms:*\n");
        for (RoomResponse room : availability.getRooms()) {
            String status = room.getIsAvailable() ? "Available" : "Occupied";
            message.append(String.format("Room %s | %s | $%s | %s\n",
                    room.getRoomNumber(), room.getRoomType().name(), room.getPrice(), status));
        }

        sendMessage(chatId, message.toString());
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
}
