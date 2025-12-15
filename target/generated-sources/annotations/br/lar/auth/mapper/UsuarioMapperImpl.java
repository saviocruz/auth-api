package br.lar.auth.mapper;

import br.lar.auth.dto.ModuloDTO;
import br.lar.auth.dto.PerfilDTO;
import br.lar.auth.dto.UnidadeDTO;
import br.lar.auth.dto.UsuarioDTO;
import br.lar.auth.model.Modulo;
import br.lar.auth.model.Perfil;
import br.lar.auth.model.Unidade;
import br.lar.auth.model.Usuario;
import br.lar.auth.utils.AtivoInativo;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-15T08:04:12-0400",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

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

    protected ModuloDTO moduloToModuloDTO(Modulo modulo) {
        if ( modulo == null ) {
            return null;
        }

        ModuloDTO.ModuloDTOBuilder moduloDTO = ModuloDTO.builder();

        moduloDTO.dataCadastro( modulo.getDataCadastro() );
        moduloDTO.descricao( modulo.getDescricao() );
        moduloDTO.emailResponsavel( modulo.getEmailResponsavel() );
        moduloDTO.id( modulo.getId() );
        moduloDTO.status( modulo.getStatus() );

        return moduloDTO.build();
    }

    protected PerfilDTO perfilToPerfilDTO(Perfil perfil) {
        if ( perfil == null ) {
            return null;
        }

        PerfilDTO.PerfilDTOBuilder perfilDTO = PerfilDTO.builder();

        perfilDTO.descricao( perfil.getDescricao() );
        perfilDTO.id( perfil.getId() );
        perfilDTO.modulo( moduloToModuloDTO( perfil.getModulo() ) );
        perfilDTO.nome( perfil.getNome() );
        if ( perfil.getStatus() != null ) {
            perfilDTO.status( perfil.getStatus().name() );
        }

        return perfilDTO.build();
    }

    protected Set<PerfilDTO> perfilSetToPerfilDTOSet(Set<Perfil> set) {
        if ( set == null ) {
            return null;
        }

        Set<PerfilDTO> set1 = new LinkedHashSet<PerfilDTO>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( Perfil perfil : set ) {
            set1.add( perfilToPerfilDTO( perfil ) );
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

    protected Modulo moduloDTOToModulo(ModuloDTO moduloDTO) {
        if ( moduloDTO == null ) {
            return null;
        }

        Modulo modulo = new Modulo();

        modulo.setDataCadastro( moduloDTO.getDataCadastro() );
        modulo.setDescricao( moduloDTO.getDescricao() );
        modulo.setEmailResponsavel( moduloDTO.getEmailResponsavel() );
        modulo.setId( moduloDTO.getId() );
        modulo.setStatus( moduloDTO.getStatus() );

        return modulo;
    }

    protected Perfil perfilDTOToPerfil(PerfilDTO perfilDTO) {
        if ( perfilDTO == null ) {
            return null;
        }

        Perfil perfil = new Perfil();

        perfil.setDescricao( perfilDTO.getDescricao() );
        perfil.setId( perfilDTO.getId() );
        perfil.setModulo( moduloDTOToModulo( perfilDTO.getModulo() ) );
        perfil.setNome( perfilDTO.getNome() );
        if ( perfilDTO.getStatus() != null ) {
            perfil.setStatus( Enum.valueOf( AtivoInativo.class, perfilDTO.getStatus() ) );
        }

        return perfil;
    }

    protected Set<Perfil> perfilDTOSetToPerfilSet(Set<PerfilDTO> set) {
        if ( set == null ) {
            return null;
        }

        Set<Perfil> set1 = new LinkedHashSet<Perfil>( Math.max( (int) ( set.size() / .75f ) + 1, 16 ) );
        for ( PerfilDTO perfilDTO : set ) {
            set1.add( perfilDTOToPerfil( perfilDTO ) );
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
