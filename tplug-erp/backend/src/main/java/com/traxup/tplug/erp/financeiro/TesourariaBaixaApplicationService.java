package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RecursoConflitanteException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TesourariaBaixaApplicationService {
    private static final String ORIGEM_RECEBER = "RECEBIMENTO_CONTA_RECEBER";
    private static final String ORIGEM_PAGAR = "PAGAMENTO_CONTA_PAGAR";

    private final ContaReceberApplicationService contaReceberService;
    private final ContaPagarApplicationService contaPagarService;
    private final ContaFinanceiraApplicationService contaFinanceiraService;

    public TesourariaBaixaApplicationService(ContaReceberApplicationService contaReceberService,
                                             ContaPagarApplicationService contaPagarService,
                                             ContaFinanceiraApplicationService contaFinanceiraService) {
        this.contaReceberService = contaReceberService;
        this.contaPagarService = contaPagarService;
        this.contaFinanceiraService = contaFinanceiraService;
    }

    @Transactional
    public ContaReceber receberEmConta(UUID tenantId, UUID usuarioId, UUID contaReceberId,
                                       UUID contaFinanceiraId, BigDecimal valor, String idempotencyKey) {
        validarNaoProcessada(tenantId, ORIGEM_RECEBER, contaReceberId, idempotencyKey);
        ContaReceber titulo = contaReceberService.buscar(tenantId, contaReceberId);
        ContaFinanceira contaFinanceira = contaFinanceiraService.buscar(tenantId, contaFinanceiraId);
        validarMesmaFilial(titulo.getFilialId(), contaFinanceira);

        ContaReceber atualizado = contaReceberService.receber(tenantId, usuarioId, contaReceberId, valor);
        contaFinanceiraService.movimentarComOrigem(
                tenantId, usuarioId, contaFinanceiraId, "ENTRADA", valor,
                "RECEBIMENTO_CONTA_RECEBER:" + contaReceberId,
                ORIGEM_RECEBER, contaReceberId, idempotencyKey);
        return atualizado;
    }

    @Transactional
    public ContaPagar pagarEmConta(UUID tenantId, UUID usuarioId, UUID contaPagarId,
                                   UUID contaFinanceiraId, String idempotencyKey) {
        validarNaoProcessada(tenantId, ORIGEM_PAGAR, contaPagarId, idempotencyKey);
        ContaPagar titulo = contaPagarService.buscar(tenantId, contaPagarId);
        return pagarEmContaValidado(tenantId, usuarioId, titulo, contaPagarId, contaFinanceiraId,
                titulo.getSaldoAberto(), idempotencyKey);
    }

    @Transactional
    public ContaPagar pagarEmConta(UUID tenantId, UUID usuarioId, UUID contaPagarId,
                                   UUID contaFinanceiraId, BigDecimal valor, String idempotencyKey) {
        validarNaoProcessada(tenantId, ORIGEM_PAGAR, contaPagarId, idempotencyKey);
        ContaPagar titulo = contaPagarService.buscar(tenantId, contaPagarId);
        return pagarEmContaValidado(tenantId, usuarioId, titulo, contaPagarId, contaFinanceiraId, valor, idempotencyKey);
    }

    private ContaPagar pagarEmContaValidado(UUID tenantId, UUID usuarioId, ContaPagar titulo, UUID contaPagarId,
                                            UUID contaFinanceiraId, BigDecimal valor, String idempotencyKey) {
        ContaFinanceira contaFinanceira = contaFinanceiraService.buscar(tenantId, contaFinanceiraId);
        validarMesmaFilial(titulo.getFilialId(), contaFinanceira);
        ContaPagar atualizado = contaPagarService.pagar(tenantId, usuarioId, contaPagarId, valor);
        contaFinanceiraService.movimentarComOrigem(
                tenantId, usuarioId, contaFinanceiraId, "SAIDA", valor,
                "PAGAMENTO_CONTA_PAGAR:" + contaPagarId,
                ORIGEM_PAGAR, contaPagarId, idempotencyKey);
        return atualizado;
    }

    private void validarNaoProcessada(UUID tenantId, String origemTipo, UUID origemId, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new RegraNegocioException("Idempotency-Key e obrigatoria para baixa integrada com tesouraria");
        }
        if (idempotencyKey.trim().length() > 120) {
            throw new RegraNegocioException("Idempotency-Key deve possuir no maximo 120 caracteres");
        }
        if (contaFinanceiraService.existeMovimentoPorOrigem(tenantId, origemTipo, origemId, idempotencyKey)) {
            throw new RecursoConflitanteException("Operacao de tesouraria ja processada para a Idempotency-Key informada");
        }
    }

    private void validarMesmaFilial(UUID filialTituloId, ContaFinanceira contaFinanceira) {
        if (!filialTituloId.equals(contaFinanceira.getFilialId())) {
            throw new RegraNegocioException("Conta financeira deve pertencer a mesma filial do titulo");
        }
    }
}
