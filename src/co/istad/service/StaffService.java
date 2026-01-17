package co.istad.service;

import co.istad.dto.request.StaffLoginRequest;
import co.istad.dto.response.StaffLoginResponse;

public interface StaffService {

    StaffLoginResponse authenticate(StaffLoginRequest request);

    boolean isStaffMember(Long telegramId);
}
