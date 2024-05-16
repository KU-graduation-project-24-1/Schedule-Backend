package graduate.schedule.dto.web.request;

import lombok.Getter;

@Getter
public class ChangeWorkerRequestDTO {
    private Long scheduleId;
    private Long employeeId;
}
