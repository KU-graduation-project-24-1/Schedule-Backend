package graduate.schedule.common.response.status;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum BaseExceptionResponseStatus implements ResponseStatus {
    /**
     * 1000: 요청 성공 (OK)
     */
    SUCCESS(1000, HttpStatus.OK.value(), "요청에 성공하였습니다."),
    ENTER_TO_STORE(1001, HttpStatus.OK.value(), "가게 참가에 성공하였습니다."),
    BUSINESS_CHECKED(1002, HttpStatus.OK.value(), "사업자 정보가 성공적으로 확인되었습니다."),
    SUCCESS_ADD_AVAILABLE_SCHEDULE(1003, HttpStatus.OK.value(), "근무 가능한 시간 정보를 등록하였습니다."),
    SUCCESS_DELETE_AVAILABLE_SCHEDULE(1004, HttpStatus.OK.value(), "근무 가능한 시간 정보를 삭제하였습니다."),
    SUCCESS_SAVE_MEMBER_NAME(1005, HttpStatus.OK.value(), "사용자 이름을 설정하였습니다."),
    SUCCESS_SET_MEMBER_GRADE(1006, HttpStatus.OK.value(), "고용 형태를 변경하였습니다."),
    SUCCESS_DELETE_STORE_MEMBER(1007, HttpStatus.OK.value(), "피고용인을 삭제하였습니다."),
    SUCCESS_DELETE_STORE(1008, HttpStatus.OK.value(), "가게를 삭제하였습니다."),
    SUCCESS_DELETE_SCHEDULE(1009, HttpStatus.OK.value(), "근무 정보를 삭제하였습니다."),
    SUCCESS_REQUEST_COVER(1010, HttpStatus.OK.value(), "대체 근무를 요청하였습니다."),

    /**
     * 2000: Request 오류 (BAD_REQUEST)
     */
    BAD_REQUEST(2000, HttpStatus.BAD_REQUEST.value(), "유효하지 않은 요청입니다."),
    URL_NOT_FOUND(2001, HttpStatus.BAD_REQUEST.value(), "유효하지 않은 URL 입니다."),
    METHOD_NOT_ALLOWED(2002, HttpStatus.METHOD_NOT_ALLOWED.value(), "해당 URL에서는 지원하지 않는 HTTP Method 입니다."),
    INAPPROPRIATE_DATA(2003, HttpStatus.BAD_REQUEST.value(), "입력한 정보가 올바르지 않습니다."),
    INVALID_INVITE_CODE(2004, HttpStatus.BAD_REQUEST.value(), "초대 코드가 유효하지 않습니다."),
    BUSINESS_CHECK_FAILED(2005, HttpStatus.BAD_REQUEST.value(), "사업자 정보가 올바르지 않습니다."),
    INVALID_MEMBER_GRADE(2006, HttpStatus.BAD_REQUEST.value(), "고용 형태가 올바르지 않습니다."),

    /**
     * 3000: Server, Database 오류 (INTERNAL_SERVER_ERROR)
     */
    SERVER_ERROR(3000, HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버에서 오류가 발생하였습니다."),
    DATABASE_ERROR(3001, HttpStatus.INTERNAL_SERVER_ERROR.value(), "데이터베이스에서 오류가 발생하였습니다."),

    /**
     * 4000: Authorization 오류
     */
    JWT_ERROR(4000, HttpStatus.UNAUTHORIZED.value(), "JWT에서 오류가 발생하였습니다."),
    TOKEN_NOT_FOUND(4001, HttpStatus.BAD_REQUEST.value(), "토큰이 HTTP Header에 없습니다."),
    UNSUPPORTED_TOKEN_TYPE(4002, HttpStatus.UNAUTHORIZED.value(), "지원되지 않는 토큰 형식입니다."),
    UNSUPPORTED_ID_TOKEN_TYPE(4003, HttpStatus.UNAUTHORIZED.value(), "OAuth Identity Token의 형식이 올바르지 않습니다."),
    INVALID_TOKEN(4007, HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다."),
    MALFORMED_TOKEN(4008, HttpStatus.UNAUTHORIZED.value(), "올바르지 않은 토큰입니다."),
    EXPIRED_TOKEN(4009, HttpStatus.UNAUTHORIZED.value(), "로그인 인증 유효 기간이 만료되었습니다."),
    TOKEN_MISMATCH(4010, HttpStatus.UNAUTHORIZED.value(), "로그인 정보가 토큰 정보와 일치하지 않습니다."),
    INVALID_CLAIMS(4011, HttpStatus.UNAUTHORIZED.value(), "OAuth Claims 값이 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(4012, HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 Refresh Token입니다."),
    IP_MISMATCH(4013, HttpStatus.UNAUTHORIZED.value(), "다른 IP에서 접속했습니다. 다시 로그인해주세요."),

    /**
     * 5000: 회원 정보 오류
     */
    NOT_FOUND_MEMBER(5000, HttpStatus.BAD_REQUEST.value(), "존재하지 않는 사용자입니다."),
    NOT_EXECUTIVE(5003, HttpStatus.BAD_REQUEST.value(), "가게 임원이 아닙니다.(권한 없음)"),
    NOT_STORE_MEMBER(5004, HttpStatus.BAD_REQUEST.value(), "가게 구성원이 아닙니다."),
    BOSS_NOT_EXIST(5004, HttpStatus.BAD_REQUEST.value(), "가게 사장이 존재하지 않습니다."),

    /**
     * 6000: Store 도메인 오류
     * */
    NOT_FOUND_STORE(6000, HttpStatus.BAD_REQUEST.value(), "존재하지 않는 가게입니다."),
    ALREADY_EXIST_STORE_MEMBER(6001, HttpStatus.BAD_REQUEST.value(), "이미 가게에 존재하는 사용자입니다."),
    EXPIRED_INVITE_CODE(6002, HttpStatus.BAD_REQUEST.value(), "초대 코드 유효 기간이 만료되었습니다."),
    ALREADY_EXIST_STORE(6003, HttpStatus.BAD_REQUEST.value(), "이미 존재하는 가게입니다."),

    /**
     * 7000: Store Schedule 오류
     * */
    NOT_FOUND_STORE_MEMBER_AVAILABLE_TIME(7000, HttpStatus.BAD_REQUEST.value(), "가능한 근무 시간 정보가 존재하지 않습니다."),
    NOT_MEMBER_WORKING_DATA(7001, HttpStatus.BAD_REQUEST.value(), "나의 근무 정보가 아닙니다."),
    NOT_FOUND_STORE_SCHEDULE(7002, HttpStatus.BAD_REQUEST.value(), "가게 근무 정보가 존재하지 않습니다."),
    NOT_SAME_SCHEDULE_EMPLOYEE(7003, HttpStatus.BAD_REQUEST.value(), "기존 근무자와 대체 근무 요청자가 일치하지 않습니다."),
    ALREADY_COVER_REQUESTED(7004, HttpStatus.BAD_REQUEST.value(), "이미 대체 근무 요청이 되어 있는 근무입니다."),

    /**
     * 8000: FCM 오류
     * */
    FCM_SEND_ERROR(8000, HttpStatus.INTERNAL_SERVER_ERROR.value(), "푸시 알림 전송 과정에서 오류가 발생하였습니다."),
    FCM_MAKE_MESSAGE_ERROR(8001, HttpStatus.INTERNAL_SERVER_ERROR.value(), "푸시 알림 메시지를 만드는 과정에서 오류가 발생하였습니다(JsonProcessingException)."),
    FCM_GET_ACCESS_TOKEN_ERROR(8002, HttpStatus.INTERNAL_SERVER_ERROR.value(), "FCM access token을 가져오는 과정에서 오류가 발생하였습니다.");


    private final int code;
    private final int status;
    private final String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
