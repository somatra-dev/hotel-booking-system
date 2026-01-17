package co.istad.domain;

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
public class Booking {

    private Integer id;

    private String guestName;

    private String guestEmail;

    private String guestPhone;

    private Integer roomId;

    private Room room;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BigDecimal totalPrice;

    private BookingStatus status;

    private LocalDateTime createdAt;

    private Long telegramChatId;
}
