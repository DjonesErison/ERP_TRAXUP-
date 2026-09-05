package com.traxup.tplug.erp.financeiro;

import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TesourariaBaixaApplicationService {
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
                                       UUID contaFinanceiraId, BigDecimal valor) {
        ContaReceber titulo = contaReceberService.buscar(tenantId, contaReceberId);
        ContaFinanceira contaFinanceira = contaFinanceiraService.buscar(tenantId, contaFinanceiraId);
        validarMesmaFilial(titulo.getFilialId(), contaFinanceira);

        ContaReceber atualizado = contaReceberService.receber(tenantId, usuarioId, contaReceberId, valor);
        contaFinanceiraService.movimentar(
                tenantId,
                usuarioId,
                contaFinanceiraId,
                "ENTRADA",
                valor,
                "RECEBIMENTO_CONTA_RECEBER:" + contaReceberId);
        return atualizado;
    }

    @Transactional
    public ContaPagar pagarEmConta(UUID tenantId, UUID usuarioId, UUID contaPagarId, UUID contaFinanceiraId) {
        ContaPagar titulo = contaPagarService.buscar(tenantId, contaPagarId);
        ContaFinanceira contaFinanceira = contaFinanceiraService.buscar(tenantId, contaFinanceiraId);
        validarMesmaFilial(titulo.getFilialId(), contaFinanceira);

        BigDecimal valor = titulo.getValorOriginal().subtract(titulo.getValorPago());
        contaFinanceiraService.movimentar(
                tenantId,
                usuarioId,
                contaFinanceiraId,
                "SAIDA",
                valor,
                "PAGAMENTO_CONTA_PAGAR:" + contaPagarId);
        return contaPagarService.pagar(tenantId, usuarioId, contaPagarId);
    }

    private void validarMesmaFilial(UUID filialTituloId, ContaFinanceira contaFinanceira) {
        if (!filialTituloId.equals(contaFinanceira.getFilialId())) {
            throw new RegraNegocioException("Conta financeira deve pertencer a mesma filial do titulo");
        }
    }
}
