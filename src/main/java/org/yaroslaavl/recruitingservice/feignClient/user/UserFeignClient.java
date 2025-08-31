package org.yaroslaavl.recruitingservice.feignClient.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.yaroslaavl.recruitingservice.config.FeignConfig;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1", configuration = FeignConfig.class)
public interface UserFeignClient {

    @GetMapping("/user/belongs")
    Boolean isRecruiterBelongToCompany(@RequestParam("recruiterKeyId") String recruiterKeyId,
                                       @RequestParam("companyId") UUID companyId);

    @GetMapping("/user/isApproved")
    boolean isApproved(@RequestParam("userId") String userId);
}
