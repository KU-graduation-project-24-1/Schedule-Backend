package graduate.schedule.auth.kakao;

import graduate.schedule.common.exception.InvalidTokenException;
import graduate.schedule.dto.auth.KakaoMemberDTO;
import graduate.schedule.utils.auth.JwtParser;
import graduate.schedule.utils.auth.PublicKeyGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;

import java.security.PublicKey;
import java.util.Map;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.INVALID_CLAIMS;


@Component
@RequiredArgsConstructor
public class KakaoMemberProvider {
    private final JwtParser jwtParser;
    private final PublicKeyGenerator publicKeyGenerator;

    private final KakaoClient kakaoClient;
    @Value("${oauth.kakao.iss}")
    private String iss;

    @Value("${oauth.kakao.client-id}")
    private String clientId;

    public KakaoMemberDTO getPayloadFromIdToken(String identityToken) {
        Map<String, String> headers = jwtParser.parseHeaders(identityToken);
        KakaoPublicKeys kakaoPublicKeys = kakaoClient.getKakaoOIDCOpenKeys();
        PublicKey publicKey = publicKeyGenerator.generateKakaoPublicKey(headers, kakaoPublicKeys);

        Claims claims = jwtParser.parsePublicKeyAndGetClaims(identityToken, publicKey);
        validateClaims(claims);

        return new KakaoMemberDTO(claims.getSubject(), claims.get("email", String.class), claims.get("picture", String.class));
    }
    private void validateClaims(Claims claims) {
        if (!claims.getIssuer().contains(iss) && claims.getAudience().equals(clientId)) {
            throw new InvalidTokenException(INVALID_CLAIMS);
        }
    }
}