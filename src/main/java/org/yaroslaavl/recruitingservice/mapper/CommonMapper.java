package org.yaroslaavl.recruitingservice.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.yaroslaavl.recruitingservice.feignClient.dto.CompanyPreviewFeignDto;

import java.util.Map;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CommonMapper {

    @Named("companyName")
    default String getCompanyName(UUID companyId, @Context Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        CompanyPreviewFeignDto preview = previewInfo.get(companyId);
        return preview != null ? preview.name() : null;
    }

    @Named("companyLogoUrl")
    default String getCompanyLogoUrl(UUID companyId, @Context Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        CompanyPreviewFeignDto preview = previewInfo.get(companyId);
        return preview != null ? preview.logoUrl() : null;
    }

    @Named("companyId")
    default UUID getCompanyId(UUID companyId, @Context Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        CompanyPreviewFeignDto preview = previewInfo.get(companyId);
        return preview != null ? preview.id() : null;
    }

    @Named("companyLocation")
    default String getCompanyLocation(UUID companyId, @Context Map<UUID, CompanyPreviewFeignDto> previewInfo) {
        CompanyPreviewFeignDto companyPreviewFeignDto = previewInfo.get(companyId);
        return companyPreviewFeignDto != null ? companyPreviewFeignDto.location() : null;
    }
}
