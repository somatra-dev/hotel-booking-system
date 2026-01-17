package co.istad.dto.response;

import co.istad.domain.BookingStatus;
import co.istad.domain.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Integer id;

    private String guestName;

    private String guestEmail;

    private String guestPhone;

    private String roomNumber;

    private RoomType roomType;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BigDecimal totalPrice;

    private BookingStatus status;

    private LocalDateTime createdAt;
}
