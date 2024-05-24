package graduate.schedule.dto.web.request.store;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
public class StoreOperationInfoRequestDTO {
    private DayOfWeek dayOfWeek;
    private int requiredEmployees;
    private String startTime;
    private String endTime;
}
