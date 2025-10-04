package org.yaroslaavl.recruitingservice.database.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "Credentials")
public enum Credentials {
    SUB("sub"),
    EMAIL("email");

    private final String credential;

    Credentials(String credential) {
        this.credential = credential;
    }
}
