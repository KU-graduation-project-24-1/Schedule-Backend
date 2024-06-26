package graduate.schedule.dto.web.response.store;

import graduate.schedule.dto.store.AvailableScheduleInDayDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AvailableScheduleOnMonthResponseDTO {
    private String memberGrade;
    private List<AvailableScheduleInDayDTO> dailyAvailableSchehdules;
}
