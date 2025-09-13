package org.yaroslaavl.recruitingservice.feignClient.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.yaroslaavl.recruitingservice.config.FeignConfig;
import org.yaroslaavl.recruitingservice.feignClient.dto.CompanyPreviewFeignDto;
import org.yaroslaavl.recruitingservice.feignClient.dto.UserFeignDto;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1", configuration = FeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/user/belongs")
    boolean isRecruiterBelongToCompany(@RequestParam("recruiterKeyId") String recruiterKeyId,
                                       @RequestParam("companyId") UUID companyId);

    @GetMapping("/user/isApproved")
    boolean isApproved(@RequestParam("userId") String userId);

    @GetMapping("/user/batch-displayName")
    Map<String, String> usersDisplayName(@RequestParam("userIds") Set<String> userIds,
                                         @RequestParam("currentUserEmail") String currentUserEmail);

    @GetMapping("/user/filtered-candidates")
    Map<String, UserFeignDto> getFilteredCandidates(@RequestParam(required = false) String salary,
                                                    @RequestParam(required = false) String workMode,
                                                    @RequestParam(required = false) Integer availableHoursPerWeek,
                                                    @RequestParam(required = false) String availableFrom);

    @GetMapping("/company/preview")
    Map<UUID, CompanyPreviewFeignDto> previewInfo (@RequestParam("companyIds") Set<UUID> companyIds);
}
