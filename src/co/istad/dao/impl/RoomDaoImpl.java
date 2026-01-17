package co.istad.dao.impl;

import co.istad.config.DataSourceConfig;
import co.istad.dao.RoomDao;
import co.istad.domain.Room;
import co.istad.domain.RoomType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDaoImpl implements RoomDao {

    @Override
    public Optional<Room> findById(Integer id) {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding room by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Room> findAvailableRooms() {
        String sql = "SELECT * FROM rooms WHERE is_available = TRUE ORDER BY room_type, room_number";
        return executeQuery(sql);
    }

    @Override
    public List<Room> findAvailableRoomsByType(RoomType roomType) {
        String sql = "SELECT * FROM rooms WHERE room_type = ?::room_type AND is_available = TRUE ORDER BY room_number";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, roomType.name());
            return mapResultSetToList(ps.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Error finding available rooms by type", e);
        }
    }

    @Override
    public List<Room> findAvailableRoomsByTypeAndDateRange(RoomType roomType, LocalDate checkIn, LocalDate checkOut) {
        String sql = """
            SELECT r.* FROM rooms r
            WHERE r.room_type = ?::room_type
            AND r.is_available = TRUE
            ORDER BY r.room_number
            """;
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, roomType.name());
            return mapResultSetToList(ps.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Error finding available rooms by type and date range", e);
        }
    }

    @Override
    public void updateAvailability(Integer roomId, boolean isAvailable) {
        String sql = "UPDATE rooms SET is_available = ? WHERE id = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setBoolean(1, isAvailable);
            ps.setInt(2, roomId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating room availability", e);
        }
    }

    @Override
    public int countByTypeAndAvailability(RoomType roomType, boolean isAvailable) {
        String sql = "SELECT COUNT(*) FROM rooms WHERE room_type = ?::room_type AND is_available = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, roomType.name());
            ps.setBoolean(2, isAvailable);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting rooms", e);
        }
        return 0;
    }

    private List<Room> executeQuery(String sql) {
        try (Statement stmt = DataSourceConfig.getConnection().createStatement()) {
            return mapResultSetToList(stmt.executeQuery(sql));
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }
    }

    private List<Room> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        while (rs.next()) {
            rooms.add(mapRowToRoom(rs));
        }
        return rooms;
    }

    private Room mapRowToRoom(ResultSet rs) throws SQLException {
        return Room.builder()
                .id(rs.getInt("id"))
                .roomNumber(rs.getString("room_number"))
                .roomType(RoomType.valueOf(rs.getString("room_type")))
                .price(rs.getBigDecimal("price"))
                .isAvailable(rs.getBoolean("is_available"))
                .build();
    }
}
