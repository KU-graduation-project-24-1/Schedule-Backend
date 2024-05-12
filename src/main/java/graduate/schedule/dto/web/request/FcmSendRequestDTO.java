package graduate.schedule.dto.web.request;

import lombok.Getter;

@Getter
public class FcmSendRequestDTO {
    private Long memberId;
    private String title;
    private String body;
}
