package graduate.schedule.dto.web.request.store;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
public class AddStoreAvailableTimeByDayRequestDTO {
    private Long storeId;
    private DayOfWeek dayOfWeek;
    private String startTime; // HH:mm
    private String endTime; // HH:mm
}
