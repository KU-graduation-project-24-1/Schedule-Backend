package graduate.schedule.service;

import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.common.exception.StoreScheduleException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.Store;
import graduate.schedule.domain.store.StoreMemberGrade;
import graduate.schedule.domain.store.StoreSchedule;
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
    private final FirebaseCloudMessageService firebaseCloudMessageService;

    private final String TITLE_CONTENT = "근무 가능한 시간에 대체 근무 요청이 있습니다!";
    private final String BODY_START_CONTENT = "님이 ";
    private final String BODY_COMMA = ", ";
    private final String BODY_WAVE = "~";
    private final String BODY_END_CONTENT = "에 대체 근무를 요청하였습니다.";

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

        String body = member.getName() + BODY_START_CONTENT + date.toString() + BODY_COMMA + timeWithoutSeconds(startTime) + BODY_WAVE + timeWithoutSeconds(endTime) + BODY_END_CONTENT;
        log.info("대체 근무 요청 메시지 body: {}", body);

        Store store = storeSchedule.getStore();
        Member employer = storeMemberRepository.findByStoreAndMemberGrade(store, StoreMemberGrade.BOSS)
                .orElseThrow(() -> new StoreMemberException(BOSS_NOT_EXIST))
                .getMember();
        log.info("send message to employer: {}", employer.getName());
        firebaseCloudMessageService.sendMessageTo(employer.getFcmToken(), TITLE_CONTENT, body);

        availableMemberFcmTokens.forEach(availableMember -> {
            log.info("send message to employee: {}", availableMember.getName());
            firebaseCloudMessageService.sendMessageTo(availableMember.getFcmToken(), TITLE_CONTENT, body);
        });
    }

}
