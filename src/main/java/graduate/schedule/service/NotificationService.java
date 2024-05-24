package graduate.schedule.service;

import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.*;
import graduate.schedule.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.BOSS_NOT_EXIST;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final FCMService fcmService;
    private final StoreRepository storeRepository;
    private final MemberRepository memberRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final StoreScheduleRepository storeScheduleRepository;
    private final StoreAvailableScheduleRepository storeAvailableScheduleRepository;
    private final StoreAvailableTimeByDayRepository storeAvailableTimeByDayRepository;

    private final String NO_COVER_ACCEPT_TITLE = "대체 근무자가 존재하지 않습니다!";
    private final String NO_COVER_ACCEPT_BODY_START = "님이 대체 근무를 요청한 ";
    private final String COMMA = ", ";
    private final String WAVE = "~";
    private final String NO_COVER_ACCEPT = "에 대한 대체 근무자가 존재하지 않으며, ";
    private final String ONE_DAY_LEFT = "하루 남았습니다.";
    private final String TWO_DAY_LEFT = "이틀 남았습니다.";

    private final String PUT_NEXT_MONT_AVAILABLE_SCHEDULE_TITLE = "다음 달에 근무 가능한 시간을 입력하세요!";
    private final String YEAR = "년 ";
    private final String MONTH = "월";
    private final String PUT_AVAILABLE_SCHEDULE_BODY = "에 근무 가능한 시간을 입력하세요.";

    /**
     * 대체 근무 요청이 있는데, 수락이 없는 경우 - 매일 12시
     */
    @Scheduled(cron = "0 0 12 * * ?")
    protected void informNoOneAcceptCover() {
        log.info("informNoOneAcceptCover() 실행; 대체 근무 요청이 있는데, 수락이 없는 경우");
        LocalDate today = LocalDate.now();
        Date dayPlusOne = Date.valueOf(today.plusDays(1));
        Date dayPlusTwo = Date.valueOf(today.plusDays(2));

        List<StoreSchedule> oneDayLeftCoverRequestedSchedules = storeScheduleRepository.findSchedulesByRequestCoverAndDate(true, dayPlusOne);
        oneDayLeftCoverRequestedSchedules.forEach((schedule) -> {
            String ONE_DAY_LEFT_BODY = schedule.getEmployeeName() + NO_COVER_ACCEPT_BODY_START +
                    schedule.getDate() + COMMA + schedule.getStartTime() + WAVE + schedule.getEndTime() +
                    NO_COVER_ACCEPT + ONE_DAY_LEFT;

            log.info("[D-1] 대체 근무자 존재하지 않는 근무 정보 알림 전송: scheduleId {}에 메시지 {}", schedule.getId(), ONE_DAY_LEFT_BODY);
            sendNoOneAcceptCoverNotification(schedule, ONE_DAY_LEFT_BODY);

        });
        List<StoreSchedule> twoDayLeftCoverRequestedSchedules = storeScheduleRepository.findSchedulesByRequestCoverAndDate(true, dayPlusTwo);
        twoDayLeftCoverRequestedSchedules.forEach((schedule) -> {
            String TWO_DAY_LEFT_BODY = schedule.getEmployeeName() + NO_COVER_ACCEPT_BODY_START +
                    schedule.getDate() + COMMA + schedule.getStartTime() + WAVE + schedule.getEndTime() +
                    NO_COVER_ACCEPT + TWO_DAY_LEFT;

            log.info("[D-2] 대체 근무자 존재하지 않는 근무 정보 알림 전송: scheduleId {}에 메시지 {}", schedule.getId(), TWO_DAY_LEFT_BODY);
            sendNoOneAcceptCoverNotification(schedule, TWO_DAY_LEFT_BODY);
        });
    }

    private void sendNoOneAcceptCoverNotification(StoreSchedule schedule, String NO_COVER_ACCEPT_BODY) {
        Member employee = schedule.getEmployee();
        Member employer = storeMemberRepository.findByStoreAndMemberGrade(schedule.getStore(), StoreMemberGrade.BOSS)
                .orElseThrow(() -> new StoreMemberException(BOSS_NOT_EXIST))
                .getMember();

        log.info("대체 근무자 존재하지 않는 근무 정보에 알림 전송");
        fcmService.sendMessageTo(employee.getFcmToken(), this.NO_COVER_ACCEPT_TITLE, NO_COVER_ACCEPT_BODY);
        fcmService.sendMessageTo(employer.getFcmToken(), this.NO_COVER_ACCEPT_TITLE, NO_COVER_ACCEPT_BODY);
    }

    /**
     * 다음 달 가능 시간 입력 시작 알림 - 매월 8일 12시
     */
    @Scheduled(cron = "0 0 12 8 * ?")
    protected void informInputNextMonthAvailableScheduleOpen() {
        log.info("informInputNextMonthAvailableScheduleOpen() 실행; 다음달 가능 시간 입력 시작 알림");
        LocalDate nextMonthDate = LocalDate.now().plusMonths(1);
        int year = nextMonthDate.getYear();
        int nextMonth = nextMonthDate.getMonth().getValue();

        String PUT_NEXT_MONT_AVAILABLE_SCHEDULE_BODY = year + YEAR + nextMonth + MONTH + PUT_AVAILABLE_SCHEDULE_BODY;
        log.info("다음달 가능 시간 입력 시작 알림 전송 메시지: {}", PUT_NEXT_MONT_AVAILABLE_SCHEDULE_BODY);

        List<Member> allMember = memberRepository.findAll();
        allMember.forEach((member) -> fcmService.sendMessageTo(member.getFcmToken(), this.PUT_NEXT_MONT_AVAILABLE_SCHEDULE_TITLE, PUT_NEXT_MONT_AVAILABLE_SCHEDULE_BODY));
    }

    /**
     * 가능한 고정 시간 데이터 저장 - 매월 8일 00시
     */
    @Scheduled(cron = "0 0 8 * * ?")
    protected void saveAvailableSchedule() {
        log.info("saveAvailableSchedule() 실행; 가능한 고정 시간 데이터 저장");
        List<Store> allStore = storeRepository.findAll();
        allStore.forEach(store -> saveStoreAvailableSchedule(store));
    }

    private void saveStoreAvailableSchedule(Store store) {
        log.info("store {}의 가능한 고정 시간 데이터 저장", store.getId());

        //StoreMember 에서 BOSS가 아닌 멤버 찾기
        List<Member> storeEmployees = storeMemberRepository.findEmployees(store);

        //특정 달, 특정 요일의 모든 날짜 구하기
        Map<DayOfWeek, List<Date>> datesOnMonthByDayOfWeek = getDatesOnMonthByDayOfWeek();

        storeEmployees.forEach(employee ->
            Arrays.stream(DayOfWeek.values()).forEach(
                    dayOfWeek -> saveStoreAvailableScheduleAboutDayOfWeek(store, employee, dayOfWeek, datesOnMonthByDayOfWeek.get(dayOfWeek))
            )
        );
    }

    private Map<DayOfWeek, List<Date>> getDatesOnMonthByDayOfWeek() {
        Map<DayOfWeek, List<Date>> datesOnMonthByDayOfWeek = new HashMap<>();

        DayOfWeek[] dayOfWeeks = DayOfWeek.values();
        for (DayOfWeek dayOfWeek : dayOfWeeks) {
            List<Date> datesOfDay = new ArrayList<>();
            
            LocalDate nextMonth = LocalDate.now().plusMonths(1);
            YearMonth yearMonth = YearMonth.of(nextMonth.getYear(), nextMonth.getMonth());

            LocalDate date = yearMonth.atDay(1).with(TemporalAdjusters.firstInMonth(dayOfWeek));
            LocalDate lastDateOfMonth = yearMonth.atEndOfMonth();
            while (!date.isAfter(lastDateOfMonth)) {
                datesOfDay.add(Date.valueOf(date));
                date = date.plusWeeks(1);
            }

            datesOnMonthByDayOfWeek.put(dayOfWeek, datesOfDay);
        }

        return datesOnMonthByDayOfWeek;
    }

    private void saveStoreAvailableScheduleAboutDayOfWeek(Store store, Member employee, DayOfWeek dayOfWeek, List<Date> datesOnMonthByDayOfWeek) {
        log.info("가게 {}/ {}의 {}에 가능한 고정 시간 데이터 저장", store.getId(), employee.getName(), dayOfWeek);
        //해당 멤버의 요일별 가능한 시간 찾기
        List<StoreAvailableTimeByDay> availableTimesByDays = storeAvailableTimeByDayRepository.findByStoreAndMemberAndDayOfWeekOrderByStartTime(store, employee, dayOfWeek);

        //요일별 모든 가능한 시간에 대하여
        availableTimesByDays.forEach(availableTime ->
                //해당 요일의 모든 날짜 별 StoreAvailableSchedule 생성
                datesOnMonthByDayOfWeek.forEach(date -> {
                    log.info("가게 {}/ {}의 {}에 가능한 고정 시간 데이터 저장:  {}, {}~{}", store.getId(), employee.getName(), dayOfWeek, date, availableTime.getStartTime(), availableTime.getEndTime());
                    saveStoreAvailableScheduleInDay(store, employee, date, availableTime);
                })
        );
    }

    private void saveStoreAvailableScheduleInDay(Store store, Member employee, Date date, StoreAvailableTimeByDay availableTime) {
        StoreAvailableSchedule storeAvailableSchedule = StoreAvailableSchedule.createStoreAvailableSchedule(
                store,
                employee,
                date,
                availableTime.getStartTime(),
                availableTime.getEndTime()
        );
        storeAvailableScheduleRepository.save(storeAvailableSchedule);
    }
}
