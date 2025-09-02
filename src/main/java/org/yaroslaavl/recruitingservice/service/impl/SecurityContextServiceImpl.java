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

    /**
     * Retrieves the security context information based on the provided credential type
     * from the current authentication token. This method extracts specific claims
     * associated with the credential type from a JWT token if present.
     *
     * @param credentials the type of credential to retrieve from the security context.
     *                     This can either be {@code Credentials.SUB} or {@code Credentials.EMAIL}.
     *                     Other types will result in an exception.
     * @return the value of the claim associated with the specified credential type if available,
     *         or {@code null} if the authentication is not a valid JWT authentication token.
     * @throws IllegalArgumentException if the provided credential type is unsupported.
     */
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