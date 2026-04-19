package br.local.auth.mapper;

import org.mapstruct.Mapper;

import br.local.auth.dto.UnidadeDTO;
import br.local.auth.model.Unidade;

/**
 * MapStruct Mapper para converter entre Unidade e UnidadeDTO
 */
@Mapper(componentModel = "spring")
public interface UnidadeMapper {

	UnidadeDTO toDTO(Unidade unidade);

	Unidade toEntity(UnidadeDTO unidadeDTO);
}
