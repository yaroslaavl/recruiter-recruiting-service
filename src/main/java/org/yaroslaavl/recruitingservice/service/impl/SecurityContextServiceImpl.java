package org.yaroslaavl.recruitingservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;

@Slf4j
@Service
public class SecurityContextServiceImpl implements SecurityContextService {

    @Override
    public String getSecurityContext(Credentials credentials) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwt)) {
            log.warn("Authentication is not JwtAuthenticationToken or it has no sub");
            return null;
        }
        var token = jwt.getToken();

        switch (credentials) {
            case SUB, EMAIL -> {
                return token.getClaimAsString(credentials.getCredential());
            }
            default -> throw new IllegalArgumentException("Unsupported credentials type");
        }
    }
}