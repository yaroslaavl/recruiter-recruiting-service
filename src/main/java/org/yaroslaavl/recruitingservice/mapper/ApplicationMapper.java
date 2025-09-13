package org.yaroslaavl.recruitingservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.yaroslaavl.recruitingservice.database.entity.Application;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationDetailsResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.ApplicationShortDto;
import org.yaroslaavl.recruitingservice.feignClient.dto.UserFeignDto;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "id", source = "application.id")
    @Mapping(target = "displayName", expression = "java(mapCandidateDisplayName(application, profileInfo))")
    @Mapping(target = "salary", expression = "java(mapCandidateSalary(application, profileInfo))")
    @Mapping(target = "workMode", expression = "java(mapCandidateWorkMode(application, profileInfo))")
    @Mapping(target = "availableHoursPerWeek", expression = "java(mapCandidateAvailableHours(application, profileInfo))")
    @Mapping(target = "availableFrom", expression = "java(mapCandidateAvailableFrom(application, profileInfo))")
    @Mapping(target = "status", source = "application.status")
    @Mapping(target = "appliedAt", source = "application.appliedAt")
    ApplicationShortDto toShortDto(Application application, Map<String, UserFeignDto> profileInfo);

    default List<ApplicationShortDto> toShortDto(List<Application> applications, Map<String, UserFeignDto> profileInfo) {
        return applications.stream()
                .map(application -> toShortDto(application, profileInfo))
                .toList();
    }

    @Mapping(target = "vacancyId", source = "vacancy.id")
    @Mapping(target = "vacancyTitle", source = "vacancy.title")
    @Mapping(target = "location", source = "vacancy.location")
    @Mapping(target = "salaryFrom", source = "vacancy.salaryFrom")
    @Mapping(target = "salaryTo", source = "vacancy.salaryTo")
    @Mapping(target = "recruiterId", source = "vacancy.recruiterId")
    ApplicationDetailsResponseDto toApplicationDetailsDto(Application application);

    default String mapCandidateSalary(Application application, Map<String, UserFeignDto> profileInfo) {
        UserFeignDto user = profileInfo.get(application.getCandidateId());
        return user != null ? user.salary() : null;
    }

    default String mapCandidateWorkMode(Application application, Map<String, UserFeignDto> profileInfo) {
        UserFeignDto user = profileInfo.get(application.getCandidateId());
        return user != null ? user.workMode() : null;
    }

    default Integer mapCandidateAvailableHours(Application application, Map<String, UserFeignDto> profileInfo) {
        UserFeignDto user = profileInfo.get(application.getCandidateId());
        return user != null ? user.availableHoursPerWeek() : null;
    }

    default String mapCandidateAvailableFrom(Application application, Map<String, UserFeignDto> profileInfo) {
        UserFeignDto user = profileInfo.get(application.getCandidateId());
        return user != null ? user.availableFrom() : null;
    }

    default String mapCandidateDisplayName(Application application, Map<String, UserFeignDto> profileInfo) {
        UserFeignDto user = profileInfo.get(application.getCandidateId());
        return user != null ? user.displayName() : null;
    }
}
