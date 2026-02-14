package tr.gov.tuketbir.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import tr.gov.tuketbir.domain.entity.Uye;
import tr.gov.tuketbir.dto.uye.UyeCreateRequest;
import tr.gov.tuketbir.dto.uye.UyeDTO;
import tr.gov.tuketbir.dto.uye.UyeUpdateRequest;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface UyeMapper {

    @Mapping(target = "birlikId", source = "birlik.id")
    @Mapping(target = "birlikAdi", source = "birlik.birlikAdi")
    @Mapping(target = "birlikKodu", source = "birlik.birlikKodu")
    UyeDTO toDTO(Uye uye);

    Uye toEntity(UyeCreateRequest request);

    void updateEntity(UyeUpdateRequest request, @MappingTarget Uye uye);
}
