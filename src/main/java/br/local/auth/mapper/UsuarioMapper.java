package br.local.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.local.auth.dto.UsuarioDTO;
import br.local.auth.model.Usuario;

/**
 * MapStruct Mapper para converter entre Usuario e UsuarioDTO
 */
@Mapper(uses = PerfilMapper.class, componentModel = "spring")
public interface UsuarioMapper {

	/**
	 * Converter de Usuario para UsuarioDTO
	 */
	UsuarioDTO toDTO(Usuario usuario);

	/**
	 * Converter de UsuarioDTO para Usuario
	 */
	@Mapping(target = "chave", ignore = true)
	@Mapping(target = "tentativasFalhas", ignore = true)
	Usuario toEntity(UsuarioDTO usuarioDTO);
}
