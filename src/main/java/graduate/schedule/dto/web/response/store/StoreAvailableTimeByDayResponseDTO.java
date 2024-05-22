package graduate.schedule.dto.web.response.store;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;

@Getter
@AllArgsConstructor
public class StoreAvailableTimeByDayResponseDTO {
    private DayOfWeek dayOfWeek;
    private String startTime;
    private String endTime;
}