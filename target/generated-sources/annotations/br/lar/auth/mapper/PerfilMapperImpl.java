package br.lar.auth.mapper;

import br.lar.auth.dto.PerfilDTO;
import br.lar.auth.model.Perfil;
import br.lar.auth.utils.AtivoInativo;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-15T08:04:12-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class PerfilMapperImpl implements PerfilMapper {

    @Autowired
    private ModuloMapper moduloMapper;

    @Override
    public PerfilDTO toDTO(Perfil perfil) {
        if ( perfil == null ) {
            return null;
        }

        PerfilDTO.PerfilDTOBuilder perfilDTO = PerfilDTO.builder();

        perfilDTO.descricao( perfil.getDescricao() );
        perfilDTO.id( perfil.getId() );
        perfilDTO.modulo( moduloMapper.toDTO( perfil.getModulo() ) );
        perfilDTO.nome( perfil.getNome() );
        if ( perfil.getStatus() != null ) {
            perfilDTO.status( perfil.getStatus().name() );
        }

        return perfilDTO.build();
    }

    @Override
    public Perfil toEntity(PerfilDTO perfilDTO) {
        if ( perfilDTO == null ) {
            return null;
        }

        Perfil perfil = new Perfil();

        perfil.setDescricao( perfilDTO.getDescricao() );
        perfil.setId( perfilDTO.getId() );
        perfil.setModulo( moduloMapper.toEntity( perfilDTO.getModulo() ) );
        perfil.setNome( perfilDTO.getNome() );
        if ( perfilDTO.getStatus() != null ) {
            perfil.setStatus( Enum.valueOf( AtivoInativo.class, perfilDTO.getStatus() ) );
        }

        return perfil;
    }
}
