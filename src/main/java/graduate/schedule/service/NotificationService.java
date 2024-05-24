package graduate.schedule.service;

import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.StoreMemberGrade;
import graduate.schedule.domain.store.StoreSchedule;
import graduate.schedule.repository.StoreMemberRepository;
import graduate.schedule.repository.StoreScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.BOSS_NOT_EXIST;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final FCMService fcmService;
    private final StoreMemberRepository storeMemberRepository;
    private final StoreScheduleRepository storeScheduleRepository;

    private final String NO_COVER_ACCEPT_TITLE = "대체 근무자가 존재하지 않습니다!";
    private final String BODY_START = "님이 대체 근무를 요청한 ";
    private final String BODY_COMMA = ", ";
    private final String BODY_WAVE = "~";
    private final String NO_COVER_ACCEPT = "에 대한 대체 근무자가 존재하지 않으며, ";
    private final String ONE_DAY_LEFT = "하루 남았습니다.";
    private final String TWO_DAY_LEFT = "이틀 남았습니다.";

    /**
     * 대체 근무 요청이 있는데, 수락이 없는 경우 - 매일 12시
     * */
    @Scheduled(cron = "0 0 12 * * ?")
    protected void informNoOneAcceptCover(){
        log.info("informNoOneAcceptCover() 실행; 대체 근무 요청이 있는데, 수락이 없는 경우");
        LocalDateTime today = LocalDateTime.now();
        Date dayPlusOne = Date.valueOf(today.plusDays(1).toLocalDate());
        Date dayPlusTwo = Date.valueOf(today.plusDays(2).toLocalDate());

        List<StoreSchedule> oneDayLeftCoverRequestedSchedules = storeScheduleRepository.findSchedulesByRequestCoverAndDate(true, dayPlusOne);
        oneDayLeftCoverRequestedSchedules.forEach(
                (schedule) -> {
                    String ONE_DAY_LEFT_BODY = schedule.getEmployeeName() + BODY_START +
                            schedule.getDate() + BODY_COMMA + schedule.getStartTime() + BODY_WAVE + schedule.getEndTime() +
                            NO_COVER_ACCEPT + ONE_DAY_LEFT;

                    log.info("[D-1] 대체 근무자 존재하지 않는 근무 정보 알림 전송: scheduleId {}에 메시지 {}", schedule.getId(), ONE_DAY_LEFT_BODY);
                    sendNotification(schedule, ONE_DAY_LEFT_BODY);

                }
        );
        List<StoreSchedule> twoDayLeftCoverRequestedSchedules = storeScheduleRepository.findSchedulesByRequestCoverAndDate(true, dayPlusTwo);
        twoDayLeftCoverRequestedSchedules.forEach(
                (schedule) -> {
                    String TWO_DAY_LEFT_BODY = schedule.getEmployeeName() + BODY_START +
                            schedule.getDate() + BODY_COMMA + schedule.getStartTime() + BODY_WAVE + schedule.getEndTime() +
                            NO_COVER_ACCEPT + TWO_DAY_LEFT;

                    log.info("[D-2] 대체 근무자 존재하지 않는 근무 정보 알림 전송: scheduleId {}에 메시지 {}", schedule.getId(), TWO_DAY_LEFT_BODY);
                    sendNotification(schedule, TWO_DAY_LEFT_BODY);
                }
        );
    }

    private void sendNotification(StoreSchedule schedule, String body) {
        Member employee = schedule.getEmployee();
        Member employer = storeMemberRepository.findByStoreAndMemberGrade(schedule.getStore(), StoreMemberGrade.BOSS)
                .orElseThrow(() -> new StoreMemberException(BOSS_NOT_EXIST))
                .getMember();

        log.info("대체 근무자 존재하지 않는 근무 정보에 알림 전송");
        fcmService.sendMessageTo(employee.getFcmToken(), NO_COVER_ACCEPT_TITLE, body);
        fcmService.sendMessageTo(employer.getFcmToken(), NO_COVER_ACCEPT_TITLE, body);
    }

}
