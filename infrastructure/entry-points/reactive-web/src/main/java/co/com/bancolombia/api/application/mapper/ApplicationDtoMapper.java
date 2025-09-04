package co.com.bancolombia.api.application.mapper;

import co.com.bancolombia.api.application.dto.ApplicationResponseDataDto;
import co.com.bancolombia.api.application.dto.CreateApplicationDto;
import co.com.bancolombia.model.application.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationDtoMapper {

    @Mapping(source ="term", target = "term",dateFormat = "yyyy-MM-dd")
    @Mapping(target = "idState", ignore = true)
    @Mapping(target = "idTypeLoan", ignore = true)
    @Mapping(target = "id", ignore = true)
    Application toApplication(CreateApplicationDto createApplicationDto);

    ApplicationResponseDataDto toResponseData(Application application);

}
