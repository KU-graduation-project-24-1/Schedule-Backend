package graduate.schedule.common.exception;

import graduate.schedule.common.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class StoreMemberTimeException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public StoreMemberTimeException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}
