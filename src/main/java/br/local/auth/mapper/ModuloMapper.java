package br.local.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import br.local.auth.dto.ModuloDTO;
import br.local.auth.model.Modulo;

/**
 * MapStruct Mapper para converter entre Modulo e ModuloDTO
 */
@Mapper(componentModel = "spring")
public interface ModuloMapper {

	ModuloDTO toDTO(Modulo modulo);

	@Mapping(target = "nome", ignore = true)
	Modulo toEntity(ModuloDTO moduloDTO);
}
