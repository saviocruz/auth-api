package br.lar.auth.mapper;

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.local.auth.dto.PerfilDTO;
import br.local.auth.dto.UnidadeDTO;
import br.local.auth.dto.UsuarioDTO;
import br.local.auth.mapper.PerfilMapper;
import br.local.auth.mapper.UsuarioMapper;
import br.local.auth.model.Perfil;
import br.local.auth.model.Unidade;
import br.local.auth.model.Usuario;
import br.local.auth.utils.AtivoInativo;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-18T12:53:39-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

    @Autowired
    private PerfilMapper perfilMapper;

    @Override
    public UsuarioDTO toDTO(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }

        UsuarioDTO.UsuarioDTOBuilder usuarioDTO = UsuarioDTO.builder();

        usuarioDTO.alterar( usuario.getAlterar() );
        usuarioDTO.cpf( usuario.getCpf() );
        usuarioDTO.dataCadastro( usuario.getDataCadastro() );
        usuarioDTO.dataUltimoLogin( usuario.getDataUltimoLogin() );
        usuarioDTO.email( usuario.getEmail() );
        usuarioDTO.id( usuario.getId() );
        usuarioDTO.matricula( usuario.getMatricula() );
        usuarioDTO.nome( usuario.getNome() );
        usuarioDTO.perfis( perfilSetToPerfilDTOSet( usuario.getPerfis() ) );
        if ( usuario.getStatus() != null ) {
            usuarioDTO.status( usuario.getStatus().name() );
        }
        usuarioDTO.unidades( unidadeSetToUnidadeDTOSet( usuario.getUnidades() ) );
        usuarioDTO.username( usuario.getUsername() );

        return usuarioDTO.build();
    }

    @Override
    public Usuario toEntity(UsuarioDTO usuarioDTO) {
        if ( usuarioDTO == null ) {
            return null;
        }

        Usuario usuario = new Usuario();

        usuario.setAlterar( usuarioDTO.getAlterar() );
        usuario.setCpf( usuarioDTO.getCpf() );
        usuario.setDataCadastro( usuarioDTO.getDataCadastro() );
        usuario.setDataUltimoLogin( usuarioDTO.getDataUltimoLogin() );
        usuario.setEmail( usuarioDTO.getEmail() );
        usuario.setId( usuarioDTO.getId() );
        usuario.setMatricula( usuarioDTO.getMatricula() );
        usuario.setNome( usuarioDTO.getNome() );
        if ( usuarioDTO.getStatus() != null ) {
            usuario.setStatus( Enum.valueOf( AtivoInativo.class, usuarioDTO.getStatus() ) );
        }
        usuario.setUsername( usuarioDTO.getUsername() );
        usuario.setPerfis( perfilDTOSetToPerfilSet( usuarioDTO.getPerfis() ) );
        usuario.setUnidades( unidadeDTOSetToUnidadeSet( usuarioDTO.getUnidades() ) );

        return usuario;
    }

    protected Set<PerfilDTO> perfilSetToPerfilDTOSet(Set<Perfil> set) {
        if ( set == null ) {
            return null;
        }

        Set<PerfilDTO> set1 = new LinkedHashSet<PerfilDTO>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Perfil perfil : set ) {
            set1.add( perfilMapper.toDTO( perfil ) );
        }

        return set1;
    }

    protected UnidadeDTO unidadeToUnidadeDTO(Unidade unidade) {
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
        unidadeDTO.unidadeSuperior( unidadeToUnidadeDTO( unidade.getUnidadeSuperior() ) );

        return unidadeDTO.build();
    }

    protected Set<UnidadeDTO> unidadeSetToUnidadeDTOSet(Set<Unidade> set) {
        if ( set == null ) {
            return null;
        }

        Set<UnidadeDTO> set1 = new LinkedHashSet<UnidadeDTO>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Unidade unidade : set ) {
            set1.add( unidadeToUnidadeDTO( unidade ) );
        }

        return set1;
    }

    protected Set<Perfil> perfilDTOSetToPerfilSet(Set<PerfilDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Perfil> set1 = new LinkedHashSet<Perfil>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( PerfilDTO perfilDTO : set ) {
            set1.add( perfilMapper.toEntity( perfilDTO ) );
        }

        return set1;
    }

    protected Unidade unidadeDTOToUnidade(UnidadeDTO unidadeDTO) {
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
        unidade.setUnidadeSuperior( unidadeDTOToUnidade( unidadeDTO.getUnidadeSuperior() ) );

        return unidade;
    }

    protected Set<Unidade> unidadeDTOSetToUnidadeSet(Set<UnidadeDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Unidade> set1 = new LinkedHashSet<Unidade>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( UnidadeDTO unidadeDTO : set ) {
            set1.add( unidadeDTOToUnidade( unidadeDTO ) );
        }

        return set1;
    }
}
