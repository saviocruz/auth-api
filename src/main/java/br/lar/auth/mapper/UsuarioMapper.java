package br.lar.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.lar.auth.dto.UsuarioDTO;
import br.lar.auth.model.Usuario;

/**
 * MapStruct Mapper para converter entre Usuario e UsuarioDTO
 */
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

	/**
	 * Converter de Usuario para UsuarioDTO
	 */
	UsuarioDTO toDTO(Usuario usuario);

	/**
	 * Converter de UsuarioDTO para Usuario
	 */
	@Mapping(target = "chave", ignore = true)
	Usuario toEntity(UsuarioDTO usuarioDTO);
}
