package com.traxup.tplug.erp.venda.api;

import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.venda.PedidoVendaApplicationService;
import com.traxup.tplug.erp.venda.PedidoVendaConsultaRecenteService;
import com.traxup.tplug.erp.venda.PedidoVendaDetalheConsultaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendas/pedidos")
public class PedidoVendaController {
    private final PedidoVendaApplicationService service;
    private final PedidoVendaConsultaRecenteService consultaRecenteService;
    private final PedidoVendaDetalheConsultaService detalheConsultaService;
    private final TenantContext tenantContext;

    public PedidoVendaController(PedidoVendaApplicationService service,
                                 PedidoVendaConsultaRecenteService consultaRecenteService,
                                 PedidoVendaDetalheConsultaService detalheConsultaService,
                                 TenantContext tenantContext) {
        this.service = service;
        this.consultaRecenteService = consultaRecenteService;
        this.detalheConsultaService = detalheConsultaService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public List<PedidoVendaResponse> listar() {
        return service.listar(tenantContext.tenantId()).stream().map(PedidoVendaResponse::from).toList();
    }

    @GetMapping("/recentes")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public PedidoVendaPaginaResponse listarRecentes(@RequestParam(defaultValue = "0") int pagina,
                                                     @RequestParam(defaultValue = "20") int tamanho,
                                                     @RequestParam(required = false) UUID filialId,
                                                     @RequestParam(required = false) UUID clienteId,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return PedidoVendaPaginaResponse.from(
                consultaRecenteService.listar(tenantContext.tenantId(), pagina, tamanho, filialId, clienteId, status, inicio, fim));
    }

    @GetMapping("/{pedidoId}")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public PedidoVendaResponse buscar(@PathVariable UUID pedidoId) {
        return PedidoVendaResponse.from(service.buscar(tenantContext.tenantId(), pedidoId));
    }

    @GetMapping("/{pedidoId}/detalhe")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public PedidoVendaDetalheResponse detalhe(@PathVariable UUID pedidoId) {
        var detalhe = detalheConsultaService.consultar(tenantContext.tenantId(), pedidoId);
        return PedidoVendaDetalheResponse.from(detalhe.pedido(), detalhe.itens());
    }

    @GetMapping("/{pedidoId}/totais")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_LER')")
    public PedidoVendaTotaisResponse totais(@PathVariable UUID pedidoId) {
        var detalhe = detalheConsultaService.consultar(tenantContext.tenantId(), pedidoId);
        return PedidoVendaTotaisResponse.from(detalhe.itens());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_CRIAR')")
    public PedidoVendaResponse criar(@Valid @RequestBody CriarPedidoVendaRequest request) {
        return PedidoVendaResponse.from(service.criar(tenantContext.tenantId(), request.filialId(), request.clienteId(),
                request.numero(), request.observacao(), tenantContext.usuarioIdOuNulo()));
    }

    @PostMapping("/{pedidoId}/pagamento")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public PedidoVendaResponse configurarPagamento(@PathVariable UUID pedidoId,
                                                    @Valid @RequestBody ConfigurarPagamentoPedidoVendaRequest request) {
        return PedidoVendaResponse.from(service.configurarPagamento(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(),
                pedidoId, request.formaPagamentoId(), request.condicaoPagamentoId()));
    }

    @PostMapping("/{pedidoId}/abrir")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public PedidoVendaResponse abrir(@PathVariable UUID pedidoId) {
        return PedidoVendaResponse.from(service.abrir(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId));
    }

    @PostMapping("/{pedidoId}/faturar")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public PedidoVendaResponse faturar(@PathVariable UUID pedidoId) {
        return PedidoVendaResponse.from(service.faturar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId));
    }

    @PostMapping("/{pedidoId}/cancelar")
    @PreAuthorize("hasAuthority('VENDA_PEDIDO_EDITAR')")
    public PedidoVendaResponse cancelar(@PathVariable UUID pedidoId) {
        return PedidoVendaResponse.from(service.cancelar(tenantContext.tenantId(), tenantContext.usuarioIdOuNulo(), pedidoId));
    }
}
