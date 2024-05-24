package graduate.schedule.service;

import graduate.schedule.common.exception.MemberException;
import graduate.schedule.common.exception.StoreException;
import graduate.schedule.common.exception.StoreMemberException;
import graduate.schedule.common.exception.StoreScheduleException;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.*;
import graduate.schedule.dto.web.request.store.*;
import graduate.schedule.dto.web.response.executive.ChangeScheduleResponseDTO;
import graduate.schedule.dto.web.response.store.AddAvailableScheduleResponseDTO;
import graduate.schedule.dto.web.response.store.AddStoreOperationInfoResponseDTO;
import graduate.schedule.dto.web.response.store.StoreScheduleResponseDTO;
import graduate.schedule.dto.web.response.store.AddAvailableTimeByDayResponseDTO;
import graduate.schedule.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;
import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.NOT_STORE_MEMBER;
import static graduate.schedule.utils.DateAndTimeFormatter.timeWithSeconds;
import static graduate.schedule.utils.DateAndTimeFormatter.timeWithoutSeconds;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StoreScheduleService {
    private final FCMService fcmService;

    private final StoreRepository storeRepository;
    private final StoreScheduleRepository storeScheduleRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final StoreAvailableScheduleRepository storeAvailableScheduleRepository;
    private final StoreAvailableTimeByDayRepository storeAvailableTimeByDayRepository;
    private final StoreOperationInfoRepository storeOperationInfoRepository;
    private final MemberRepository memberRepository;

    private final String REQUEST_COVER_TITLE = "근무 가능한 시간에 대체 근무 요청이 있습니다!";
    private final String ACCEPT_COVER_TITLE = "대체 근무 요청이 수락되었습니다!";

    private final String BODY_START = "님이 ";
    private final String BODY_COMMA = ", ";
    private final String BODY_WAVE = "~";
    private final String REQUEST_COVER_BODY_END = "에 대체 근무를 요청하였습니다.";
    private final String ACCEPT_COVER_BODY_END = "에 대체 근무를 수락하였습니다.";


    public AddAvailableScheduleResponseDTO addAvailableScheduleInDay(Member member, AddAvailableScheduleRequestDTO storeRequest) {
        Store store = storeRepository.findById(storeRequest.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.existsMember(member, store)) {
            throw new StoreMemberException(NOT_STORE_MEMBER);
        }

        LocalDate previousMonthOfRequestDate = storeRequest.getDate().toLocalDate().minusMonths(1);
        LocalDate previousMonth8thDateOfRequestDate = LocalDate.of(previousMonthOfRequestDate.getYear(), previousMonthOfRequestDate.getMonth(), 8);
        if (previousMonth8thDateOfRequestDate.isAfter(LocalDate.now())) {
            throw new StoreScheduleException(NOT_ADDING_SCHEDULE_TERM);
        }

        StoreAvailableSchedule newStoreAvailableSchedule =
                StoreAvailableSchedule.createStoreAvailableSchedule(
                        store,
                        member,
                        storeRequest.getDate(),
                        timeWithSeconds(storeRequest.getStartTime()),
                        timeWithSeconds(storeRequest.getEndTime())
                );
        storeAvailableScheduleRepository.save(newStoreAvailableSchedule);
        return new AddAvailableScheduleResponseDTO(newStoreAvailableSchedule.getId());

    }

    public void deleteAvailableScheduleInDay(Member member, DeleteAvailableScheduleRequestDTO storeRequest) {
        Store store = storeRepository.findById(storeRequest.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.existsMember(member, store)) {
            throw new StoreMemberException(NOT_STORE_MEMBER);
        }

        StoreAvailableSchedule availableTime = storeAvailableScheduleRepository.findById(storeRequest.getStoreAvailableScheduleId())
                .orElseThrow(() -> new StoreScheduleException(NOT_FOUND_STORE_MEMBER_AVAILABLE_TIME));
        if (!availableTime.getEmployee().equals(member)) {
            throw new StoreScheduleException(NOT_MEMBER_WORKING_DATA);
        }
        storeAvailableScheduleRepository.delete(availableTime);
    }

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

    // 주 단위 고정 근무시간 수정하기
    public AddAvailableTimeByDayResponseDTO addStoreAvailableTimeByDay(Member member, AddStoreAvailableTimeByDayRequestDTO request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.existsMember(member, store)) {
            throw new StoreMemberException(NOT_STORE_MEMBER);
        }
        DayOfWeek dayOfWeek = request.getDayOfWeek();
        Time newStartTime = Time.valueOf(request.getStartTime());
        Time newEndTime = Time.valueOf(request.getEndTime());

        // 기존 요일에 해당하는 데이터와 병합
        List<StoreAvailableTimeByDay> existingSchedules = storeAvailableTimeByDayRepository.findByStoreAndMemberAndDayOfWeekOrderByStartTime(store, member, dayOfWeek);

        for (StoreAvailableTimeByDay schedule : existingSchedules) {
            if (newStartTime.before(schedule.getEndTime()) && newEndTime.after(schedule.getStartTime())) {
                newStartTime = new Time(Math.min(newStartTime.getTime(), schedule.getStartTime().getTime()));
                newEndTime = new Time(Math.max(newEndTime.getTime(), schedule.getEndTime().getTime()));
                storeAvailableTimeByDayRepository.delete(schedule);
            }
        }

        StoreAvailableTimeByDay newStoreAvailableTimeByDay =
                StoreAvailableTimeByDay.createStoreAvailableTimeByDay(
                        store,
                        member,
                        dayOfWeek,
                        newStartTime,
                        newEndTime
                );
        storeAvailableTimeByDayRepository.save(newStoreAvailableTimeByDay);
        return new AddAvailableTimeByDayResponseDTO(newStoreAvailableTimeByDay.getId());
    }


    public void deleteStoreAvailableTimeByDay(Member member, DeleteStoreAvailableTimeByDayRequestDTO request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.existsMember(member, store)) {
            throw new StoreMemberException(NOT_STORE_MEMBER);
        }

        StoreAvailableTimeByDay schedule = storeAvailableTimeByDayRepository.findById(request.getStoreAvailableTimeByDayId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE_MEMBER_AVAILABLE_TIME));

        Time deleteStartTime = schedule.getStartTime();
        Time deleteEndTime = schedule.getEndTime();

        List<StoreAvailableTimeByDay> existingSchedules = storeAvailableTimeByDayRepository.findByStoreAndMemberAndDayOfWeekOrderByStartTime(store, member, schedule.getDayOfWeek());

        for (StoreAvailableTimeByDay existingSchedule : existingSchedules) {
            if (existingSchedule.getStartTime().before(deleteEndTime) && existingSchedule.getEndTime().after(deleteStartTime)) {
                if (existingSchedule.getStartTime().before(deleteStartTime) && existingSchedule.getEndTime().after(deleteEndTime)) {
                    // 기존 스케줄이 삭제할 시간 범위를 포함하는 경우, 두 개로 나눔
                    Time originalEndTime = existingSchedule.getEndTime();
                    existingSchedule.updateWorkTime(existingSchedule.getStartTime(), deleteStartTime);
                    storeAvailableTimeByDayRepository.save(existingSchedule);

                    StoreAvailableTimeByDay newSchedule = new StoreAvailableTimeByDay(store, member, schedule.getDayOfWeek(), deleteEndTime, originalEndTime);
                    storeAvailableTimeByDayRepository.save(newSchedule);
                } else if (existingSchedule.getStartTime().before(deleteStartTime)) {
                    // 삭제 범위가 기존 스케줄 끝 부분에 걸치는 경우
                    existingSchedule.updateWorkTime(existingSchedule.getStartTime(), deleteStartTime);
                    storeAvailableTimeByDayRepository.save(existingSchedule);
                } else if (existingSchedule.getEndTime().after(deleteEndTime)) {
                    // 삭제 범위가 기존 스케줄 시작 부분에 걸치는 경우
                    existingSchedule.updateWorkTime(deleteEndTime, existingSchedule.getEndTime());
                    storeAvailableTimeByDayRepository.save(existingSchedule);
                } else {
                    // 삭제 범위가 기존 스케줄 전체를 포함하는 경우
                    storeAvailableTimeByDayRepository.delete(existingSchedule);
                }
            }
        }
    }




    // 가게 근무시간 설정하기
    public AddStoreOperationInfoResponseDTO addStoreOperationInfo(Member member, Long storeId, StoreOperationInfoRequestDTO request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));

        // 사용자가 고용인인지 확인
        boolean isEmployee = storeMemberRepository.findByStoreAndMemberGrade(store, StoreMemberGrade.BOSS)
                .filter(storeMember -> storeMember.getMember().equals(member))
                .isPresent();
        if (!isEmployee) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        DayOfWeek dayOfWeek = request.getDayOfWeek();
        int requiredEmployees = request.getRequiredEmployees();
        Time startTime = Time.valueOf(request.getStartTime() + ":00");
        Time endTime = Time.valueOf(request.getEndTime() + ":00");

        StoreOperationInfo operationInfo = new StoreOperationInfo(store, dayOfWeek, requiredEmployees, startTime, endTime);

        StoreOperationInfo savedOperationInfo = storeOperationInfoRepository.save(operationInfo);

        return new AddStoreOperationInfoResponseDTO(savedOperationInfo.getId());
    }

    public void deleteStoreOperationInfo(Member member, DeleteStoreOperationInfoRequestDTO request) {
        StoreOperationInfo operationInfo = storeOperationInfoRepository.findById(request.getStoreOperationInfoId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE_OPERATION_INFO));

        Store store = operationInfo.getStore();

        // 사용자가 고용인인지 확인
        boolean isEmployee = storeMemberRepository.findByStoreAndMemberGrade(store, StoreMemberGrade.BOSS)
                .filter(storeMember -> storeMember.getMember().equals(member))
                .isPresent();
        if (!isEmployee) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        storeOperationInfoRepository.delete(operationInfo);
    }

    public StoreScheduleResponseDTO generateSchedule(Long storeId, List<Integer> m, List<Integer> k, List<List<List<Integer>>> preferences) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        int N = preferences.size();
        int days = m.size();

        // 결과 저장을 위한 배열 (각 날짜별로 시간대에 배정된 사람들의 리스트)
        List<List<List<Integer>>> schedules = new ArrayList<>();
        for (int day = 0; day < days; day++) {
            List<List<Integer>> schedule = new ArrayList<>();
            for (int i = 0; i < m.get(day); i++) {
                schedule.add(new ArrayList<>());
            }
            schedules.add(schedule);
        }

        // 각 사람의 총 근무 시간을 추적하는 배열
        int[] workHours = new int[N];

        // 각 날짜에 대한 스케줄링
        for (int day = 0; day < days; day++) {
            List<List<Integer>> schedule = schedules.get(day);
            int totalRequired = k.get(day);
            int numSlots = m.get(day);

            for (int slot = 0; slot < numSlots; slot++) {
                int needed = totalRequired;

                while (needed > 0) {
                    int minWorkHours = Integer.MAX_VALUE;
                    int selectedPerson = -1;

                    for (int person = 0; person < N; person++) {
                        if (preferences.get(person).get(day).contains(slot) && !schedule.get(slot).contains(person) && workHours[person] < minWorkHours) {
                            minWorkHours = workHours[person];
                            selectedPerson = person;
                        }
                    }

                    if (selectedPerson != -1) {
                        schedule.get(slot).add(selectedPerson);
                        workHours[selectedPerson]++;
                        needed--;
                    } else {
                        break; // 더 이상 배정할 사람이 없으면 종료
                    }
                }
            }
        }

        // 결과 저장
        for (int day = 0; day < days; day++) {
            List<List<Integer>> schedule = schedules.get(day);
            for (int slot = 0; slot < schedule.size(); slot++) {
                for (int employeeId : schedule.get(slot)) {
                    Member employee = memberRepository.findById((long) employeeId).orElseThrow(() -> new StoreException(NOT_STORE_MEMBER));

                    // 시작 시간 설정
                    int startSlot = slot;
                    while (slot + 1 < schedule.size() && schedule.get(slot + 1).contains(employeeId)) {
                        slot++;
                    }
                    // 종료 시간 설정
                    int endSlot = slot + 1;

                    String startTime = String.format("%02d:%02d:00", startSlot / 2, startSlot % 2);
                    String endTime = String.format("%02d:%02d:00", endSlot / 2, endSlot % 2);

                    StoreSchedule storeSchedule = StoreSchedule.createStoreSchedule(
                            store,
                            employee,
                            new Date(System.currentTimeMillis() + day * 86400000L), // 날짜 계산 부분
                            startTime, // 시작 시간
                            endTime // 종료 시간
                    );
                    storeScheduleRepository.save(storeSchedule);
                }
            }
        }

        // 결과 DTO 생성
        StoreScheduleResponseDTO response = new StoreScheduleResponseDTO();
        response.setDay(days);
        response.setSchedules(schedules);
        return response;
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
