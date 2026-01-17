package co.istad.telegram.session;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StaffSession {

    private String staffName;
    private LocalDateTime loginTime;
}
