package co.istad.service.impl;

import co.istad.dao.BookingDao;
import co.istad.dao.RoomDao;
import co.istad.dao.impl.BookingDaoImpl;
import co.istad.dao.impl.RoomDaoImpl;
import co.istad.domain.Booking;
import co.istad.domain.BookingStatus;
import co.istad.domain.Room;
import co.istad.domain.RoomType;
import co.istad.dto.request.BookingCancelRequest;
import co.istad.dto.request.BookingCreateRequest;
import co.istad.dto.request.SearchRequest;
import co.istad.dto.response.BookingResponse;
import co.istad.dto.response.TodayScheduleResponse;
import co.istad.service.BookingService;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingDao bookingDao;
    private final RoomDao roomDao;

    public BookingServiceImpl() {
        this.bookingDao = new BookingDaoImpl();
        this.roomDao = new RoomDaoImpl();
    }

    @Override
    public BookingResponse createBooking(BookingCreateRequest request) {
        // Find available room
        List<Room> availableRooms = roomDao.findAvailableRoomsByTypeAndDateRange(
                request.getRoomType(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (availableRooms.isEmpty()) {
            throw new RuntimeException("No rooms available for the selected dates");
        }

        Room selectedRoom = availableRooms.getFirst();
        BigDecimal totalPrice = calculateTotalPrice(
                request.getRoomType(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        Booking booking = Booking.builder()
                .guestName(request.getGuestName())
                .guestEmail(request.getGuestEmail())
                .guestPhone(request.getGuestPhone())
                .roomId(selectedRoom.getId())
                .room(selectedRoom)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .telegramChatId(request.getTelegramChatId())
                .build();

        Booking savedBooking = bookingDao.save(booking);
        savedBooking.setRoom(selectedRoom);

        // Mark room as unavailable
        roomDao.updateAvailability(selectedRoom.getId(), false);

        return mapToBookingResponse(savedBooking);
    }

    @Override
    public BookingResponse getBookingById(Integer id) {
        return bookingDao.findById(id)
                .map(this::mapToBookingResponse)
                .orElse(null);
    }

    @Override
    public List<BookingResponse> getBookingsByPhone(String phone) {
        return bookingDao.findByGuestPhone(phone).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TodayScheduleResponse getTodaySchedule() {
        List<BookingResponse> checkIns = bookingDao.findTodayCheckIns().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        List<BookingResponse> checkOuts = bookingDao.findTodayCheckOuts().stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return TodayScheduleResponse.builder()
                .checkIns(checkIns)
                .checkOuts(checkOuts)
                .totalCheckIns(checkIns.size())
                .totalCheckOuts(checkOuts.size())
                .build();
    }

    @Override
    public List<BookingResponse> searchBookings(SearchRequest request) {
        return bookingDao.searchByGuestNameOrPhone(request.getQuery()).stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean cancelBooking(BookingCancelRequest request) {
        Optional<Booking> bookingOpt = bookingDao.findById(request.getBookingId());

        if (bookingOpt.isEmpty()) {
            return false;
        }

        Booking booking = bookingOpt.get();

        // Verify phone number matches
        if (!booking.getGuestPhone().equals(request.getGuestPhone())) {
            return false;
        }

        // Only allow cancellation of CONFIRMED bookings
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            return false;
        }

        bookingDao.updateStatus(request.getBookingId(), BookingStatus.CANCELLED);

        // Mark room as available again
        if (booking.getRoomId() != null) {
            roomDao.updateAvailability(booking.getRoomId(), true);
        }

        return true;
    }

    @Override
    public BigDecimal calculateTotalPrice(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        if (nights <= 0) {
            throw new RuntimeException("Check-out date must be after check-in date");
        }

        return roomType.getPrice().multiply(BigDecimal.valueOf(nights));
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .guestName(booking.getGuestName())
                .guestEmail(booking.getGuestEmail())
                .guestPhone(booking.getGuestPhone())
                .roomNumber(booking.getRoom() != null ? booking.getRoom().getRoomNumber() : null)
                .roomType(booking.getRoom() != null ? booking.getRoom().getRoomType() : null)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
