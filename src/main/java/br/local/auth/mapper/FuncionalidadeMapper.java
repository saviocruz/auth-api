package br.local.auth.mapper;

import org.mapstruct.Mapper;

import br.local.auth.dto.FuncionalidadeDTO;
import br.local.auth.model.Funcionalidade;

/**
 * MapStruct Mapper para converter entre Funcionalidade e FuncionalidadeDTO
 */
@Mapper(componentModel = "spring")
public interface FuncionalidadeMapper {

	FuncionalidadeDTO toDTO(Funcionalidade funcionalidade);

	Funcionalidade toEntity(FuncionalidadeDTO funcionalidadeDTO);
}
