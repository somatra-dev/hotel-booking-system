package co.istad.telegram.state;

import co.istad.domain.RoomType;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class UserState {

    private ConversationState conversationState = ConversationState.NONE;
    private Map<String, Object> data = new HashMap<>();

    // Booking data
    private RoomType selectedRoomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String guestName;
    private String guestEmail;
    private String guestPhone;

    // Cancel data
    private Integer bookingIdToCancel;

    // Login data
    private String loginEmail;
}
