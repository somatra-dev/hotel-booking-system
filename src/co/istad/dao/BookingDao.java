package co.istad.dao;

import co.istad.domain.Booking;
import co.istad.domain.BookingStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingDao {

    Booking save(Booking booking);

    Optional<Booking> findById(Integer id);

    List<Booking> findByGuestPhone(String phone);

    List<Booking> findTodayCheckIns();

    List<Booking> findTodayCheckOuts();

    List<Booking> searchByGuestNameOrPhone(String query);

    void updateStatus(Integer bookingId, BookingStatus status);

}
