package co.istad.dao.impl;

import co.istad.config.DataSourceConfig;
import co.istad.dao.StaffDao;
import co.istad.domain.Staff;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class StaffDaoImpl implements StaffDao {

    @Override
    public Optional<Staff> findByEmail(String email) {
        String sql = "SELECT * FROM staff WHERE email = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToStaff(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding staff by email", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Staff> findByTelegramId(Long telegramId) {
        String sql = "SELECT * FROM staff WHERE telegram_id = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setLong(1, telegramId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToStaff(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding staff by telegram ID", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByTelegramId(Long telegramId) {
        String sql = "SELECT 1 FROM staff WHERE telegram_id = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setLong(1, telegramId);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking staff existence", e);
        }
    }

    @Override
    public void updateLastLogin(Integer staffId) {
        String sql = "UPDATE staff SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating last login", e);
        }
    }

    @Override
    public void updateTelegramId(Integer staffId, Long telegramId) {
        String sql = "UPDATE staff SET telegram_id = ? WHERE id = ?";
        try (PreparedStatement ps = DataSourceConfig.getConnection().prepareStatement(sql)) {
            ps.setLong(1, telegramId);
            ps.setInt(2, staffId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating telegram ID", e);
        }
    }

    private Staff mapRowToStaff(ResultSet rs) throws SQLException {
        return Staff.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .telegramId(rs.getObject("telegram_id", Long.class))
                .createdAt(rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .lastLogin(rs.getTimestamp("last_login") != null
                        ? rs.getTimestamp("last_login").toLocalDateTime() : null)
                .build();
    }
}
