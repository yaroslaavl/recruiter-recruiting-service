package org.yaroslaavl.recruitingservice.feignClient.cv;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.yaroslaavl.recruitingservice.config.FeignConfig;

import java.util.UUID;

@FeignClient(name = "cv-service", path = "/api/v1", configuration = FeignConfig.class)
public interface CvFeignClient {

    @GetMapping(value = "/cv/{cvId}/recruiter")
    String getCvForRecruiter(@PathVariable UUID cvId);
}
