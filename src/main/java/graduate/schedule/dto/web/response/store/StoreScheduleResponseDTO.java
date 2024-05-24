package graduate.schedule.dto.web.response.store;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StoreScheduleResponseDTO {
    private int day;
    private List<List<List<Integer>>> schedules;
}
