package co.istad.service.impl;

import co.istad.dao.RoomDao;
import co.istad.dao.impl.RoomDaoImpl;
import co.istad.domain.Room;
import co.istad.domain.RoomType;
import co.istad.dto.response.RoomAvailabilityResponse;
import co.istad.dto.response.RoomResponse;
import co.istad.service.RoomService;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomDao roomDao;

    public RoomServiceImpl() {
        this.roomDao = new RoomDaoImpl();
    }


    @Override
    public List<RoomResponse> getAllRooms() {
        // Only show available rooms
        return roomDao.findAvailableRooms().stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponse getRoomById(Integer id) {
        return roomDao.findById(id)
                .map(this::mapToRoomResponse)
                .orElse(null);
    }

    @Override
    public List<RoomResponse> getAvailableRoomsByType(RoomType roomType) {
        return roomDao.findAvailableRoomsByType(roomType).stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomResponse> getAvailableRoomsByTypeAndDateRange(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        return roomDao.findAvailableRoomsByTypeAndDateRange(roomType, checkIn, checkOut).stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoomAvailabilityResponse getRoomAvailabilitySummary() {
        Map<RoomType, Integer> availableCount = new LinkedHashMap<>();

        for (RoomType type : RoomType.values()) {
            int count = roomDao.countByTypeAndAvailability(type, true);
            availableCount.put(type, count);
        }

        // Only show available rooms
        List<RoomResponse> availableRooms = roomDao.findAvailableRooms().stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());

        return RoomAvailabilityResponse.builder()
                .availableCount(availableCount)
                .rooms(availableRooms)
                .build();
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .price(room.getPrice())
                .isAvailable(room.getIsAvailable())
                .build();
    }
}
