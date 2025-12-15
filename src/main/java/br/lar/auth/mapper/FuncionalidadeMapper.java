package br.lar.auth.mapper;

import org.mapstruct.Mapper;

import br.lar.auth.dto.FuncionalidadeDTO;
import br.lar.auth.model.Funcionalidade;

/**
 * MapStruct Mapper para converter entre Funcionalidade e FuncionalidadeDTO
 */
@Mapper(componentModel = "spring")
public interface FuncionalidadeMapper {

	FuncionalidadeDTO toDTO(Funcionalidade funcionalidade);

	Funcionalidade toEntity(FuncionalidadeDTO funcionalidadeDTO);
}
