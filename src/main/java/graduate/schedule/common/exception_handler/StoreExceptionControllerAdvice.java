package graduate.schedule.common.exception_handler;

import graduate.schedule.common.exception.StoreException;
import graduate.schedule.common.response.BaseErrorResponse;
import jakarta.annotation.Priority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Priority(0)
@RestControllerAdvice
public class StoreExceptionControllerAdvice {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(StoreException.class)
    public BaseErrorResponse handel_MemberException(StoreException e) {
        log.error("[handle_StoreException]", e);
        return new BaseErrorResponse(e.getExceptionStatus(), e.getMessage());
    }
}