package graduate.schedule.utils.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

@Component
public class AuthorizationExtractor {
    public static final String AUTHORIZATION = "Authorization";

    public String extract(HttpServletRequest httpRequest, String type) {
        Enumeration<String> headers = httpRequest.getHeaders(AUTHORIZATION);
        while (headers.hasMoreElements()) {
            String value = headers.nextElement();
            if (value.toLowerCase().startsWith(type.toLowerCase())) {
                return value.substring(type.length()).trim();
            }
        }

        return Strings.EMPTY;
    }
}