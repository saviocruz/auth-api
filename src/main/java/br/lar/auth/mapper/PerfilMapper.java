package br.lar.auth.mapper;

import org.mapstruct.Mapper;

import br.lar.auth.dto.PerfilDTO;
import br.lar.auth.model.Perfil;

/**
 * MapStruct Mapper para converter entre Perfil e PerfilDTO
 */
@Mapper(uses = ModuloMapper.class, componentModel = "spring")
public interface PerfilMapper {

	PerfilDTO toDTO(Perfil perfil);

	Perfil toEntity(PerfilDTO perfilDTO);
}
