package co.istad.service;

import co.istad.domain.RoomType;
import co.istad.dto.request.BookingCancelRequest;
import co.istad.dto.request.BookingCreateRequest;
import co.istad.dto.request.SearchRequest;
import co.istad.dto.response.BookingResponse;
import co.istad.dto.response.TodayScheduleResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingCreateRequest request);

    BookingResponse getBookingById(Integer id);

    List<BookingResponse> getBookingsByPhone(String phone);

    TodayScheduleResponse getTodaySchedule();

    List<BookingResponse> searchBookings(SearchRequest request);

    boolean cancelBooking(BookingCancelRequest request);

    BigDecimal calculateTotalPrice(RoomType roomType, LocalDate checkIn, LocalDate checkOut);
}
