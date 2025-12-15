package br.lar.auth.mapper;

import br.lar.auth.dto.ModuloDTO;
import br.lar.auth.model.Modulo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-15T08:04:12-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class ModuloMapperImpl implements ModuloMapper {

    @Override
    public ModuloDTO toDTO(Modulo modulo) {
        if ( modulo == null ) {
            return null;
        }

        ModuloDTO.ModuloDTOBuilder moduloDTO = ModuloDTO.builder();

        moduloDTO.dataCadastro( modulo.getDataCadastro() );
        moduloDTO.descricao( modulo.getDescricao() );
        moduloDTO.emailResponsavel( modulo.getEmailResponsavel() );
        moduloDTO.id( modulo.getId() );
        moduloDTO.status( modulo.getStatus() );

        return moduloDTO.build();
    }

    @Override
    public Modulo toEntity(ModuloDTO moduloDTO) {
        if ( moduloDTO == null ) {
            return null;
        }

        Modulo modulo = new Modulo();

        modulo.setDataCadastro( moduloDTO.getDataCadastro() );
        modulo.setDescricao( moduloDTO.getDescricao() );
        modulo.setEmailResponsavel( moduloDTO.getEmailResponsavel() );
        modulo.setId( moduloDTO.getId() );
        modulo.setStatus( moduloDTO.getStatus() );

        return modulo;
    }
}
