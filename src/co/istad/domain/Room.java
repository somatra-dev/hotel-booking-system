package co.istad.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    private Integer id;

    private String roomNumber;

    private RoomType roomType;

    private BigDecimal price;

    private Boolean isAvailable;
}
