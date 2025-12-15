package br.lar.auth.mapper;

import org.mapstruct.Mapper;

import br.lar.auth.dto.UnidadeDTO;
import br.lar.auth.model.Unidade;

/**
 * MapStruct Mapper para converter entre Unidade e UnidadeDTO
 */
@Mapper(componentModel = "spring")
public interface UnidadeMapper {

	UnidadeDTO toDTO(Unidade unidade);

	Unidade toEntity(UnidadeDTO unidadeDTO);
}
