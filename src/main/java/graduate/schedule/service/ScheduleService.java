package graduate.schedule.service;

import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.common.exception.StoreScheduleException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMember;
import graduate.schedule.domain.store.StoreMemberGrade;
import graduate.schedule.domain.store.StoreSchedule;
import graduate.schedule.dto.web.response.executive.ChangeScheduleResponseDTO;
import graduate.schedule.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;
import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.NOT_STORE_MEMBER;
import static graduate.schedule.utils.DateAndTimeFormatter.timeWithoutSeconds;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleService {
    private final StoreScheduleRepository storeScheduleRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final StoreAvailableScheduleRepository storeAvailableScheduleRepository;
    private final FCMService fcmService;

    private final String REQUEST_COVER_TITLE = "근무 가능한 시간에 대체 근무 요청이 있습니다!";
    private final String ACCEPT_COVER_TITLE = "대체 근무 요청이 수락되었습니다!";

    private final String BODY_START = "님이 ";
    private final String BODY_COMMA = ", ";
    private final String BODY_WAVE = "~";
    private final String REQUEST_COVER_BODY_END = "에 대체 근무를 요청하였습니다.";
    private final String ACCEPT_COVER_BODY_END = "에 대체 근무를 수락하였습니다.";

    public void requestCover(Member member, Long scheduleId) {
        StoreSchedule storeSchedule = storeScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new StoreScheduleException(NOT_FOUND_STORE_SCHEDULE));
        storeMemberRepository.findByStoreAndMember(storeSchedule.getStore(), member)
                .orElseThrow(() -> new StoreMemberException(NOT_STORE_MEMBER));
        if (!storeSchedule.getEmployee().equals(member)) {
            throw new StoreScheduleException(NOT_SAME_SCHEDULE_EMPLOYEE);
        }

        if (storeSchedule.isRequestCover()) {
            throw new StoreScheduleException(ALREADY_COVER_REQUESTED);
        }
        storeSchedule.setRequestCover(true);

        sendRequestCoverMessage(member, storeSchedule);

    }

    private void sendRequestCoverMessage(Member member, StoreSchedule storeSchedule) {
        Date date = storeSchedule.getDate();
        Time startTime = storeSchedule.getStartTime();
        Time endTime = storeSchedule.getEndTime();
        List<Member> availableMemberFcmTokens = storeAvailableScheduleRepository.findMembersByStoreAndDateAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
                storeSchedule.getStore(),
                date,
                startTime,
                endTime);
        log.info("대체 근무 요청 푸시 알림 전송 size: {}", availableMemberFcmTokens.size());

        String requestCoverBody = member.getName() + BODY_START +
                date.toString() + BODY_COMMA +
                timeWithoutSeconds(startTime) + BODY_WAVE + timeWithoutSeconds(endTime) +
                REQUEST_COVER_BODY_END;
        log.info("대체 근무 요청 메시지 requestCoverBody: {}", requestCoverBody);

        Store store = storeSchedule.getStore();
        Member employer = storeMemberRepository.findByStoreAndMemberGrade(store, StoreMemberGrade.BOSS)
                .orElseThrow(() -> new StoreMemberException(BOSS_NOT_EXIST))
                .getMember();
        log.info("send message to employer: {}", employer.getName());
        fcmService.sendMessageTo(employer.getFcmToken(), REQUEST_COVER_TITLE, requestCoverBody);

        availableMemberFcmTokens.forEach(availableMember -> {
            log.info("send message to employee: {}", availableMember.getName());
            fcmService.sendMessageTo(availableMember.getFcmToken(), REQUEST_COVER_TITLE, requestCoverBody);
        });
    }

    public ChangeScheduleResponseDTO acceptCover(Member substitute, Long scheduleId) {
        StoreSchedule storeSchedule = storeScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new StoreScheduleException(NOT_FOUND_STORE_SCHEDULE));
        StoreMember storeMember = storeMemberRepository.findByStoreAndMember(storeSchedule.getStore(), substitute)
                .orElseThrow(() -> new StoreMemberException(NOT_STORE_MEMBER));
        Member employer = storeMemberRepository.findByStoreAndMemberGrade(storeSchedule.getStore(), StoreMemberGrade.BOSS)
                .orElseThrow(() -> new StoreMemberException(BOSS_NOT_EXIST))
                .getMember();

        //대체 근무자와 대체 근무 요청자가 동일한 경우
        if (substitute == storeSchedule.getEmployee()) {
            throw new StoreScheduleException(SUBSTITUTE_SAME_AS_PREVIOUS_EMPLOYEE);
        }

        //대체 근무자가 고용인인 경우 해당 근무 정보 삭제
        if (storeMember.getMember().equals(employer)) {
            log.info("대체 근무자가 고용인으로 설정되어 근무 정보를 삭제합니다.");
            storeScheduleRepository.delete(storeSchedule);
            return new ChangeScheduleResponseDTO();
        }

        Member previousEmployee = storeSchedule.getEmployee();
        storeSchedule.setEmployee(substitute);
        storeSchedule.setRequestCover(false);

        //대체 근무 요청자, 대체 근무자, 고용인에게 푸시 알림
        String acceptCoverBody = substitute.getName() + BODY_START +
                storeSchedule.getDate() + BODY_COMMA +
                timeWithoutSeconds(storeSchedule.getStartTime()) + BODY_WAVE + timeWithoutSeconds(storeSchedule.getEndTime()) +
                ACCEPT_COVER_BODY_END;
        log.info("대체 근무 수락 메시지 acceptCoverBody: {}", acceptCoverBody);

        fcmService.sendMessageTo(previousEmployee.getFcmToken(), ACCEPT_COVER_TITLE, acceptCoverBody);
        fcmService.sendMessageTo(substitute.getFcmToken(), ACCEPT_COVER_TITLE, acceptCoverBody);
        fcmService.sendMessageTo(employer.getFcmToken(), ACCEPT_COVER_TITLE, acceptCoverBody);

        return new ChangeScheduleResponseDTO(storeSchedule);
    }
}
