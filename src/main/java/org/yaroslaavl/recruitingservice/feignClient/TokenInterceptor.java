package org.yaroslaavl.recruitingservice.feignClient;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Component
@RequiredArgsConstructor
public class TokenInterceptor implements RequestInterceptor {

    private final TokenManager tokenManager;
    private static final String TOKEN = "Bearer {0}";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String formattedToken = MessageFormat.format(TOKEN, tokenManager.getServiceToken());
        requestTemplate.header("Authorization", formattedToken);
    }
}
