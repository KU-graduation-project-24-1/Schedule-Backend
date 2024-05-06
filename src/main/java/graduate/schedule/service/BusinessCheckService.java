package graduate.schedule.service;

import graduate.schedule.business.BusinessCheckClient;
import graduate.schedule.business.BusinessValidateRequestDTO;
import graduate.schedule.common.exception.BusinessException;
import graduate.schedule.dto.business.BusinessValidateResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.BUSINESS_CHECK_FAILED;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BusinessCheckService {
    private final BusinessCheckClient businessCheckClient;

    @Value("${feign.business.check.service-key}")
    private String serviceKey;

    public BusinessValidateResponseDTO validateBusiness(BusinessValidateRequestDTO validateBusinessData) {
        try {
            businessCheckClient.validateBusinessNumber(serviceKey, validateBusinessData);
        } catch (Exception e) {
            log.error("error in BusinessCheckService");
            throw new BusinessException(BUSINESS_CHECK_FAILED);
        }
        return businessCheckClient.validateBusinessNumber(serviceKey, validateBusinessData);
    }
}
