package graduate.schedule.dto.web.request;

import lombok.Getter;

import java.sql.Date;

@Getter
public class CreateScheduleRequestDTO {
    private Long storeId;
    private Date date;
    private Long employeeId;
    private String startTime; //HH:mm
    private String endTime; //HH:mm
}
