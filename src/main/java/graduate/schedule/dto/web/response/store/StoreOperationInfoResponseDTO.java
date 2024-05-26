package graduate.schedule.dto.web.response.store;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.time.DayOfWeek;

@Getter
@Setter
@AllArgsConstructor
public class StoreOperationInfoResponseDTO {
    private DayOfWeek dayOfWeek;
    private Long storeOperationInfoId;
    private Time startTime;
    private Time endTime;
    private int requiredEmployees;
}
