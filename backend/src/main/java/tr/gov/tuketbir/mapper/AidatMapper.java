package tr.gov.tuketbir.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import tr.gov.tuketbir.domain.entity.Aidat;
import tr.gov.tuketbir.domain.entity.AidatDonemi;
import tr.gov.tuketbir.domain.entity.Tahsilat;
import tr.gov.tuketbir.dto.aidat.AidatDTO;
import tr.gov.tuketbir.dto.aidat.AidatDonemiCreateRequest;
import tr.gov.tuketbir.dto.aidat.AidatDonemiDTO;
import tr.gov.tuketbir.dto.aidat.TahsilatCreateRequest;
import tr.gov.tuketbir.dto.aidat.TahsilatDTO;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public interface AidatMapper {

    @Mapping(target = "uyeId", source = "uye.id")
    @Mapping(target = "uyeNo", source = "uye.uyeNo")
    @Mapping(target = "uyeAdSoyad", expression = "java(aidat.getUye().getAd() + \" \" + aidat.getUye().getSoyad())")
    @Mapping(target = "aidatDonemiId", source = "aidatDonemi.id")
    @Mapping(target = "donemAdi", source = "aidatDonemi.donemAdi")
    @Mapping(target = "yil", source = "aidatDonemi.yil")
    @Mapping(target = "birlikId", source = "birlik.id")
    @Mapping(target = "birlikAdi", source = "birlik.birlikAdi")
    @Mapping(target = "durumu", source = "aidatDurum")
    @Mapping(target = "kalanTutar", source = "kalanBorc")
    @Mapping(target = "gecikmeTutari", source = "gecikmeFaizi")
    @Mapping(target = "tahakkukTutari", source = "tahakkukTutari")
    AidatDTO toDTO(Aidat aidat);

    @Mapping(target = "birlikId", source = "birlik.id")
    @Mapping(target = "birlikAdi", source = "birlik.birlikAdi")
    @Mapping(target = "aktif", source = "donemAktif")
    AidatDonemiDTO toDTO(AidatDonemi donem);

    @Mapping(target = "donemAktif", constant = "true")
    AidatDonemi toEntity(AidatDonemiCreateRequest request);

    @Mapping(target = "aidatId", source = "aidat.id")
    @Mapping(target = "uyeId", source = "uye.id")
    @Mapping(target = "uyeNo", source = "uye.uyeNo")
    @Mapping(target = "uyeAdSoyad", expression = "java(tahsilat.getUye().getAd() + \" \" + tahsilat.getUye().getSoyad())")
    @Mapping(target = "donemAdi", source = "aidat.aidatDonemi.donemAdi")
    TahsilatDTO toTahsilatDTO(Tahsilat tahsilat);

    Tahsilat toEntity(TahsilatCreateRequest request);
}
