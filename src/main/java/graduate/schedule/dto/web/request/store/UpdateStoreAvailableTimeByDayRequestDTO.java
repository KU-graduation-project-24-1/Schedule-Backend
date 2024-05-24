package graduate.schedule.dto.web.request.store;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
public class UpdateStoreAvailableTimeByDayRequestDTO {
    private DayOfWeek dayOfWeek;
    private String startTime;
    private String endTime;
}