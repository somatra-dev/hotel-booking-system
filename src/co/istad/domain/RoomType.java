package co.istad.domain;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum RoomType {

    SINGLE(new BigDecimal("40.00")),
    DOUBLE(new BigDecimal("60.00")),
    FAMILY(new BigDecimal("100.00")),
    VIP(new BigDecimal("200.00"));

    private final BigDecimal price;

    RoomType(BigDecimal price) {
        this.price = price;
    }

    public static RoomType fromString(String type) {
        return valueOf(type.toUpperCase());
    }
}
