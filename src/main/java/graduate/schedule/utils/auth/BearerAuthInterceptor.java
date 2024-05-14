package graduate.schedule.utils.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class BearerAuthInterceptor implements HandlerInterceptor {
    private AuthorizationExtractor authExtractor;
    private JwtParser jwtParser;

    public BearerAuthInterceptor(AuthorizationExtractor authExtractor, JwtParser jwtParser) {
        this.authExtractor = authExtractor;
        this.jwtParser = jwtParser;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpRequest,
                             HttpServletResponse httpResponse, Object handler) {
        log.debug("interceptor.preHandle 호출");
        String token = authExtractor.extract(httpRequest, "Bearer");
        httpRequest.setAttribute("token", token);
        log.debug("Token: {}", token);

        if (token == null || token.length() == 0) {
            return true;
        }

        Long memberId = jwtParser.getMemberIdFromToken(token);
        log.debug("memberId: {}", memberId);
        httpRequest.setAttribute("memberId", memberId);

        return true;
    }
}