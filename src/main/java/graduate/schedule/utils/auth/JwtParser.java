package graduate.schedule.utils.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graduate.schedule.common.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;


@Component
public class JwtParser {
    private static final String IDENTITY_TOKEN_SPLITER = "\\.";

    @Value("${secret.jwt-secret-key}")
    private String secretKey;
    private static final int HEADER_INDEX = 0;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Long getMemberIdFromToken(String accessToken) {
        try {
            return Long.parseLong(Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(accessToken)
                    .getBody().getSubject());
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(EXPIRED_TOKEN);
        } catch (UnsupportedJwtException | SignatureException | MalformedJwtException  e){
            throw new InvalidTokenException(MALFORMED_TOKEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }

    public Map<String, String> parseHeaders(String identityToken) {
        try {
            String encodeHeader = identityToken.split(IDENTITY_TOKEN_SPLITER)[HEADER_INDEX];
            String decodeHeader = new String(Base64.getDecoder().decode(encodeHeader));
            return objectMapper.readValue(decodeHeader, Map.class);
        } catch (JsonProcessingException | ArrayIndexOutOfBoundsException e) {
            throw new InvalidTokenException(UNSUPPORTED_ID_TOKEN_TYPE, e.getMessage());
        }
    }

    public Claims parsePublicKeyAndGetClaims(String idToken, PublicKey publicKey) {
        try {
            return Jwts.parser()
                    .setSigningKey(publicKey)
                    .parseClaimsJws(idToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException(EXPIRED_TOKEN);
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e){
            throw new InvalidTokenException(MALFORMED_TOKEN, e.getMessage());
        }
    }
}
