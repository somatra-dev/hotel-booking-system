package co.istad.telegram.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@FunctionalInterface
public interface MessageSender {

    void send(Long chatId, String text, ReplyKeyboard keyboard);

    default void send(Long chatId, String text) {
        send(chatId, text, null);
    }
}
