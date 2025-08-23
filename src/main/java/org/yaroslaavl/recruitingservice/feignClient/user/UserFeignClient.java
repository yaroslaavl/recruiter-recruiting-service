package org.yaroslaavl.recruitingservice.feignClient.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "user-service", path = "/api/v1")
public interface UserFeignClient {

    @GetMapping("/user/belongs")
    Boolean isRecruiterBelongToCompany(@RequestHeader("Authorization") String authorization,
                          @RequestParam("recruiterKeyId") String recruiterKeyId,
                          @RequestParam("companyId") UUID companyId);
}
