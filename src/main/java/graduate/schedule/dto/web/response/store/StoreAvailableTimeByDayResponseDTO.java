package graduate.schedule.dto.web.response.store;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class StoreAvailableTimeByDayResponseDTO {
    private List<Long> availableTimeByDayId;
    private List<DayOfWeek> dayOfWeeks;
    private List<String> startTimes;
    private List<String> endTimes;
}
