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
import static graduate.schedule.utils.DateAndTimeFormatter.timeWithSeconds;
import static graduate.schedule.utils.DateAndTimeFormatter.timeWithoutSeconds;

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

        List<Date> existingAvailableDatesByStoreAndMonth = storeAvailableScheduleRepository.findDatesByStoreAndEmployeeAndMonthOrderByDate(store, member, searchMonth);
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
        if (!availableTime.getEmployee().equals(member)) {
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
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));
        StoreMemberGrade memberGrade = storeMemberRepository.findByStoreAndMember(store, member)
                .orElseThrow(() -> new StoreMemberException(NOT_STORE_MEMBER))
                .getMemberGrade();

        List<StoreAvailableTimeByDay> schedules = storeAvailableTimeByDayRepository.findByStoreAndMember(store, member);

        List<DayOfWeek> dayOfWeeks = schedules.stream()
                .map(StoreAvailableTimeByDay::getDayOfWeek)
                .toList();
        List<String> startTimes = schedules.stream()
                .map(schedule -> schedule.getStartTime().toString())
                .toList();
        List<String> endTimes = schedules.stream()
                .map(schedule -> schedule.getEndTime().toString())
                .toList();

        return new StoreAvailableTimeByDayResponseDTO(dayOfWeeks, startTimes, endTimes);
    }

    public List<StoreOperationInfoResponseDTO> getStoreOperationInfoByDay(Long storeId, DayOfWeek dayOfWeek) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));

        List<StoreOperationInfo> operationInfos = storeOperationInfoRepository.findByStoreAndDayOfWeek(store, dayOfWeek);

        return operationInfos.stream()
                .map(info -> new StoreOperationInfoResponseDTO(
                        info.getDayOfWeek(),
                        info.getStartTime(),
                        info.getEndTime(),
                        info.getRequiredEmployees()))
                .toList();
    }

    public StoreRequiredEmployeesResponseDTO getRequiredEmployees(Long storeId, DayOfWeek dayOfWeek) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE));

        StoreOperationInfo operationInfo = storeOperationInfoRepository.findByStoreAndDayOfWeek(store, dayOfWeek)
                .stream().findFirst()
                .orElseThrow(() -> new StoreException(NOT_FOUND_STORE_MEMBER_AVAILABLE_TIME));

        return new StoreRequiredEmployeesResponseDTO(store.getId(), operationInfo.getId(), operationInfo.getDayOfWeek(), operationInfo.getRequiredEmployees());
    }
}
