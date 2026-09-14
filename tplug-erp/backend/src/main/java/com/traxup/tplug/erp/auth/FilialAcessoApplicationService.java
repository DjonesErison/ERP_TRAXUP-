package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import com.traxup.tplug.erp.usuario.UsuarioRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FilialAcessoApplicationService {
    private static final int LIMITE_FILIAIS = 200;

    private final JdbcTemplate jdbc;
    private final UsuarioRepository usuarioRepository;
    private final FilialRepository filialRepository;
    private final AuditoriaApplicationService auditoria;

    public FilialAcessoApplicationService(
            JdbcTemplate jdbc,
            UsuarioRepository usuarioRepository,
            FilialRepository filialRepository,
            AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc;
        this.usuarioRepository = usuarioRepository;
        this.filialRepository = filialRepository;
        this.auditoria = auditoria;
    }

    @Transactional(readOnly = true)
    public AcessoFiliais listar(UUID tenantId, UUID usuarioId) {
        validarUsuario(tenantId, usuarioId);
        List<UUID> filiais = jdbc.queryForList("""
                SELECT filial_id
                FROM usuario_filiais
                WHERE tenant_id = ? AND usuario_id = ?
                ORDER BY filial_id
                """, UUID.class, tenantId, usuarioId);
        return new AcessoFiliais(usuarioId, filiais);
    }

    @Transactional
    public AcessoFiliais substituir(UUID tenantId, UUID atorId,
                                    UUID usuarioId, List<UUID> filialIds) {
        validarUsuario(tenantId, usuarioId);
        List<UUID> filiais = normalizarFiliais(filialIds);
        for (UUID filialId : filiais) {
            if (!filialRepository.existsByIdAndTenantId(filialId, tenantId))
                throw new RecursoNaoEncontradoException(
                        "Filial nao encontrada para o tenant informado");
        }

        jdbc.update("""
                DELETE FROM usuario_filiais
                WHERE tenant_id = ? AND usuario_id = ?
                """, tenantId, usuarioId);
        if (!filiais.isEmpty()) {
            List<Object[]> lotes = new ArrayList<>();
            for (UUID filialId : filiais)
                lotes.add(new Object[]{tenantId, usuarioId, filialId, atorId});
            jdbc.batchUpdate("""
                    INSERT INTO usuario_filiais
                        (tenant_id, usuario_id, filial_id, criado_por_id)
                    VALUES (?, ?, ?, ?)
                    """, lotes);
        }

        auditoria.registrar(tenantId, atorId, null, null,
                "SUBSTITUIR_ACESSO_FILIAIS", "USUARIO",
                usuarioId, "totalFiliais=" + filiais.size());
        return new AcessoFiliais(usuarioId, filiais);
    }

    static List<UUID> normalizarFiliais(List<UUID> filialIds) {
        if (filialIds == null)
            throw new RegraNegocioException("Lista de filiais e obrigatoria");
        if (filialIds.size() > LIMITE_FILIAIS)
            throw new RegraNegocioException(
                    "No maximo 200 filiais podem ser vinculadas por usuario");
        if (filialIds.stream().anyMatch(id -> id == null))
            throw new RegraNegocioException("Filial informada e invalida");
        Set<UUID> unicas = new HashSet<>(filialIds);
        if (unicas.size() != filialIds.size())
            throw new RegraNegocioException(
                    "Lista de filiais possui valores duplicados");
        return unicas.stream().sorted().toList();
    }

    private void validarUsuario(UUID tenantId, UUID usuarioId) {
        if (usuarioId == null
                || usuarioRepository.findByIdAndTenantId(usuarioId, tenantId)
                        .isEmpty())
            throw new RecursoNaoEncontradoException(
                    "Usuario nao encontrado para o tenant informado");
    }

    public record AcessoFiliais(UUID usuarioId, List<UUID> filialIds) {}
}
