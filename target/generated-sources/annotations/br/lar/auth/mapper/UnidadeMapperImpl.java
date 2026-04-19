package br.lar.auth.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

import br.local.auth.dto.UnidadeDTO;
import br.local.auth.mapper.UnidadeMapper;
import br.local.auth.model.Unidade;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:53:39-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UnidadeMapperImpl implements UnidadeMapper {

    @Override
    public UnidadeDTO toDTO(Unidade unidade) {
        if ( unidade == null ) {
            return null;
        }

        UnidadeDTO.UnidadeDTOBuilder unidadeDTO = UnidadeDTO.builder();

        unidadeDTO.bairro( unidade.getBairro() );
        unidadeDTO.cep( unidade.getCep() );
        unidadeDTO.descricao( unidade.getDescricao() );
        unidadeDTO.email( unidade.getEmail() );
        unidadeDTO.endereco( unidade.getEndereco() );
        unidadeDTO.id( unidade.getId() );
        if ( unidade.getNumero() != null ) {
            unidadeDTO.numero( String.valueOf( unidade.getNumero() ) );
        }
        unidadeDTO.ordenacao( unidade.getOrdenacao() );
        unidadeDTO.sigla( unidade.getSigla() );
        unidadeDTO.situacao( unidade.getSituacao() );
        unidadeDTO.telefone( unidade.getTelefone() );
        unidadeDTO.unidadeSuperior( toDTO( unidade.getUnidadeSuperior() ) );

        return unidadeDTO.build();
    }

    @Override
    public Unidade toEntity(UnidadeDTO unidadeDTO) {
        if ( unidadeDTO == null ) {
            return null;
        }

        Unidade unidade = new Unidade();

        unidade.setBairro( unidadeDTO.getBairro() );
        unidade.setCep( unidadeDTO.getCep() );
        unidade.setDescricao( unidadeDTO.getDescricao() );
        unidade.setEmail( unidadeDTO.getEmail() );
        unidade.setEndereco( unidadeDTO.getEndereco() );
        unidade.setId( unidadeDTO.getId() );
        if ( unidadeDTO.getNumero() != null ) {
            unidade.setNumero( Long.parseLong( unidadeDTO.getNumero() ) );
        }
        unidade.setOrdenacao( unidadeDTO.getOrdenacao() );
        unidade.setSigla( unidadeDTO.getSigla() );
        unidade.setSituacao( unidadeDTO.getSituacao() );
        unidade.setTelefone( unidadeDTO.getTelefone() );
        unidade.setUnidadeSuperior( toEntity( unidadeDTO.getUnidadeSuperior() ) );

        return unidade;
    }
}
