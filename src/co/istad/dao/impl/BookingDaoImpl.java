package co.istad.dao.impl;

import co.istad.config.DataSourceConfig;
import co.istad.dao.BookingDao;
import co.istad.domain.Booking;
import co.istad.domain.BookingStatus;
import co.istad.domain.Room;
import co.istad.domain.RoomType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingDaoImpl implements BookingDao {

    @Override
    public Booking save(Booking booking) {
        String sql = """
            INSERT INTO bookings (guest_name, guest_email, guest_phone, room_id,
                                  check_in_date, check_out_date, total_price, status, telegram_chat_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?::booking_status, ?)
            RETURNING id, created_at
            """;
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, booking.getGuestName());
            ps.setString(2, booking.getGuestEmail());
            ps.setString(3, booking.getGuestPhone());
            ps.setInt(4, booking.getRoomId());
            ps.setDate(5, Date.valueOf(booking.getCheckInDate()));
            ps.setDate(6, Date.valueOf(booking.getCheckOutDate()));
            ps.setBigDecimal(7, booking.getTotalPrice());
            ps.setString(8, booking.getStatus().name());
            ps.setLong(9, booking.getTelegramChatId());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                booking.setId(rs.getInt("id"));
                booking.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            return booking;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving booking", e);
        }
    }

    @Override
    public Optional<Booking> findById(Integer id) {
        String sql = """
            SELECT b.*, r.room_number, r.room_type, r.price, r.is_available
            FROM bookings b
            LEFT JOIN rooms r ON b.room_id = r.id
            WHERE b.id = ?
            """;
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToBookingWithRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding booking by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Booking> findByGuestPhone(String phone) {
        String sql = """
            SELECT b.*, r.room_number, r.room_type, r.price, r.is_available
            FROM bookings b
            LEFT JOIN rooms r ON b.room_id = r.id
            WHERE b.guest_phone = ?
            ORDER BY b.check_in_date DESC
            """;
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, phone);
            return mapResultSetToList(ps.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Error finding bookings by phone", e);
        }
    }

    @Override
    public List<Booking> findTodayCheckIns() {
        String sql = """
            SELECT b.*, r.room_number, r.room_type, r.price, r.is_available
            FROM bookings b
            LEFT JOIN rooms r ON b.room_id = r.id
            WHERE b.check_in_date = CURRENT_DATE
            AND b.status = 'CONFIRMED'
            ORDER BY b.id
            """;
        return executeQuery(sql);
    }

    @Override
    public List<Booking> findTodayCheckOuts() {
        String sql = """
            SELECT b.*, r.room_number, r.room_type, r.price, r.is_available
            FROM bookings b
            LEFT JOIN rooms r ON b.room_id = r.id
            WHERE b.check_out_date = CURRENT_DATE
            AND b.status = 'CONFIRMED'
            ORDER BY b.id
            """;
        return executeQuery(sql);
    }

    @Override
    public List<Booking> searchByGuestNameOrPhone(String query) {
        String sql = """
            SELECT b.*, r.room_number, r.room_type, r.price, r.is_available
            FROM bookings b
            LEFT JOIN rooms r ON b.room_id = r.id
            WHERE LOWER(b.guest_name) LIKE LOWER(?)
            OR b.guest_phone LIKE ?
            ORDER BY b.created_at DESC
            LIMIT 20
            """;
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            return mapResultSetToList(ps.executeQuery());
        } catch (SQLException e) {
            throw new RuntimeException("Error searching bookings", e);
        }
    }

    @Override
    public void updateStatus(Integer bookingId, BookingStatus status) {
        String sql = "UPDATE bookings SET status = ?::booking_status WHERE id = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating booking status", e);
        }
    }

    private List<Booking> executeQuery(String sql) {
        try (Statement stmt = DataSourceConfig.getConnection().createStatement()) {
            return mapResultSetToList(stmt.executeQuery(sql));
        } catch (SQLException e) {
            throw new RuntimeException("Error executing query", e);
        }
    }

    private List<Booking> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        while (rs.next()) {
            bookings.add(mapRowToBookingWithRoom(rs));
        }
        return bookings;
    }

    private Booking mapRowToBookingWithRoom(ResultSet rs) throws SQLException {
        Room room = null;
        if (rs.getObject("room_id") != null) {
            room = Room.builder()
                    .id(rs.getInt("room_id"))
                    .roomNumber(rs.getString("room_number"))
                    .roomType(RoomType.valueOf(rs.getString("room_type")))
                    .price(rs.getBigDecimal("price"))
                    .isAvailable(rs.getBoolean("is_available"))
                    .build();
        }

        return Booking.builder()
                .id(rs.getInt("id"))
                .guestName(rs.getString("guest_name"))
                .guestEmail(rs.getString("guest_email"))
                .guestPhone(rs.getString("guest_phone"))
                .roomId(rs.getObject("room_id", Integer.class))
                .room(room)
                .checkInDate(rs.getDate("check_in_date").toLocalDate())
                .checkOutDate(rs.getDate("check_out_date").toLocalDate())
                .totalPrice(rs.getBigDecimal("total_price"))
                .status(BookingStatus.valueOf(rs.getString("status")))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .telegramChatId(rs.getLong("telegram_chat_id"))
                .build();
    }
}
