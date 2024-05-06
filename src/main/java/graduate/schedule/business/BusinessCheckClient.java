package graduate.schedule.business;

import graduate.schedule.dto.business.BusinessValidateResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "business-check-client", url = "${feign.business.url}")
public interface BusinessCheckClient {
    @PostMapping("/validate")
    BusinessValidateResponseDTO validateBusinessNumber(@RequestParam("serviceKey") String serviceKey, @RequestBody BusinessValidateRequestDTO request);

}