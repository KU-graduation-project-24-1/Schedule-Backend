package graduate.schedule.dto.web.request;

import lombok.Getter;

@Getter
public class RegenerateInviteCodeRequestDTO {
    private Long memberId;
    private Long storeId;
}
