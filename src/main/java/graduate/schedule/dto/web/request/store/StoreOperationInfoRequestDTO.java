package graduate.schedule.dto.web.request.store;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
public class StoreOperationInfoRequestDTO {
    private DayOfWeek dayOfWeek;
    private String startTime; // HH:mm 형식의 문자열
    private String endTime; // HH:mm 형식의 문자열
}
