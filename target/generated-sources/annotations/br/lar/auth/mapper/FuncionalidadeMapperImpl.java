package br.lar.auth.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

import br.local.auth.dto.FuncionalidadeDTO;
import br.local.auth.mapper.FuncionalidadeMapper;
import br.local.auth.model.Funcionalidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:53:38-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class FuncionalidadeMapperImpl implements FuncionalidadeMapper {

    @Override
    public FuncionalidadeDTO toDTO(Funcionalidade funcionalidade) {
        if ( funcionalidade == null ) {
            return null;
        }

        FuncionalidadeDTO.FuncionalidadeDTOBuilder funcionalidadeDTO = FuncionalidadeDTO.builder();

        funcionalidadeDTO.descricao( funcionalidade.getDescricao() );
        funcionalidadeDTO.id( funcionalidade.getId() );
        funcionalidadeDTO.nome( funcionalidade.getNome() );
        funcionalidadeDTO.status( funcionalidade.getStatus() );

        return funcionalidadeDTO.build();
    }

    @Override
    public Funcionalidade toEntity(FuncionalidadeDTO funcionalidadeDTO) {
        if ( funcionalidadeDTO == null ) {
            return null;
        }

        Funcionalidade funcionalidade = new Funcionalidade();

        funcionalidade.setDescricao( funcionalidadeDTO.getDescricao() );
        funcionalidade.setId( funcionalidadeDTO.getId() );
        funcionalidade.setNome( funcionalidadeDTO.getNome() );
        funcionalidade.setStatus( funcionalidadeDTO.getStatus() );

        return funcionalidade;
    }
}
