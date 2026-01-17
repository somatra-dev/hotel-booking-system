package co.istad.dao;

import co.istad.domain.Staff;

import java.util.Optional;

public interface StaffDao {

    Optional<Staff> findByEmail(String email);

    Optional<Staff> findByTelegramId(Long telegramId);

    boolean existsByTelegramId(Long telegramId);

    void updateLastLogin(Integer staffId);

    void updateTelegramId(Integer staffId, Long telegramId);
}
