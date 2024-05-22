package graduate.schedule.common.exception;

import graduate.schedule.common.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class FCMException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public FCMException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }

    public FCMException(ResponseStatus exceptionStatus, String message) {
        super(message);
        this.exceptionStatus = exceptionStatus;
    }
}
