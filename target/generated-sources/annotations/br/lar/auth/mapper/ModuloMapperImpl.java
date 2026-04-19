package br.lar.auth.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

import br.local.auth.dto.ModuloDTO;
import br.local.auth.mapper.ModuloMapper;
import br.local.auth.model.Modulo;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:53:39-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
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
        moduloDTO.nome( modulo.getNome() );
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
