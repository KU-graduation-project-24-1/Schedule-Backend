package graduate.schedule.dto.web.request;

import lombok.Getter;

@Getter
public class DeleteStoreMemberRequestDTO {
    private Long storeId;
    private Long employeeId;
}
