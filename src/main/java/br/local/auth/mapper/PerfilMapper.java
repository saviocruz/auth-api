package br.local.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.local.auth.dto.PerfilDTO;
import br.local.auth.model.Perfil;

/**
 * MapStruct Mapper para converter entre Perfil e PerfilDTO
 */
@Mapper(uses = ModuloMapper.class, componentModel = "spring")
public interface PerfilMapper {

	@Mapping(target = "funcionalidades", ignore = true)
	PerfilDTO toDTO(Perfil perfil);

	Perfil toEntity(PerfilDTO perfilDTO);
}
