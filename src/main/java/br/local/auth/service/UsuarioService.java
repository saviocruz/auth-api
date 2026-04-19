package br.local.auth.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.local.auth.dto.UsuarioDTO;

/**
 * Interface de serviço para gerência de usuários.
 *
 * Responsabilidades:
 * 1. CRUD de usuários
 * 2. Filtrar usuários por critérios
 * 3. Gerenciar perfis de usuário
 * 4. Gerenciar unidades de usuário
 * 5. Bloquear/desbloquear usuários
 * 6. Alterar senhas
 * 7. Registrar eventos em auditoria
 */
public interface UsuarioService {

	/**
	 * Listar todos os usuários com paginação e filtros
	 *
	 * @param pageable Informações de paginação (página, tamanho, ordenação)
	 * @param status Filtro opcional por status (ATIVO, INATIVO, BLOQUEADO)
	 * @param busca Filtro opcional por username/nome/cpf/email
	 * @return Página de UsuarioDTO
	 */
	Page<UsuarioDTO> listarUsuarios(Pageable pageable, String status, String busca);

	/**
	 * Obter detalhes de um usuário específico
	 *
	 * @param usuarioId ID do usuário
	 * @return UsuarioDTO com dados completos
	 * @throws RuntimeException Se usuário não encontrado
	 */
	UsuarioDTO obterUsuario(Long usuarioId);

	/**
	 * Criar novo usuário
	 *
	 * @param usuarioDTO Dados do usuário a criar
	 * @return UsuarioDTO criado com ID gerado
	 * @throws RuntimeException Se dados inválidos ou duplicados
	 */
	UsuarioDTO criarUsuario(UsuarioDTO usuarioDTO);

	/**
	 * Atualizar dados de um usuário existente
	 *
	 * @param usuarioId ID do usuário
	 * @param usuarioDTO Dados atualizados
	 * @return UsuarioDTO atualizado
	 * @throws RuntimeException Se usuário não encontrado
	 */
	UsuarioDTO atualizarUsuario(Long usuarioId, UsuarioDTO usuarioDTO);

	/**
	 * Deletar (soft delete) um usuário
	 * Muda status para INATIVO em vez de deletar fisicamente
	 *
	 * @param usuarioId ID do usuário
	 * @throws RuntimeException Se usuário não encontrado
	 */
	void deletarUsuario(Long usuarioId);

	/**
	 * Alterar perfis (roles) de um usuário
	 *
	 * @param usuarioId ID do usuário
	 * @param perfilIds IDs dos perfis a atribuir
	 * @return UsuarioDTO com perfis atualizados
	 * @throws RuntimeException Se usuário ou perfil não encontrado
	 */
	UsuarioDTO alterarPerfis(Long usuarioId, List<Long> perfilIds);

	/**
	 * Alterar unidades de um usuário
	 *
	 * @param usuarioId ID do usuário
	 * @param unidadeIds IDs das unidades a atribuir
	 * @return UsuarioDTO com unidades atualizadas
	 * @throws RuntimeException Se usuário ou unidade não encontrado
	 */
	UsuarioDTO alterarUnidades(Long usuarioId, List<Long> unidadeIds);

	/**
	 * Bloquear um usuário (status = BLOQUEADO)
	 *
	 * @param usuarioId ID do usuário
	 * @return UsuarioDTO com status BLOQUEADO
	 * @throws RuntimeException Se usuário não encontrado
	 */
	UsuarioDTO bloquearUsuario(Long usuarioId);

	/**
	 * Desbloquear um usuário (status = ATIVO, resetar tentativas)
	 *
	 * @param usuarioId ID do usuário
	 * @return UsuarioDTO com status ATIVO
	 * @throws RuntimeException Se usuário não encontrado
	 */
	UsuarioDTO desbloquearUsuario(Long usuarioId);

	/**
	 * Alterar senha de um usuário
	 *
	 * @param usuarioId ID do usuário
	 * @param senhaAtual Senha atual para validação
	 * @param novaSenha Nova senha (será hashada com BCrypt)
	 * @throws RuntimeException Se usuário não encontrado ou senha inválida
	 */
	void alterarSenha(Long usuarioId, String senhaAtual, String novaSenha);

	/**
	 * Resetar senha de um usuário para uma padrão
	 * (para administradores forçarem reset)
	 *
	 * @param usuarioId ID do usuário
	 * @param senhaPadrao Senha padrão a aplicar
	 * @throws RuntimeException Se usuário não encontrado
	 */
	void resetarSenha(Long usuarioId, String senhaPadrao);

	/**
	 * Obter histórico de logins de um usuário
	 *
	 * @param usuarioId ID do usuário
	 * @param limite Número máximo de registros a retornar
	 * @return Lista de eventos de login
	 */
	List<java.util.Map<String, Object>> obterHistoricoLogins(Long usuarioId, int limite);

	/**
	 * Incrementar tentativas falhas de login
	 * (chamado internamente pelo CustomAuthenticationProvider)
	 *
	 * @param usuarioId ID do usuário
	 */
	void incrementarTentativasFalhas(Long usuarioId);

	/**
	 * Resetar tentativas falhas de login
	 * (chamado internamente após login bem-sucedido)
	 *
	 * @param usuarioId ID do usuário
	 */
	void resetarTentativasFalhas(Long usuarioId);

	/**
	 * Verificar se um username já existe
	 *
	 * @param username Username a verificar
	 * @return true se existe, false caso contrário
	 */
	boolean usernameExiste(String username);

	/**
	 * Verificar se um CPF já existe
	 *
	 * @param cpf CPF a verificar
	 * @return true se existe, false caso contrário
	 */
	boolean cpfExiste(String cpf);

	/**
	 * Verificar se um email já existe
	 *
	 * @param email Email a verificar
	 * @return true se existe, false caso contrário
	 */
	boolean emailExiste(String email);
}
