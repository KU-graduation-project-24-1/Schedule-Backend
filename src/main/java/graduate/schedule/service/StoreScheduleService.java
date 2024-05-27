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
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;
import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.NOT_STORE_MEMBER;
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
    private final String EMPLOYER_ACCEPTED_COVER_BODY = "의 대체 근무 요청을 고용인이 수락하여 근무 정보를 삭제합니다!";

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

        Date date = storeRequest.getDate();
        Time newStartTime = Time.valueOf(storeRequest.getStartTime() + ":00");
        Time newEndTime = Time.valueOf(storeRequest.getEndTime() + ":00");

        List<StoreAvailableSchedule> existingSchedules = storeAvailableScheduleRepository.findByStoreAndDate(store, date);
        List<StoreAvailableSchedule> schedulesToDelete = new ArrayList<>();
        boolean merged = false;

        for (StoreAvailableSchedule schedule : existingSchedules) {
            Time existingStartTime = schedule.getStartTime();
            Time existingEndTime = schedule.getEndTime();

            if (newEndTime.before(existingStartTime) || newStartTime.after(existingEndTime)) {
                continue;
            } else {
                newStartTime = new Time(Math.min(newStartTime.getTime(), existingStartTime.getTime()));
                newEndTime = new Time(Math.max(newEndTime.getTime(), existingEndTime.getTime()));
                schedulesToDelete.add(schedule);
                merged = true;
            }
        }

        if (merged) {
            for (StoreAvailableSchedule schedule : schedulesToDelete) {
                storeAvailableScheduleRepository.delete(schedule);
            }
        }

        StoreAvailableSchedule newStoreAvailableSchedule =
                StoreAvailableSchedule.createStoreAvailableSchedule(
                        store,
                        member,
                        date,
                        newStartTime,
                        newEndTime
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
        Member previousEmployee = storeSchedule.getEmployee();

        //대체 근무자와 대체 근무 요청자가 동일한 경우
        if (substitute.equals(previousEmployee)) {
            throw new StoreScheduleException(SUBSTITUTE_SAME_AS_PREVIOUS_EMPLOYEE);
        }

        //대체 근무자가 고용인인 경우 해당 근무 정보 삭제
        if (substitute.equals(employer)) {
            log.info("대체 근무자가 고용인으로 설정되어 근무 정보를 삭제합니다.");
            String acceptCoverByEmployerBody = storeSchedule.getDate() + BODY_COMMA +
                    timeWithoutSeconds(storeSchedule.getStartTime()) + BODY_WAVE + timeWithoutSeconds(storeSchedule.getEndTime()) +
                    EMPLOYER_ACCEPTED_COVER_BODY;
            fcmService.sendMessageTo(previousEmployee.getFcmToken(), ACCEPT_COVER_TITLE, acceptCoverByEmployerBody);
            storeScheduleRepository.delete(storeSchedule);
            return new ChangeScheduleResponseDTO();
        }

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

    // 주 단위 고정 근무시간 추가하기
    public AddAvailableTimeByDayResponseDTO addStoreAvailableTimeByDay(Member member, AddStoreAvailableTimeByDayRequestDTO request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.existsMember(member, store)) {
            throw new StoreMemberException(NOT_STORE_MEMBER);
        }

        DayOfWeek dayOfWeek = request.getDayOfWeek();
        Time newStartTime = Time.valueOf(request.getStartTime() + ":00");
        Time newEndTime = Time.valueOf(request.getEndTime() + ":00");

        StoreAvailableTimeByDay newStoreAvailableTimeByDay =
                StoreAvailableTimeByDay.createStoreAvailableTimeByDay(
                        store,
                        member,
                        dayOfWeek,
                        newStartTime,
                        newEndTime
                );
        storeAvailableTimeByDayRepository.save(newStoreAvailableTimeByDay);

        Date now = new Date(System.currentTimeMillis());
        YearMonth currentMonth = YearMonth.of(now.toLocalDate().getYear(), now.toLocalDate().getMonth());
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        LocalDate lastDayOfMonth = currentMonth.atEndOfMonth();

        // 월의 모든 날짜를 포함하는 리스트를 생성
        List<LocalDate> datesInMonth = firstDayOfMonth.datesUntil(lastDayOfMonth.plusDays(1)).toList();

        for (LocalDate date : datesInMonth) {
            if (date.getDayOfWeek().equals(dayOfWeek)) {
                Date sqlDate = Date.valueOf(date);

                List<StoreAvailableSchedule> existingSchedules = storeAvailableScheduleRepository.findByStoreAndDate(store, sqlDate);
                List<StoreAvailableSchedule> schedulesToDelete = new ArrayList<>();
                boolean merged = false;

                Time startTime = newStartTime;
                Time endTime = newEndTime;

                for (StoreAvailableSchedule schedule : existingSchedules) {
                    Time existingStartTime = schedule.getStartTime();
                    Time existingEndTime = schedule.getEndTime();

                    if (endTime.before(existingStartTime) || startTime.after(existingEndTime)) {
                        continue;
                    } else {
                        startTime = new Time(Math.min(startTime.getTime(), existingStartTime.getTime()));
                        endTime = new Time(Math.max(endTime.getTime(), existingEndTime.getTime()));
                        schedulesToDelete.add(schedule);
                        merged = true;
                    }
                }

                if (merged) {
                    for (StoreAvailableSchedule schedule : schedulesToDelete) {
                        storeAvailableScheduleRepository.delete(schedule);
                    }
                }

                StoreAvailableSchedule newStoreAvailableSchedule =
                        StoreAvailableSchedule.createStoreAvailableSchedule(
                                store,
                                member,
                                sqlDate,
                                startTime,
                                endTime
                        );
                storeAvailableScheduleRepository.save(newStoreAvailableSchedule);
            }
        }

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
        DayOfWeek dayOfWeek = schedule.getDayOfWeek();

        List<StoreAvailableSchedule> existingSchedules = storeAvailableScheduleRepository.findByStoreAndMemberAndDayOfWeek(store, member, dayOfWeek.getValue());

        for (StoreAvailableSchedule existingSchedule : existingSchedules) {
            Time existingStartTime = existingSchedule.getStartTime();
            Time existingEndTime = existingSchedule.getEndTime();

            if (!deleteEndTime.before(existingStartTime) && !deleteStartTime.after(existingEndTime)) {
                if (deleteStartTime.after(existingStartTime) && deleteEndTime.before(existingEndTime)) {
                    // 기존 스케줄이 삭제할 시간 범위를 포함하는 경우, 두 개로 나눔
                    StoreAvailableSchedule newSchedule1 = StoreAvailableSchedule.createStoreAvailableSchedule(
                            store, member, existingSchedule.getDate(), existingStartTime, deleteStartTime);
                    storeAvailableScheduleRepository.save(newSchedule1);

                    StoreAvailableSchedule newSchedule2 = StoreAvailableSchedule.createStoreAvailableSchedule(
                            store, member, existingSchedule.getDate(), deleteEndTime, existingEndTime);
                    storeAvailableScheduleRepository.save(newSchedule2);

                    storeAvailableScheduleRepository.delete(existingSchedule);
                } else if (deleteStartTime.after(existingStartTime)) {
                    // 삭제 범위가 기존 스케줄 끝 부분에 걸치는 경우
                    existingSchedule.updateWorkTime(existingStartTime, deleteStartTime);
                    storeAvailableScheduleRepository.save(existingSchedule);
                } else if (deleteEndTime.before(existingEndTime)) {
                    // 삭제 범위가 기존 스케줄 시작 부분에 걸치는 경우
                    existingSchedule.updateWorkTime(deleteEndTime, existingEndTime);
                    storeAvailableScheduleRepository.save(existingSchedule);
                } else {
                    // 삭제 범위가 기존 스케줄 전체를 포함하는 경우
                    storeAvailableScheduleRepository.delete(existingSchedule);
                }
            }
        }

        storeAvailableTimeByDayRepository.delete(schedule);
    }







    // 가게 근무시간 설정하기
    public AddStoreOperationInfoResponseDTO addStoreOperationInfo(Member member, Long storeId, StoreOperationInfoRequestDTO request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));

        DayOfWeek dayOfWeek = request.getDayOfWeek();
        Time startTime = Time.valueOf(request.getStartTime() + ":00");
        Time endTime = Time.valueOf(request.getEndTime() + ":00");

        StoreOperationInfo operationInfo = new StoreOperationInfo(store, dayOfWeek, 0, startTime, endTime); // requiredEmployees를 0으로 설정
        storeOperationInfoRepository.save(operationInfo);

        return new AddStoreOperationInfoResponseDTO(operationInfo.getId());
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

    public void updateRequiredEmployees(UpdateRequiredEmployeesRequestDTO request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));

        List<StoreOperationInfo> operationInfos = storeOperationInfoRepository.findByStoreAndDayOfWeek(store, request.getDayOfWeek());

        if (operationInfos.isEmpty()) {
            throw new StoreException(NOT_FOUND_STORE_MEMBER_AVAILABLE_TIME);
        }

        for (StoreOperationInfo info : operationInfos) {
            info.setRequiredEmployees(request.getRequiredEmployees());
            storeOperationInfoRepository.save(info);
        }
    }

    @Scheduled(cron = "0 0 0 15 * ?")
    public void generateMonthlySchedule() {
        List<Store> stores = storeRepository.findAll();
        for (Store store : stores) {
            try {
                generateSchedule(store.getId());
                List<Member> members = storeMemberRepository.findAllByStore(store);
                for (Member member : members) {
                    fcmService.sendMessageTo(member.getFcmToken(), "스케줄 생성 완료", "다음 달 스케줄이 성공적으로 생성되었습니다. (스토어: " + store.getName() + ")");
                }
            } catch (Exception e) {
                log.error("스케줄 생성 중 오류 발생 (스토어: " + store.getName() + ")", e);
                List<Member> members = storeMemberRepository.findAllByStore(store);
                for (Member member : members) {
                    fcmService.sendMessageTo(member.getFcmToken(), "스케줄 생성 실패", "다음 달 스케줄 생성 중 오류가 발생했습니다. (스토어: " + store.getName() + ")");
                }
            }
        }
    }

    public void generateSchedule(Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        List<Member> members = storeMemberRepository.findAllByStore(store);
        int N = members.size();

        Map<DayOfWeek, List<StoreOperationInfo>> operationInfoByDay = storeOperationInfoRepository.findByStore(store)
                .stream()
                .collect(Collectors.groupingBy(StoreOperationInfo::getDayOfWeek));

        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        LocalDate firstDayOfNextMonth = nextMonth.atDay(1);
        LocalDate lastDayOfNextMonth = nextMonth.atEndOfMonth();

        Map<Member, Integer> workHours = new HashMap<>();
        for (Member member : members) {
            workHours.put(member, 0);
        }

        for (LocalDate date = firstDayOfNextMonth; !date.isAfter(lastDayOfNextMonth); date = date.plusDays(1)) {
            log.info("Processing date: {}", date);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            List<StoreOperationInfo> operationInfos = operationInfoByDay.getOrDefault(dayOfWeek, new ArrayList<>());

            if (operationInfos.isEmpty()) {
                log.info("No operation info for day: {}", dayOfWeek);
                continue; // 해당 요일에 운영정보가 없는 경우 스킵
            }

            List<Member> availableMembers = new ArrayList<>();
            for (Member member : members) {
                if (isMemberAvailableOnDate(member, store, date)) {
                    availableMembers.add(member);
                }
            }

            for (StoreOperationInfo operationInfo : operationInfos) {
                int requiredEmployees = operationInfo.getRequiredEmployees();
                Time operationStartTime = operationInfo.getStartTime();
                Time operationEndTime = operationInfo.getEndTime();

                List<Member> assignedMembers = new ArrayList<>();
                availableMembers.sort((m1, m2) -> Integer.compare(workHours.get(m1), workHours.get(m2)));

                for (Member member : availableMembers) {
                    if (assignedMembers.size() < requiredEmployees && isMemberAvailableAtTime(member, store, date, operationStartTime, operationEndTime)) {
                        assignedMembers.add(member);
                    }
                }

                if (assignedMembers.size() < requiredEmployees) {
                    log.warn("날짜: {} 시간: {} ~ {}에 필요한 인원이 부족합니다. 배정된 인원 수: {}", date, operationStartTime, operationEndTime, assignedMembers.size());
                }

                for (Member assignedMember : assignedMembers) {
                    workHours.put(assignedMember, workHours.get(assignedMember) + calculateDuration(operationStartTime, operationEndTime));
                    saveSchedule(store, assignedMember, date, operationStartTime, operationEndTime);
                }
            }
        }
    }

    private boolean isMemberAvailableOnDate(Member member, Store store, LocalDate date) {
        List<StoreAvailableSchedule> schedules = storeAvailableScheduleRepository.findByStoreAndMemberAndDate(store, member, Date.valueOf(date));
        return !schedules.isEmpty();
    }

    private boolean isMemberAvailableAtTime(Member member, Store store, LocalDate date, Time operationStartTime, Time operationEndTime) {
        List<StoreAvailableSchedule> schedules = storeAvailableScheduleRepository.findByStoreAndMemberAndDate(store, member, Date.valueOf(date));
        for (StoreAvailableSchedule schedule : schedules) {
            Time availableStartTime = schedule.getStartTime();
            Time availableEndTime = schedule.getEndTime();
            if (!operationEndTime.before(availableStartTime) && !operationStartTime.after(availableEndTime)) {
                return true;
            }
        }
        return false;
    }

    private void saveSchedule(Store store, Member member, LocalDate date, Time operationStartTime, Time operationEndTime) {
        List<StoreAvailableSchedule> availableSchedules = storeAvailableScheduleRepository.findByStoreAndMemberAndDate(store, member, Date.valueOf(date));

        for (StoreAvailableSchedule availableSchedule : availableSchedules) {
            Time availableStartTime = availableSchedule.getStartTime();
            Time availableEndTime = availableSchedule.getEndTime();

            Time startTime = maxTime(operationStartTime, availableStartTime);
            Time endTime = minTime(operationEndTime, availableEndTime);

            if (startTime.before(endTime)) {
                mergeAndSave(store, member, date, startTime, endTime);
            }
        }
    }

    private Time maxTime(Time time1, Time time2) {
        return time1.after(time2) ? time1 : time2;
    }

    private Time minTime(Time time1, Time time2) {
        return time1.before(time2) ? time1 : time2;
    }

    private void mergeAndSave(Store store, Member member, LocalDate date, Time startTime, Time endTime) {
        List<StoreSchedule> existingSchedules = storeScheduleRepository.findByStoreAndMemberAndDate(store, member, Date.valueOf(date));

        LocalTime newStartTime = startTime.toLocalTime();
        LocalTime newEndTime = endTime.toLocalTime();

        for (StoreSchedule schedule : existingSchedules) {
            LocalTime existingStartTime = schedule.getStartTime().toLocalTime();
            LocalTime existingEndTime = schedule.getEndTime().toLocalTime();

            if (!newEndTime.isBefore(existingStartTime) && !newStartTime.isAfter(existingEndTime)) {
                newStartTime = newStartTime.isBefore(existingStartTime) ? newStartTime : existingStartTime;
                newEndTime = newEndTime.isAfter(existingEndTime) ? newEndTime : existingEndTime;
                storeScheduleRepository.delete(schedule);
            }
        }

        StoreSchedule newSchedule = StoreSchedule.createStoreSchedule(
                store,
                member,
                Date.valueOf(date),
                newStartTime.toString(),
                newEndTime.toString()
        );
        storeScheduleRepository.save(newSchedule);
        log.info("Saved new schedule for member {} on date {} from {} to {}", member.getId(), date, newStartTime, newEndTime);
    }

    private int calculateDuration(Time startTime, Time endTime) {
        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();
        return (int) java.time.Duration.between(start, end).toMinutes();
    }

    /**
     * 가능한 고정 시간 데이터 저장 - 매월 8일 00시
     */
    @Scheduled(cron = "0 0 0 8 * ?")
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
