package com.traxup.tplug.erp.usuario;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UsuarioApplicationService {

    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioApplicationService(
            UsuarioRepository usuarioRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listar(UUID tenantId) {
        validarTenant(tenantId);
        return usuarioRepository.findAllByTenantId(tenantId);
    }

    public Usuario buscarPorId(UUID tenantId, UUID usuarioId) {
        return usuarioRepository.findByIdAndTenantId(usuarioId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado para o tenant informado"));
    }

    public Usuario buscarPorEmail(UUID tenantId, String email) {
        String emailNormalizado = normalizarEmail(email);
        return usuarioRepository.findByTenantIdAndEmailIgnoreCase(tenantId, emailNormalizado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuario nao encontrado para o tenant informado"));
    }

    @Transactional
    public Usuario criar(UUID tenantId, String nome, String email, String senha) {
        Tenant tenant = validarTenant(tenantId);
        String emailNormalizado = normalizarEmail(email);

        if (usuarioRepository.existsByTenantIdAndEmailIgnoreCase(tenantId, emailNormalizado)) {
            throw new RecursoConflitanteException("Ja existe usuario com este email no tenant informado");
        }

        String senhaHash = passwordEncoder.encode(senha);
        Usuario usuario = new Usuario(tenant, nome.trim(), emailNormalizado, senhaHash);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario desativar(UUID tenantId, UUID usuarioId) {
        Usuario usuario = buscarPorId(tenantId, usuarioId);
        usuario.desativar();
        return usuarioRepository.save(usuario);
    }

    private Tenant validarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant nao encontrado"));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
