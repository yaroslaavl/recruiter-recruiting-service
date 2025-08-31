package org.yaroslaavl.recruitingservice.database.entity.enums;

import lombok.Getter;

@Getter
public enum Credentials {
    SUB("sub"),
    EMAIL("email");

    private final String credential;

    Credentials(String credential) {
        this.credential = credential;
    }
}
