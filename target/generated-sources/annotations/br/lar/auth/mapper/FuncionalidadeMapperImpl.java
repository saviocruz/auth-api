package br.lar.auth.mapper;

import br.lar.auth.dto.FuncionalidadeDTO;
import br.lar.auth.model.Funcionalidade;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-15T08:04:12-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
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
