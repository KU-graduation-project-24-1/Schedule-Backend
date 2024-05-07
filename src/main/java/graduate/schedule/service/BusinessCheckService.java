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

import static graduate.schedule.common.response.status.BaseExceptionResponseStatus.INAPPROPRIATE_DATA;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BusinessCheckService {
    private final BusinessCheckClient businessCheckClient;

    @Value("${feign.business.check.service-key}")
    private String serviceKey;

    public BusinessValidateResponseDTO callValidateBusinessAPI(BusinessValidateRequestDTO validateBusinessData) {
        BusinessValidateResponseDTO response;
        try {
            response = businessCheckClient.validateBusinessNumber(serviceKey, validateBusinessData);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(INAPPROPRIATE_DATA);
        }
        return response;
    }
}
