package graduate.schedule.dto.web.request;

import lombok.Getter;

@Getter
public class DeleteAvailableScheduleRequestDTO {
    private Long memberId;
    private Long storeId;
    private Long storeMemberAvailableTimeId;
}
