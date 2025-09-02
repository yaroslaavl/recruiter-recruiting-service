package org.yaroslaavl.recruitingservice.mapper;

import org.mapstruct.Mapper;
import org.yaroslaavl.recruitingservice.database.entity.ReportSystem;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemShortDto;

@Mapper(componentModel = "spring")
public interface ReportSystemMapper {

    ReportSystemShortDto toShortDto(ReportSystem reportSystem);

    ReportSystemResponseDto toResponseDto(ReportSystem reportSystem);
}
