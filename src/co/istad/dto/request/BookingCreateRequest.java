package co.istad.dto.request;

import co.istad.domain.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateRequest {

    private String guestName;

    private String guestEmail;

    private String guestPhone;

    private RoomType roomType;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Long telegramChatId;
}
