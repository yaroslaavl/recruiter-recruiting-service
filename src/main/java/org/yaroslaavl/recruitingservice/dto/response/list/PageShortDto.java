package org.yaroslaavl.recruitingservice.dto.response.list;

import java.util.List;

public record PageShortDto<T>(
        List<T> allContent,
        long totalElements,
        long totalPages,
        int page,
        int size
) { }
