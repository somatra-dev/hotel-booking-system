package co.istad.telegram.util;

import co.istad.dto.response.BookingResponse;

import java.util.List;


public class MessageFormatter {
    private MessageFormatter() {}

    public static String formatBookingList(List<BookingResponse> bookings) {
        StringBuilder sb = new StringBuilder();

        for (BookingResponse booking : bookings) {
            sb.append(formatBooking(booking));
            sb.append("\n---\n");
        }

        return sb.toString();
    }

    public static String formatBooking(BookingResponse booking) {
        return String.format("""
                *Booking #%d*
                Status: %s
                Room: %s (%s)
                Guest: %s
                Phone: %s
                Check-in: %s
                Check-out: %s
                Total: $%s
                """,
                booking.getId(),
                booking.getStatus() != null ? booking.getStatus().name() : "N/A",
                booking.getRoomNumber() != null ? booking.getRoomNumber() : "TBD",
                booking.getRoomType() != null ? booking.getRoomType().name() : "N/A",
                booking.getGuestName(),
                booking.getGuestPhone(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalPrice()
        );
    }
}
