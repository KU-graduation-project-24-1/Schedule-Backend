package graduate.schedule.dto.store;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableTimeInDayDTO {
    private Long storeMemberAvailableTimeId;
    private String startTime;
    private String endTime;
}
