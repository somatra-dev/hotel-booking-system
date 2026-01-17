package co.istad.telegram.handler;

import co.istad.dto.request.StaffLoginRequest;
import co.istad.dto.response.StaffLoginResponse;
import co.istad.service.StaffService;
import co.istad.telegram.session.SessionManager;
import co.istad.telegram.state.ConversationState;
import co.istad.telegram.state.UserState;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

public class LoginFlowHandler {

    private final TelegramLongPollingBot bot;
    private final StaffService staffService;
    private final SessionManager sessionManager;
    private final Map<Long, UserState> userStates;

    public LoginFlowHandler(TelegramLongPollingBot bot, StaffService staffService,
                            SessionManager sessionManager, Map<Long, UserState> userStates) {
        this.bot = bot;
        this.staffService = staffService;
        this.sessionManager = sessionManager;
        this.userStates = userStates;
    }

    public void handleLogin(Update update) {
        Long chatId = update.getMessage().getChatId();

        if (sessionManager.isAuthenticated(chatId)) {
            String staffName = sessionManager.getStaffName(chatId);
            sendMessage(chatId, "You are already logged in as " + staffName + ".\nUse /logout to logout first.");
            return;
        }

        UserState state = new UserState();
        state.setConversationState(ConversationState.LOGIN_ENTER_EMAIL);
        userStates.put(chatId, state);

        sendMessage(chatId, "*Staff Login*\n\nPlease enter your email:");
    }

    public void handleLoginFlow(Update update, UserState userState) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        switch (userState.getConversationState()) {
            case LOGIN_ENTER_EMAIL:
                userState.setLoginEmail(text);
                userState.setConversationState(ConversationState.LOGIN_ENTER_PASSWORD);
                sendMessage(chatId, "Please enter your password:");
                break;

            case LOGIN_ENTER_PASSWORD:
                handlePasswordInput(update, text, userState);
                break;

            default:
                userStates.remove(chatId);
        }
    }

    private void handlePasswordInput(Update update, String password, UserState userState) {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();
        String email = userState.getLoginEmail();

        StaffLoginRequest request = StaffLoginRequest.builder()
                .email(email)
                .password(password)
                .telegramUserId(userId)
                .build();

        StaffLoginResponse response = staffService.authenticate(request);

        if (response.isSuccess()) {
            sessionManager.createSession(chatId, response.getStaffName());

            sendMessage(chatId, String.format("""
                    *Login Successful!*

                    Welcome, %s!

                    *Staff Commands:*
                    /today - View today's check-ins/outs
                    /search - Search bookings
                    /rooms - View room status
                    /logout - Logout

                    Session valid for 24 hours.
                    """, response.getStaffName()));
        } else {
            sendMessage(chatId, "Invalid email or password. Use /login to try again.");
        }

        userStates.remove(chatId);
    }

    public void handleLogout(Update update) {
        Long chatId = update.getMessage().getChatId();

        if (sessionManager.isAuthenticated(chatId)) {
            sessionManager.removeSession(chatId);
            sendMessage(chatId, "You have been logged out successfully.");
        } else {
            sendMessage(chatId, "You are not logged in.");
        }
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
