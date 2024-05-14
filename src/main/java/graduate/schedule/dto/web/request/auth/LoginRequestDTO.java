package graduate.schedule.dto.web.request.auth;

import lombok.Getter;

@Getter
public class LoginRequestDTO {
    private String idToken;
    private String fcmToken;
}