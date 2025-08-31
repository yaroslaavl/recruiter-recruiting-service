package org.yaroslaavl.recruitingservice.mapper;

import org.mapstruct.Mapper;
import org.yaroslaavl.recruitingservice.database.entity.Application;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationDetailsResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationResponseDto;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    ApplicationResponseDto toApplicationDto(Application application);

    ApplicationDetailsResponseDto toApplicationDetailsDto(Application application);
}
