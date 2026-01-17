package co.istad.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff {

    private Integer id;

    private String name;

    private String email;

    private String passwordHash;

    private Long telegramId;

    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;
}
