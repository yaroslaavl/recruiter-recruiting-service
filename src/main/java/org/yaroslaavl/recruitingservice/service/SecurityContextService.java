package org.yaroslaavl.recruitingservice.service;

import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;

@FunctionalInterface
public interface SecurityContextService {

    String getSecurityContext(Credentials credentials);
}
