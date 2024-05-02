package graduate.schedule.common.exception;

import graduate.schedule.common.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class StoreException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public StoreException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}
