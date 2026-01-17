package co.istad.service;

import co.istad.domain.RoomType;
import co.istad.dto.response.RoomAvailabilityResponse;
import co.istad.dto.response.RoomResponse;

import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    List<RoomResponse> getAllRooms();

    RoomResponse getRoomById(Integer id);

    List<RoomResponse> getAvailableRoomsByType(RoomType roomType);

    List<RoomResponse> getAvailableRoomsByTypeAndDateRange(RoomType roomType, LocalDate checkIn, LocalDate checkOut);

    RoomAvailabilityResponse getRoomAvailabilitySummary();
}
