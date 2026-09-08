package com.traxup.tplug.erp.inventario;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueSaldoRepository;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.produto.ProdutoRepository;
import com.traxup.tplug.erp.produto.grade.GradeProdutoRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class InventarioApplicationService {
    private static final Set<String> STATUS = Set.of("ABERTO", "CONCLUIDO", "CANCELADO");
    private static final Set<String> TIPOS_ITEM = Set.of("PRODUTO", "GRADE");
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 500;

    private final InventarioSessaoRepository sessaoRepository;
    private final InventarioContagemRepository contagemRepository;
    private final EstoqueSaldoRepository estoqueSaldoRepository;
    private final FilialRepository filialRepository;
    private final ProdutoRepository produtoRepository;
    private final GradeProdutoRepository gradeProdutoRepository;
    private final AuditoriaApplicationService auditoria;

    public InventarioApplicationService(InventarioSessaoRepository sessaoRepository,
                                        InventarioContagemRepository contagemRepository,
                                        EstoqueSaldoRepository estoqueSaldoRepository,
                                        FilialRepository filialRepository,
                                        ProdutoRepository produtoRepository,
                                        GradeProdutoRepository gradeProdutoRepository,
                                        AuditoriaApplicationService auditoria) {
        this.sessaoRepository = sessaoRepository;
        this.contagemRepository = contagemRepository;
        this.estoqueSaldoRepository = estoqueSaldoRepository;
        this.filialRepository = filialRepository;
        this.produtoRepository = produtoRepository;
        this.gradeProdutoRepository = gradeProdutoRepository;
        this.auditoria = auditoria;
    }

    public List<InventarioSessao> listar(UUID tenantId, UUID filialId, String status, Integer limite) {
        int tamanho = validarLimite(limite);
        validarFilial(tenantId, filialId, false);
        String statusNormalizado = normalizarStatusOpcional(status);
        return sessaoRepository.buscar(tenantId, filialId, statusNormalizado, PageRequest.of(0, tamanho));
    }

    public InventarioSessao buscar(UUID tenantId, UUID inventarioId) {
        return sessaoRepository.findByIdAndTenantId(inventarioId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Inventario nao encontrado para o tenant informado"));
    }

    public List<InventarioContagem> listarContagens(UUID tenantId, UUID inventarioId, Integer limite) {
        int tamanho = validarLimite(limite);
        buscar(tenantId, inventarioId);
        return contagemRepository.findAllByTenantIdAndInventarioIdOrderByTipoItemAscItemIdAsc(
                tenantId, inventarioId, PageRequest.of(0, tamanho));
    }

    @Transactional
    public InventarioSessao criar(UUID tenantId, UUID usuarioId, UUID filialId, String descricao) {
        validarFilial(tenantId, filialId, true);
        String descricaoNormalizada = normalizarDescricao(descricao);
        InventarioSessao sessao = sessaoRepository.save(new InventarioSessao(tenantId, filialId, descricaoNormalizada, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, filialId, "CRIAR", "INVENTARIO_SESSAO", sessao.getId(), null);
        return sessao;
    }

    @Transactional
    public InventarioContagem registrarContagem(UUID tenantId, UUID usuarioId, UUID inventarioId,
                                                 String tipoItem, UUID itemId, BigDecimal quantidadeContada) {
        InventarioSessao sessao = buscarParaAtualizar(tenantId, inventarioId);
        if (!"ABERTO".equals(sessao.getStatus())) {
            throw new RegraNegocioException("Somente inventario ABERTO permite contagem");
        }
        String tipoNormalizado = normalizarTipoItem(tipoItem);
        validarItem(tenantId, tipoNormalizado, itemId);
        if (quantidadeContada == null || quantidadeContada.signum() < 0) {
            throw new RegraNegocioException("Quantidade contada deve ser maior ou igual a zero");
        }

        BigDecimal quantidadeSistema = estoqueSaldoRepository
                .findByTenantIdAndFilialIdAndTipoItemAndItemId(tenantId, sessao.getFilialId(), tipoNormalizado, itemId)
                .map(saldo -> saldo.getQuantidade())
                .orElse(BigDecimal.ZERO);

        InventarioContagem contagem = contagemRepository
                .findByTenantIdAndInventarioIdAndTipoItemAndItemId(tenantId, inventarioId, tipoNormalizado, itemId)
                .orElseGet(() -> new InventarioContagem(tenantId, inventarioId, tipoNormalizado, itemId,
                        quantidadeSistema, quantidadeContada, usuarioId));
        contagem.atualizar(quantidadeSistema, quantidadeContada, usuarioId);
        contagem = contagemRepository.save(contagem);

        auditoria.registrar(tenantId, usuarioId, null, sessao.getFilialId(), "CONTAR", "INVENTARIO_CONTAGEM",
                contagem.getId(), "inventarioId=" + inventarioId + ";tipoItem=" + tipoNormalizado + ";itemId=" + itemId);
        return contagem;
    }

    @Transactional
    public InventarioSessao concluir(UUID tenantId, UUID usuarioId, UUID inventarioId) {
        InventarioSessao sessao = buscarParaAtualizar(tenantId, inventarioId);
        if (!contagemRepository.existsByTenantIdAndInventarioId(tenantId, inventarioId)) {
            throw new RegraNegocioException("Inventario precisa possuir ao menos uma contagem para ser concluido");
        }
        sessao.concluir(usuarioId);
        sessaoRepository.save(sessao);
        auditoria.registrar(tenantId, usuarioId, null, sessao.getFilialId(), "CONCLUIR", "INVENTARIO_SESSAO",
                sessao.getId(), null);
        return sessao;
    }

    @Transactional
    public InventarioSessao cancelar(UUID tenantId, UUID usuarioId, UUID inventarioId) {
        InventarioSessao sessao = buscarParaAtualizar(tenantId, inventarioId);
        sessao.cancelar(usuarioId);
        sessaoRepository.save(sessao);
        auditoria.registrar(tenantId, usuarioId, null, sessao.getFilialId(), "CANCELAR", "INVENTARIO_SESSAO",
                sessao.getId(), null);
        return sessao;
    }

    private InventarioSessao buscarParaAtualizar(UUID tenantId, UUID inventarioId) {
        return sessaoRepository.buscarParaAtualizar(inventarioId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Inventario nao encontrado para o tenant informado"));
    }

    private void validarFilial(UUID tenantId, UUID filialId, boolean obrigatoria) {
        if (filialId == null) {
            if (obrigatoria) throw new RegraNegocioException("Filial do inventario e obrigatoria");
            return;
        }
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
    }

    private void validarItem(UUID tenantId, String tipoItem, UUID itemId) {
        if (itemId == null) throw new RegraNegocioException("Item da contagem e obrigatorio");
        boolean existe = switch (tipoItem) {
            case "PRODUTO" -> produtoRepository.findByIdAndTenantId(itemId, tenantId).isPresent();
            case "GRADE" -> gradeProdutoRepository.findByIdAndTenantId(itemId, tenantId).isPresent();
            default -> false;
        };
        if (!existe) throw new RecursoNaoEncontradoException("Item de estoque nao encontrado para o tenant informado");
    }

    private int validarLimite(Integer limite) {
        int tamanho = limite == null ? LIMITE_PADRAO : limite;
        if (tamanho < 1 || tamanho > LIMITE_MAXIMO) throw new RegraNegocioException("Limite deve estar entre 1 e 500");
        return tamanho;
    }

    private String normalizarStatusOpcional(String status) {
        if (status == null || status.isBlank()) return null;
        String normalizado = status.trim().toUpperCase();
        if (!STATUS.contains(normalizado)) throw new RegraNegocioException("Status de inventario invalido");
        return normalizado;
    }

    private String normalizarTipoItem(String tipoItem) {
        if (tipoItem == null || tipoItem.isBlank()) throw new RegraNegocioException("Tipo do item e obrigatorio");
        String normalizado = tipoItem.trim().toUpperCase();
        if (!TIPOS_ITEM.contains(normalizado)) throw new RegraNegocioException("Tipo do item deve ser PRODUTO ou GRADE");
        return normalizado;
    }

    private String normalizarDescricao(String descricao) {
        if (descricao == null || descricao.isBlank()) return null;
        String normalizada = descricao.trim();
        if (normalizada.length() > 160) throw new RegraNegocioException("Descricao do inventario deve possuir no maximo 160 caracteres");
        return normalizada;
    }
}
