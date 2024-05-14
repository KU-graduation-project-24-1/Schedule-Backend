package graduate.schedule.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoMemberDTO {
    private String platformId;
    private String email;
    private String profileImg;

}
