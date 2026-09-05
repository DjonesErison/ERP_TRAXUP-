package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.perfil.Perfil;
import com.traxup.tplug.erp.perfil.PerfilRepository;
import com.traxup.tplug.erp.permissao.Permissao;
import com.traxup.tplug.erp.permissao.PermissaoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RbacApplicationService {

    private final TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;
    private final AutorizacaoRepository autorizacaoRepository;
    private final JdbcTemplate jdbcTemplate;

    public RbacApplicationService(
            TenantRepository tenantRepository,
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PermissaoRepository permissaoRepository,
            AutorizacaoRepository autorizacaoRepository,
            JdbcTemplate jdbcTemplate) {
        this.tenantRepository = tenantRepository;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.permissaoRepository = permissaoRepository;
        this.autorizacaoRepository = autorizacaoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Perfil> listarPerfis(UUID tenantId) {
        validarTenant(tenantId);
        return perfilRepository.findAllByTenantId(tenantId);
    }

    public List<String> listarPermissoesDoUsuario(UUID tenantId, UUID usuarioId) {
        validarUsuario(tenantId, usuarioId);
        return autorizacaoRepository.listarPermissoesEfetivas(tenantId, usuarioId);
    }

    @Transactional
    public Perfil criarPerfil(UUID tenantId, String nome, String descricao) {
        Tenant tenant = validarTenant(tenantId);
        if (perfilRepository.existsByTenantIdAndNomeIgnoreCase(tenantId, nome)) {
            throw new RecursoConflitanteException("Ja existe um perfil com esse nome no tenant");
        }
        return perfilRepository.save(new Perfil(tenant, nome, descricao));
    }

    @Transactional
    public Permissao criarPermissao(String chave, String descricao) {
        String chaveNormalizada = chave.trim().toUpperCase();
        if (permissaoRepository.existsByChave(chaveNormalizada)) {
            throw new RecursoConflitanteException("Permissao ja cadastrada");
        }
        return permissaoRepository.save(new Permissao(chaveNormalizada, descricao));
    }

    @Transactional
    public void atribuirPerfilAoUsuario(UUID tenantId, UUID usuarioId, UUID perfilId) {
        validarUsuario(tenantId, usuarioId);
        validarPerfil(tenantId, perfilId);
        jdbcTemplate.update("""
                INSERT INTO usuario_perfis (tenant_id, usuario_id, perfil_id)
                VALUES (?, ?, ?)
                ON CONFLICT DO NOTHING
                """, tenantId, usuarioId, perfilId);
    }

    @Transactional
    public void atribuirPermissaoAoPerfil(UUID tenantId, UUID perfilId, UUID permissaoId) {
        validarPerfil(tenantId, perfilId);
        permissaoRepository.findById(permissaoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Permissao nao encontrada"));
        jdbcTemplate.update("""
                INSERT INTO perfil_permissoes (tenant_id, perfil_id, permissao_id)
                VALUES (?, ?, ?)
                ON CONFLICT DO NOTHING
                """, tenantId, perfilId, permissaoId);
    }

    private Tenant validarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant nao encontrado"));
    }

    private Usuario validarUsuario(UUID tenantId, UUID usuarioId) {
        return usuarioRepository.findByIdAndTenantId(usuarioId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado para o tenant informado"));
    }

    private Perfil validarPerfil(UUID tenantId, UUID perfilId) {
        return perfilRepository.findByIdAndTenantId(perfilId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Perfil nao encontrado para o tenant informado"));
    }
}
