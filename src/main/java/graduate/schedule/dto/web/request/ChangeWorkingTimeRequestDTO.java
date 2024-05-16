package graduate.schedule.dto.web.request;

import lombok.Getter;

@Getter
public class ChangeWorkingTimeRequestDTO {
    private Long scheduleId;
    private String startTime; //HH:mm
    private String endTime; //HH:mm
}
