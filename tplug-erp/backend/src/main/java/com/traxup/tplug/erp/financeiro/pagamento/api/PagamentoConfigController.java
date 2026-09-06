package com.traxup.tplug.erp.financeiro.pagamento.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.CondicaoPagamentoParcela;
import com.traxup.tplug.erp.financeiro.pagamento.FormaPagamento;
import com.traxup.tplug.erp.financeiro.pagamento.PagamentoConfigApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/financeiro/pagamentos-config")
public class PagamentoConfigController {
    private final PagamentoConfigApplicationService service;
    private final TenantContext tenantContext;

    public PagamentoConfigController(PagamentoConfigApplicationService service, TenantContext tenantContext) {
        this.service = service;
        this.tenantContext = tenantContext;
    }

    @GetMapping("/formas")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAMENTO_CONFIG_LER')")
    public List<FormaResponse> listarFormas() {
        return service.listarFormas(tenantContext.tenantId()).stream().map(FormaResponse::from).toList();
    }

    @PostMapping("/formas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAMENTO_CONFIG_EDITAR')")
    public FormaResponse criarForma(@RequestBody CriarFormaRequest request) {
        return FormaResponse.from(service.criarForma(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), request.codigo(), request.nome()));
    }

    @PostMapping("/formas/{id}/desativar")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAMENTO_CONFIG_EDITAR')")
    public void desativarForma(@PathVariable UUID id) {
        service.desativarForma(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), id);
    }

    @GetMapping("/condicoes")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAMENTO_CONFIG_LER')")
    public List<CondicaoResponse> listarCondicoes() {
        UUID tenantId = tenantContext.tenantId();
        return service.listarCondicoes(tenantId).stream().map(c -> CondicaoResponse.from(c, service.listarParcelas(tenantId, c.getId()))).toList();
    }

    @PostMapping("/condicoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAMENTO_CONFIG_EDITAR')")
    public CondicaoResponse criarCondicao(@RequestBody CriarCondicaoRequest request) {
        UUID tenantId = tenantContext.tenantId();
        List<PagamentoConfigApplicationService.ParcelaDefinicao> parcelas = request.parcelas().stream()
                .map(p -> new PagamentoConfigApplicationService.ParcelaDefinicao(p.numero(), p.dias(), p.percentual())).toList();
        CondicaoPagamento condicao = service.criarCondicao(tenantId, tenantContext.usuarioIdOuNulo(), request.codigo(), request.nome(), parcelas);
        return CondicaoResponse.from(condicao, service.listarParcelas(tenantId, condicao.getId()));
    }

    @PostMapping("/condicoes/{id}/desativar")
    @PreAuthorize("hasAuthority('FINANCEIRO_PAGAMENTO_CONFIG_EDITAR')")
    public void desativarCondicao(@PathVariable UUID id) {
        service.desativarCondicao(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), id);
    }

    public record CriarFormaRequest(String codigo, String nome) {}
    public record CriarCondicaoRequest(String codigo, String nome, List<ParcelaRequest> parcelas) {}
    public record ParcelaRequest(int numero, int dias, BigDecimal percentual) {}

    public record FormaResponse(UUID id, String codigo, String nome, boolean ativo) {
        static FormaResponse from(FormaPagamento forma) { return new FormaResponse(forma.getId(), forma.getCodigo(), forma.getNome(), forma.isAtivo()); }
    }

    public record ParcelaResponse(int numero, int dias, BigDecimal percentual) {
        static ParcelaResponse from(CondicaoPagamentoParcela parcela) { return new ParcelaResponse(parcela.getNumero(), parcela.getDias(), parcela.getPercentual()); }
    }

    public record CondicaoResponse(UUID id, String codigo, String nome, boolean ativo, List<ParcelaResponse> parcelas) {
        static CondicaoResponse from(CondicaoPagamento condicao, List<CondicaoPagamentoParcela> parcelas) {
            return new CondicaoResponse(condicao.getId(), condicao.getCodigo(), condicao.getNome(), condicao.isAtivo(), parcelas.stream().map(ParcelaResponse::from).toList());
        }
    }
}
