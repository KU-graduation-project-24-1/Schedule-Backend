package graduate.schedule.dto.web.response.store;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@AllArgsConstructor
public class StoreAvailableTimeByDayResponseDTO {
    private List<DayOfWeek> dayOfWeeks;
    private List<String> startTimes;
    private List<String> endTimes;
}
