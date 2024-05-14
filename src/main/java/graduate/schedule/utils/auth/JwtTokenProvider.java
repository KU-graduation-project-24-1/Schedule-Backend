package graduate.schedule.utils.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {
    private static final long REFRESH_TOKEN_EXPIRED_IN = 15L * 24 * 60 * 60 * 1000; // 15일
    private final String secretKey;
    private final long validityInMilliseconds;
    private final JwtParser jwtParser;
    private final long ACCESS_TOKEN_EXPIRED_IN;

    public JwtTokenProvider(@Value("${secret.jwt-secret-key}") String secretKey,
                            @Value("${secret.jwt-expired-in}") long validityInMilliseconds,
                            @Value("${spring.access-token.expired-date}") Long accesstokenExpiredDate) {
        this.secretKey = secretKey;
        this.validityInMilliseconds = validityInMilliseconds;
        this.jwtParser = Jwts.parser().setSigningKey(secretKey);
        this.ACCESS_TOKEN_EXPIRED_IN = accesstokenExpiredDate;
    }

    public String createAccessToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + ACCESS_TOKEN_EXPIRED_IN);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + REFRESH_TOKEN_EXPIRED_IN);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}