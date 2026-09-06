package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.venda.PedidoVendaPreviaFinanceiraService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendas/pedidos")
public class PedidoVendaPreviaFinanceiraController {
    private final PedidoVendaPreviaFinanceiraService service;
    private final TenantContext tenantContext;

    public PedidoVendaPreviaFinanceiraController(PedidoVendaPreviaFinanceiraService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/{pedidoId}/previa-financeira")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public PreviaFinanceiraResponse prever(@PathVariable UUID pedidoId) {
        return PreviaFinanceiraResponse.from(service.prever(tenantContext.tenantId(), pedidoId));
    }

    public record PreviaFinanceiraResponse(BigDecimal totalLiquido, BigDecimal desconto, BigDecimal juros,
                                           BigDecimal entrada, BigDecimal totalFinanceiro, BigDecimal saldoParcelar,
                                           List<TituloPrevistoResponse> titulos) {
        static PreviaFinanceiraResponse from(PedidoVendaPreviaFinanceiraService.PreviaFinanceira previa) {
            return new PreviaFinanceiraResponse(previa.totalLiquido(), previa.desconto(), previa.juros(), previa.entrada(),
                    previa.totalFinanceiro(), previa.saldoParcelar(),
                    previa.titulos().stream().map(TituloPrevistoResponse::from).toList());
        }
    }

    public record TituloPrevistoResponse(String tipo, int numero, LocalDate vencimento, BigDecimal valor) {
        static TituloPrevistoResponse from(PedidoVendaPreviaFinanceiraService.ParcelaPrevista parcela) {
            return new TituloPrevistoResponse(parcela.tipo(), parcela.numero(), parcela.vencimento(), parcela.valor());
        }
    }
}
