package graduate.schedule.auth.kakao;

import graduate.schedule.auth.OneulAlbaPublicKey;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KakaoPublicKeys {
    private List<OneulAlbaPublicKey> keys;

    public OneulAlbaPublicKey getMatchesKey(String kid) {
        return this.keys.stream()
                .filter(o -> o.getKid().equals(kid))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Kakao JWT 값의 kid 정보가 올바르지 않습니다."));
    }
}
