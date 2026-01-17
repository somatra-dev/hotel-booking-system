package co.istad.dao;

import co.istad.domain.Room;
import co.istad.domain.RoomType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomDao {

    Optional<Room> findById(Integer id);

    List<Room> findAvailableRooms();

    List<Room> findAvailableRoomsByType(RoomType roomType);

    List<Room> findAvailableRoomsByTypeAndDateRange(RoomType roomType, LocalDate checkIn, LocalDate checkOut);

    void updateAvailability(Integer roomId, boolean isAvailable);

    int countByTypeAndAvailability(RoomType roomType, boolean isAvailable);
}
