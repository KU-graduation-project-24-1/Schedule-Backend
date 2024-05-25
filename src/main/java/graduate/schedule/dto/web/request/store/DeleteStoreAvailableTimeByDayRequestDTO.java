package graduate.schedule.dto.web.request.store;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
public class DeleteStoreAvailableTimeByDayRequestDTO {
    private Long storeAvailableTimeByDayId;
}
