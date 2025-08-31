package org.yaroslaavl.recruitingservice.mapper;

import org.mapstruct.Mapper;
import org.yaroslaavl.recruitingservice.database.entity.ReportSystem;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemResponseDto;

@Mapper(componentModel = "spring")
public interface ReportSystemMapper {

    ReportSystemResponseDto toResponseDto(ReportSystem reportSystem);
}
