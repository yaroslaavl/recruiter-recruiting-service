package org.yaroslaavl.recruitingservice.mapper;

import org.apache.catalina.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.yaroslaavl.recruitingservice.database.entity.ReportSystem;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.ReportSystemShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.UserReportsShortDto;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ReportSystemMapper {

    @Mapping(target = "id", source = "reportSystem.id")
    @Mapping(target = "reportedAt", source = "reportSystem.createdAt")
    @Mapping(target = "status", source = "reportSystem.status")
    @Mapping(target = "displayName", expression = "java(mapUserEmail(reportSystem, usersDisplayName))")
    ReportSystemShortDto toShortDto(ReportSystem reportSystem, Map<String, String> usersDisplayName);

    default List<ReportSystemShortDto> toShortDto(List<ReportSystem> reportSystemList, Map<String, String> usersDisplayName) {
        return reportSystemList.stream()
                .map(reportSystem -> toShortDto(reportSystem, usersDisplayName))
                .toList();
    }

    @Mapping(target = "vacancyTitle", source = "vacancy.title")
    @Mapping(target = "reportedAt", source = "createdAt")
    UserReportsShortDto toUserShortDto(ReportSystem reportSystem);

    List<UserReportsShortDto> toUserShortDto(List<ReportSystem> reportSystemList);

    @Mapping(target = "reportedAt", source = "createdAt")
    ReportSystemResponseDto toResponseDto(ReportSystem reportSystem);

    default String mapUserEmail(ReportSystem reportSystem, Map<String, String> usersDisplayName) {
        return usersDisplayName.get(reportSystem.getUserId());
    }
}
