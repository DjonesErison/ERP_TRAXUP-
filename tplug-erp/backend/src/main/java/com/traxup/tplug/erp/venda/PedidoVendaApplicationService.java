package com.traxup.tplug.erp.venda;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.estoque.EstoqueMovimentacaoApplicationService;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.financeiro.ContaReceberApplicationService;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcela;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcelaRepository;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoRepository;
import com.traxup.tplug.erp.financeiro.pagamento.FormaPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.FormaPagamentoRepository;
import com.traxup.tplug.erp.pessoa.Pessoa;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoVendaApplicationService {
    private static final String ORIGEM_PEDIDO_VENDA = "PEDIDO_VENDA";
    private static final int LIMITE_RECENTES_MAXIMO = 100;

    private final PedidoVendaRepository repository;
    private final PedidoVendaItemRepository itemRepository;
    private final FilialRepository filialRepository;
    private final PessoaRepository pessoaRepository;
    private final EstoqueMovimentacaoApplicationService estoqueMovimentacaoService;
    private final ContaReceberApplicationService contaReceberService;
    private final FormaPagamentoRepository formaPagamentoRepository;
    private final CondicaoPagamentoRepository condicaoPagamentoRepository;
    private final CondicaoPagamentoParcelaRepository parcelaRepository;
    private final AuditoriaApplicationService auditoria;

    public PedidoVendaApplicationService(PedidoVendaRepository repository, PedidoVendaItemRepository itemRepository,
                                         FilialRepository filialRepository, PessoaRepository pessoaRepository,
                                         EstoqueMovimentacaoApplicationService estoqueMovimentacaoService,
                                         ContaReceberApplicationService contaReceberService,
                                         FormaPagamentoRepository formaPagamentoRepository,
                                         CondicaoPagamentoRepository condicaoPagamentoRepository,
                                         CondicaoPagamentoParcelaRepository parcelaRepository,
                                         AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.itemRepository = itemRepository;
        this.filialRepository = filialRepository;
        this.pessoaRepository = pessoaRepository;
        this.estoqueMovimentacaoService = estoqueMovimentacaoService;
        this.contaReceberService = contaReceberService;
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.condicaoPagamentoRepository = condicaoPagamentoRepository;
        this.parcelaRepository = parcelaRepository;
        this.auditoria = auditoria;
    }

    public List<PedidoVenda> listar(UUID tenantId) {
        return repository.findAllByTenantIdOrderByCriadoEmDescIdAsc(tenantId);
    }

    public List<PedidoVenda> listarRecentes(UUID tenantId, int limite) {
        if (limite < 1 || limite > LIMITE_RECENTES_MAXIMO) {
            throw new IllegalArgumentException("Limite de vendas recentes deve estar entre 1 e 100");
        }
        return repository.findAllByTenantIdOrderByCriadoEmDescIdAsc(tenantId, PageRequest.of(0, limite));
    }

    public PedidoVenda buscar(UUID tenantId, UUID pedidoId) {
        return repository.findByIdAndTenantId(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
    }

    public List<PedidoVendaItem> listarItens(UUID tenantId, UUID pedidoId) {
        buscar(tenantId, pedidoId);
        return itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId);
    }

    @Transactional
    public PedidoVenda criar(UUID tenantId, UUID filialId, UUID clienteId, String numero, String observacao, UUID usuarioId) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
        if (clienteId != null) {
            Pessoa cliente = pessoaRepository.findByIdAndTenantId(clienteId, tenantId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado para o tenant informado"));
            if (!cliente.isCliente() || !cliente.isAtivo()) throw new IllegalArgumentException("Pessoa informada nao e um cliente ativo");
        }
        String numeroNormalizado = numero.trim();
        if (repository.existsByTenantIdAndNumeroIgnoreCase(tenantId, numeroNormalizado)) {
            throw new IllegalArgumentException("Numero de pedido de venda ja cadastrado para o tenant");
        }
        String observacaoNormalizada = observacao == null || observacao.isBlank() ? null : observacao.trim();
        PedidoVenda pedido = repository.save(new PedidoVenda(tenantId, filialId, clienteId, numeroNormalizado, observacaoNormalizada, usuarioId));
        auditoria.registrar(tenantId, usuarioId, null, filialId, "CRIAR", "PEDIDO_VENDA", pedido.getId(), null);
        return pedido;
    }

    @Transactional
    public PedidoVenda configurarPagamento(UUID tenantId, UUID usuarioId, UUID pedidoId, UUID formaPagamentoId, UUID condicaoPagamentoId) {
        PedidoVenda pedido = buscar(tenantId, pedidoId);
        FormaPagamento forma = formaPagamentoRepository.findByIdAndTenantId(formaPagamentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Forma de pagamento nao encontrada para o tenant informado"));
        CondicaoPagamento condicao = condicaoPagamentoRepository.findByIdAndTenantId(condicaoPagamentoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Condicao de pagamento nao encontrada para o tenant informado"));
        if (!forma.isAtivo() || !condicao.isAtivo()) throw new IllegalArgumentException("Forma e condicao de pagamento devem estar ativas");
        if (parcelaRepository.findAllByTenantIdAndCondicaoPagamentoIdOrderByNumeroAsc(tenantId, condicaoPagamentoId).isEmpty()) {
            throw new IllegalArgumentException("Condicao de pagamento precisa possuir parcelas");
        }
        pedido.configurarPagamento(formaPagamentoId, condicaoPagamentoId);
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "CONFIGURAR_PAGAMENTO", "PEDIDO_VENDA", pedido.getId(),
                "formaPagamentoId=" + formaPagamentoId + ";condicaoPagamentoId=" + condicaoPagamentoId);
        return pedido;
    }

    @Transactional
    public PedidoVenda abrir(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoVenda pedido = buscar(tenantId, pedidoId);
        if (itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId).isEmpty()) {
            throw new IllegalArgumentException("Pedido de venda precisa possuir itens antes de ser aberto");
        }
        pedido.abrir();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "ABRIR", "PEDIDO_VENDA", pedido.getId(), null);
        return pedido;
    }

    @Transactional
    public PedidoVenda faturar(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoVenda pedido = repository.buscarParaFaturar(pedidoId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de venda nao encontrado para o tenant informado"));
        if (!"ABERTO".equals(pedido.getStatus())) {
            throw new IllegalArgumentException("Somente pedido de venda ABERTO pode ser faturado");
        }
        List<PedidoVendaItem> itens = itemRepository.findAllByTenantIdAndPedidoVendaIdOrderByCriadoEmAscIdAsc(tenantId, pedidoId);
        if (itens.isEmpty()) throw new IllegalArgumentException("Pedido de venda precisa possuir itens antes de ser faturado");

        for (PedidoVendaItem item : itens) {
            String tipoItem = item.getGradeId() == null ? "PRODUTO" : "GRADE";
            UUID itemEstoqueId = item.getGradeId() == null ? item.getProdutoId() : item.getGradeId();
            estoqueMovimentacaoService.movimentar(tenantId, pedido.getFilialId(), tipoItem, itemEstoqueId,
                    "SAIDA", item.getQuantidade(), "FATURAMENTO_PEDIDO_VENDA:" + pedido.getId(), usuarioId);
        }

        if (pedido.getClienteId() != null) {
            BigDecimal totalLiquido = itens.stream().map(PedidoVendaItem::getTotalItem).reduce(BigDecimal.ZERO, BigDecimal::add);
            CondicaoPagamento condicao = null;
            List<CondicaoPagamentoParcela> parcelas = List.of();
            if (pedido.getCondicaoPagamentoId() != null) {
                condicao = condicaoPagamentoRepository.findByIdAndTenantId(pedido.getCondicaoPagamentoId(), tenantId)
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Condicao de pagamento nao encontrada para o tenant informado"));
                parcelas = parcelaRepository.findAllByTenantIdAndCondicaoPagamentoIdOrderByNumeroAsc(
                        tenantId, pedido.getCondicaoPagamentoId());
            }

            PedidoVendaPlanoFinanceiroCalculadora.Plano plano = PedidoVendaPlanoFinanceiroCalculadora
                    .calcular(totalLiquido, condicao, parcelas, LocalDate.now());
            for (PedidoVendaPlanoFinanceiroCalculadora.Titulo titulo : plano.titulos()) {
                if ("ENTRADA".equals(titulo.tipo())) {
                    contaReceberService.criarComOrigem(tenantId, usuarioId, pedido.getFilialId(), pedido.getClienteId(),
                            "PV-" + pedido.getNumero() + "-ENTRADA",
                            "Entrada do faturamento pedido de venda " + pedido.getNumero(),
                            titulo.valor(), titulo.vencimento(), ORIGEM_PEDIDO_VENDA, pedido.getId(), "ENTRADA");
                } else {
                    criarParcelaFinanceira(tenantId, usuarioId, pedido, titulo.valor(), titulo.vencimento(), titulo.numero());
                }
            }
        }

        pedido.faturar();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(),
                "FATURAR", "PEDIDO_VENDA", pedido.getId(), "itens=" + itens.size());
        return pedido;
    }

    private void criarParcelaFinanceira(UUID tenantId, UUID usuarioId, PedidoVenda pedido, BigDecimal valor,
                                        LocalDate vencimento, int numeroParcela) {
        contaReceberService.criarComOrigem(tenantId, usuarioId, pedido.getFilialId(), pedido.getClienteId(),
                "PV-" + pedido.getNumero() + "-" + numeroParcela,
                "Faturamento pedido de venda " + pedido.getNumero() + " parcela " + numeroParcela,
                valor, vencimento, ORIGEM_PEDIDO_VENDA, pedido.getId(), "PARCELA:" + numeroParcela);
    }

    @Transactional
    public PedidoVenda cancelar(UUID tenantId, UUID usuarioId, UUID pedidoId) {
        PedidoVenda pedido = buscar(tenantId, pedidoId);
        pedido.cancelar();
        repository.save(pedido);
        auditoria.registrar(tenantId, usuarioId, null, pedido.getFilialId(), "CANCELAR", "PEDIDO_VENDA", pedido.getId(), null);
        return pedido;
    }
}
