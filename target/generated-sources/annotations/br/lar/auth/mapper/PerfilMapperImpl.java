package br.lar.auth.mapper;

import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.local.auth.dto.PerfilDTO;
import br.local.auth.mapper.ModuloMapper;
import br.local.auth.mapper.PerfilMapper;
import br.local.auth.model.Perfil;
import br.local.auth.utils.AtivoInativo;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:53:39-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
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
