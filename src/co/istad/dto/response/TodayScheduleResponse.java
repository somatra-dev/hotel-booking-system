package co.istad.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodayScheduleResponse {

    private List<BookingResponse> checkIns;

    private List<BookingResponse> checkOuts;

    private int totalCheckIns;

    private int totalCheckOuts;
}
