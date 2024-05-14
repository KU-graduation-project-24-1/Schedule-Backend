package graduate.schedule.dto.web.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponseDTO {
    private String email;
    private String accessToken;
    private String refreshToken;
    private String imgUrl;
    private String memberName;
}
