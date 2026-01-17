package co.istad.dto.response;

import co.istad.domain.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Integer id;

    private String roomNumber;

    private RoomType roomType;

    private BigDecimal price;

    private Boolean isAvailable;
}
