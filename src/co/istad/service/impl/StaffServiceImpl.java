package co.istad.service.impl;

import co.istad.dao.StaffDao;
import co.istad.dao.impl.StaffDaoImpl;
import co.istad.domain.Staff;
import co.istad.dto.request.StaffLoginRequest;
import co.istad.dto.response.StaffLoginResponse;
import co.istad.service.StaffService;
import lombok.AllArgsConstructor;
import java.util.Optional;

@AllArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffDao staffDao;

    public StaffServiceImpl() {
        this.staffDao = new StaffDaoImpl();
    }

    @Override
    public StaffLoginResponse authenticate(StaffLoginRequest request) {
        Optional<Staff> staffOpt = staffDao.findByEmail(request.getEmail());

        if (staffOpt.isEmpty()) {
            return StaffLoginResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        }

        Staff staff = staffOpt.get();

        if (!request.getPassword().equals(staff.getPasswordHash())) {
            return StaffLoginResponse.builder()
                    .success(false)
                    .message("Invalid email or password")
                    .build();
        }

        // Update last login
        staffDao.updateLastLogin(staff.getId());

        // Link telegram account if provided
        if (request.getTelegramUserId() != null) {
            staffDao.updateTelegramId(staff.getId(), request.getTelegramUserId());
        }

        return StaffLoginResponse.builder()
                .success(true)
                .staffName(staff.getName())
                .message("Login successful")
                .build();
    }

    @Override
    public boolean isStaffMember(Long telegramId) {
        return staffDao.existsByTelegramId(telegramId);
    }
}
