package graduate.schedule.dto.web.request.store;

import lombok.Getter;

@Getter
public class DeleteAvailableScheduleRequestDTO {
    private Long storeId;
    private Long storeMemberAvailableTimeId;
}
