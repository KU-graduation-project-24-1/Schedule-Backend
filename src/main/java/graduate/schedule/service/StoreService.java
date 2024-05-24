package graduate.schedule.service;

import graduate.schedule.business.BusinessDataDTO;
import graduate.schedule.business.BusinessValidateRequestDTO;
import graduate.schedule.common.exception.*;
import graduate.schedule.domain.member.Member;
import graduate.schedule.domain.store.*;
import graduate.schedule.dto.business.BusinessValidateResponseDTO;
import graduate.schedule.dto.business.BusinessValidatedDTO;
import graduate.schedule.dto.store.AvailableScheduleInDayDTO;
import graduate.schedule.dto.store.AvailableTimeInDayDTO;
import graduate.schedule.dto.store.WorkScheduleOnDayDTO;
import graduate.schedule.dto.store.WorkerAndTimeDTO;
import graduate.schedule.dto.web.request.store.*;
import graduate.schedule.dto.web.response.store.*;
import graduate.schedule.dto.web.request.store.CreateStoreRequestDTO;
import graduate.schedule.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.sql.Time;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.*;
import static graduate.schedule.utils.DateAndTimeFormatter.timeWithoutSeconds;
import static graduate.schedule.utils.DateAndTimeFormatter.timeWithSeconds;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreMemberRepository storeMemberRepository;
    private final StoreScheduleRepository storeScheduleRepository;
    private final StoreAvailableScheduleRepository storeAvailableScheduleRepository;
    private final BusinessCheckService businessCheckService;
    private final StoreAvailableTimeByDayRepository storeAvailableTimeByDayRepository;
    private final StoreOperationInfoRepository storeOperationInfoRepository;
    private final MemberRepository memberRepository;


    private final int LEFT_LIMIT = 48;
    private final int RIGHT_LIMIT = 122;
    static final int ASCII_NUMBER_NINE = 57;
    static final int ASCII_UPPERCASE_A = 65;
    static final int ASCII_UPPERCASE_Z = 90;
    static final int ASCII_LOWERCASE_A = 97;
    private final int TARGET_STRING_LENGTH = 8;

    public CreateStoreResponseDTO createStore(Member storeCreator, CreateStoreRequestDTO storeRequest) {
        String inviteCode = getRandomInviteCode();
        LocalDateTime codeGeneratedTime = LocalDateTime.now();

        Store newStore = Store.createStore(storeRequest.getStoreName(), storeRequest.getBusinessRegistrationNumber(), inviteCode, codeGeneratedTime, storeCreator);
        storeRepository.save(newStore);

        return new CreateStoreResponseDTO(newStore);
    }

    private String getRandomInviteCode() {
        String inviteCode;
        do {
            inviteCode = generateInviteCode();
        } while (storeRepository.existsInviteCode(inviteCode));

        return inviteCode;
    }

    private String generateInviteCode() {
        Random random = new Random();

        return random.ints(LEFT_LIMIT, RIGHT_LIMIT + 1)
                .filter(i -> (i <= ASCII_NUMBER_NINE || i >= ASCII_UPPERCASE_A) && (i <= ASCII_UPPERCASE_Z || i >= ASCII_LOWERCASE_A))
                .limit(TARGET_STRING_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public SearchStoreWithInviteCodeResponseDTO searchStoreWithInviteCode(String inviteCode) {
        Store store = storeRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new StoreException(INVALID_INVITE_CODE));

        //초대 코드 유효 여부 검사
        compareInviteCodeAndRequestTime(store);

        return new SearchStoreWithInviteCodeResponseDTO(
                store.getId(),
                store.getName()
        );
    }

    private void compareInviteCodeAndRequestTime(Store store) {
        LocalDateTime joinRequestTime = LocalDateTime.now();
        LocalDateTime expirationDateTime = store.getCodeGeneratedTime().plusDays(1);

        if (joinRequestTime.isAfter(expirationDateTime)) {
            log.error("유효 기간 만료: {}", EXPIRED_INVITE_CODE.getMessage());
            throw new StoreException(EXPIRED_INVITE_CODE);
        }
    }

    public void joinStore(Member member, RequestWithOnlyStoreIdDTO storeRequest) {
        Store store = storeRepository.findById(storeRequest.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));

        if (storeMemberRepository.existsMember(member, store)) {
            throw new StoreException(ALREADY_EXIST_STORE_MEMBER);
        }

        StoreMember.createEmployee(store, member);
    }

    public RegenerateInviteCodeResponseDTO regenerateInviteCode(Member member, RequestWithOnlyStoreIdDTO storeRequest) {
        Store store = storeRepository.findById(storeRequest.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.existsMember(member, store)) {
            throw new StoreMemberException(NOT_STORE_MEMBER);
        }
        if (!storeMemberRepository.isExecutive(store, member)) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        String newInviteCode = getRandomInviteCode();
        LocalDateTime codeGeneratedTime = LocalDateTime.now();
        store.setNewInviteCode(newInviteCode, codeGeneratedTime);

        return new RegenerateInviteCodeResponseDTO(newInviteCode);
    }

    public void businessProof(BusinessProofRequestDTO storeRequest) {
        // 1. 이미 존재하는 가게인지 검사
        if (storeRepository.findByBusinessRegistrationNumber(storeRequest.getBusinessRegistrationNumber()).isPresent()) {
            throw new StoreException(ALREADY_EXIST_STORE);
        }

        // 2. 사업자 진위 여부 검사 - 오픈 api
        BusinessValidateRequestDTO validateBusinessData = requestToValidateBusinessData(storeRequest);
        BusinessValidateResponseDTO apiResponse = businessCheckService.callValidateBusinessAPI(validateBusinessData);

        validateBusiness(apiResponse);
    }

    private void validateBusiness(BusinessValidateResponseDTO apiResponse) {
        String statusCode = apiResponse.getStatus_code();
        BusinessValidatedDTO validatedData = apiResponse.getData().get(0);

        if (validatedData.getValid().equals("02")) {
            throw new BusinessException(BUSINESS_CHECK_FAILED);
        }
    }

    private static BusinessValidateRequestDTO requestToValidateBusinessData(BusinessProofRequestDTO storeRequest) {
        String decodedBusinessNumber = storeRequest.getBusinessRegistrationNumber();
        BusinessDataDTO businessData = new BusinessDataDTO(decodedBusinessNumber, storeRequest.getOpeningDate(), storeRequest.getCeoName());

        List<BusinessDataDTO> businessDataList = new ArrayList<>();
        businessDataList.add(businessData);

        return new BusinessValidateRequestDTO(businessDataList);
    }

    public WorkScheduleOnMonthResponseDTO getScheduleOnMonth(Member member, Long storeId, String searchMonth) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.existsMember(member, store)) {
            throw new StoreMemberException(NOT_STORE_MEMBER);
        }

        List<Date> existingWorkingDatesOnMonth = storeScheduleRepository.findDatesByStoreAndMonthOrderByDate(store, searchMonth);
        List<WorkScheduleOnDayDTO> daySchedules = existingWorkingDatesOnMonth.stream()
                .map(date -> getDateSchedule(store, member, date)).toList();

        return new WorkScheduleOnMonthResponseDTO(daySchedules);
    }

    private WorkScheduleOnDayDTO getDateSchedule(Store store, Member member, Date date) {
        List<StoreSchedule> schedulesIndDay = storeScheduleRepository.findSchedulesByStoreAndDate(store, date);

        List<WorkerAndTimeDTO> workDatas = schedulesIndDay.stream()
                .map(schedule -> new WorkerAndTimeDTO(
                        schedule.getId(),
                        schedule.getEmployeeId(),
                        schedule.getEmployeeName(),
                        storeMemberRepository.findByStoreAndMember(store, schedule.getEmployee())
                                .orElseThrow(() -> new StoreMemberException(NOT_STORE_MEMBER))
                                .getMemberGrade(),
                        schedule.getEmployee() == member,
                        timeWithoutSeconds(schedule.getStartTime()),
                        timeWithoutSeconds(schedule.getEndTime()),
                        schedule.isRequestCover()))
                .sorted(Comparator.comparing(WorkerAndTimeDTO::getStartTime)).toList();

        return new WorkScheduleOnDayDTO(
                date,
                workDatas
        );
    }

    public AvailableScheduleOnMonthResponseDTO getAvailableScheduleOnMonth(Member member, Long storeId, String searchMonth) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        StoreMemberGrade memberGrade = storeMemberRepository.findByStoreAndMember(store, member)
                .orElseThrow(() -> new StoreMemberException(NOT_STORE_MEMBER))
                .getMemberGrade();

        List<Date> existingAvailableDatesByStoreAndMonth = storeAvailableScheduleRepository.findDatesByStoreAndMemberAndMonthOrderByDate(store, member, searchMonth);
        List<AvailableScheduleInDayDTO> dayAvailableSchedules = existingAvailableDatesByStoreAndMonth.stream()
                .map(date -> getDateAvailableSchedule(store, member, date)).toList();

        return new AvailableScheduleOnMonthResponseDTO(
                memberGrade.getGrade(),
                dayAvailableSchedules
        );
    }

    private AvailableScheduleInDayDTO getDateAvailableSchedule(Store store, Member member, Date date) {
        List<StoreAvailableSchedule> availableTimesInDay = storeAvailableScheduleRepository.findByStoreAndDateOrderByStartTime(store, date);
        List<AvailableTimeInDayDTO> availableTimeDatas = availableTimesInDay.stream()
                .map(time -> new AvailableTimeInDayDTO(
                        time.getId(),
                        timeWithoutSeconds(time.getStartTime()),
                        timeWithoutSeconds(time.getEndTime())
                ))
                .sorted(Comparator.comparing(AvailableTimeInDayDTO::getStartTime)).toList();

        return new AvailableScheduleInDayDTO(
                date,
                availableTimeDatas
        );
    }

    public AddAvailableScheduleResponseDTO addAvailableScheduleInDay(Member member, AddAvailableScheduleRequestDTO storeRequest) {
        Store store = storeRepository.findById(storeRequest.getStoreId())
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        if (!storeMemberRepository.existsMember(member, store)) {
            throw new StoreMemberException(NOT_STORE_MEMBER);
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
        if (!availableTime.getMember().equals(member)) {
            throw new StoreScheduleException(NOT_MEMBER_WORKING_DATA);
        }
        storeAvailableScheduleRepository.delete(availableTime);
    }

    // 특정 가게의 내 정보 가져오기
    public MyStoreInfoResponseDTO getMyStoreInfo(Member member, Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        StoreMember storeMember = storeMemberRepository.findByStoreAndMember(store, member)
                .orElseThrow(() -> new StoreException(NOT_STORE_MEMBER));

        return new MyStoreInfoResponseDTO(member.getName(), member.getProfileImg(), storeMember.getMemberGrade().getGrade());
    }

    // 주 단위 고정 근무시간 가져오기
    public StoreAvailableTimeByDayResponseDTO getStoreAvailableTimeByDay(Member member, Long storeId) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        List<StoreAvailableTimeByDay> schedules = storeAvailableTimeByDayRepository.findByStoreAndMember(store, member);

        List<DayOfWeek> dayOfWeeks = schedules.stream().map(StoreAvailableTimeByDay::getDayOfWeek).toList();
        List<String> startTimes = schedules.stream().map(schedule -> schedule.getStartTime().toString()).toList();
        List<String> endTimes = schedules.stream().map(schedule -> schedule.getEndTime().toString()).toList();

        return new StoreAvailableTimeByDayResponseDTO(dayOfWeeks, startTimes, endTimes);
    }

    // 주 단위 고정 근무시간 수정하기
    public void updateStoreAvailableTimeByDay(Member member, Long storeId, UpdateStoreAvailableTimeByDayRequestDTO request) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        DayOfWeek dayOfWeek = request.getDayOfWeek();
        Time newStartTime = timeWithSeconds(request.getStartTime());
        Time newEndTime = timeWithSeconds(request.getEndTime());

        // 기존 고정 근무시간을 찾아서 업데이트하거나 새로 생성
        StoreAvailableTimeByDay fixedSchedule = storeAvailableTimeByDayRepository.findByStoreAndMemberAndDayOfWeek(store, member, dayOfWeek)
                .orElse(new StoreAvailableTimeByDay(member, store, dayOfWeek, newStartTime, newEndTime));

        // 기존 요일에 해당하는 데이터 삭제 또는 수정
        List<StoreAvailableTimeByDay> existingSchedules = storeAvailableTimeByDayRepository.findByStoreAndMemberAndDayOfWeekOrderByStartTime(store, member, dayOfWeek);

        for (StoreAvailableTimeByDay schedule : existingSchedules) {
            if (schedule.getStartTime().before(newEndTime) && schedule.getEndTime().after(newStartTime)) {
                if (schedule.getStartTime().before(newStartTime)) {
                    schedule.updateWorkTime(schedule.getStartTime(), newStartTime);
                    storeAvailableTimeByDayRepository.save(schedule);
                } else if (schedule.getEndTime().after(newEndTime)) {
                    schedule.updateWorkTime(newEndTime, schedule.getEndTime());
                    storeAvailableTimeByDayRepository.save(schedule);
                } else {
                    storeAvailableTimeByDayRepository.delete(schedule);
                }
            }
        }

        // 새로운 입력 데이터를 추가하거나, 이미 있는 데이터와 경계가 겹치는 경우 병합
        for (StoreAvailableTimeByDay schedule : existingSchedules) {
            if (newStartTime.before(schedule.getEndTime()) && newEndTime.after(schedule.getStartTime())) {
                newStartTime = new Time(Math.min(newStartTime.getTime(), schedule.getStartTime().getTime()));
                newEndTime = new Time(Math.max(newEndTime.getTime(), schedule.getEndTime().getTime()));
                storeAvailableTimeByDayRepository.delete(schedule);
            }
        }

        fixedSchedule.updateWorkTime(newStartTime, newEndTime);
        storeAvailableTimeByDayRepository.save(fixedSchedule);
    }

    // 가게 근무시간 설정하기
    public void setStoreOperationInfo(Member member, Long storeId, StoreOperationInfoRequestDTO request) {
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new StoreException(NOT_FOUND_STORE));

        // 사용자가 고용인인지 확인
        boolean isEmployee = storeMemberRepository.findByStoreAndMemberGrade(store, StoreMemberGrade.BOSS)
                .filter(storeMember -> storeMember.getMember().equals(member))
                .isPresent();
        if (!isEmployee) {
            throw new MemberException(NOT_EXECUTIVE);
        }

        DayOfWeek dayOfWeek = request.getDayOfWeek();
        int requiredEmployees = request.getRequiredEmployees();
        Time startTime = Time.valueOf(request.getStartTime());
        Time endTime = Time.valueOf(request.getEndTime());

        StoreOperationInfo operationInfo = storeOperationInfoRepository.findByStoreAndDayOfWeek(store, dayOfWeek)
                .orElse(new StoreOperationInfo(store, dayOfWeek, requiredEmployees, startTime, endTime));

        operationInfo.setRequiredEmployees(requiredEmployees);
        operationInfo.setStartTime(startTime);
        operationInfo.setEndTime(endTime);

        storeOperationInfoRepository.save(operationInfo);
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
}
