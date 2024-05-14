package graduate.schedule.dto.web.response.store;

import graduate.schedule.dto.store.WorkScheduleOnDayDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WorkScheduleOnMonthResponseDTO {
    private List<WorkScheduleOnDayDTO> daySchedules;
}
