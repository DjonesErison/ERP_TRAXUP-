package com.traxup.tplug.erp.venda;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoVendaConsultaRecenteService {
    private static final int TAMANHO_MAXIMO = 100;
    private static final Set<String> STATUS_VALIDOS = Set.of("RASCUNHO", "ABERTO", "FATURADO", "CANCELADO");

    private final PedidoVendaRepository repository;

    public PedidoVendaConsultaRecenteService(PedidoVendaRepository repository) {
        this.repository = repository;
    }

    public Page<PedidoVenda> listar(UUID tenantId, int pagina, int tamanho, UUID filialId, UUID clienteId, String status,
                                   Instant inicio, Instant fim) {
        if (pagina < 0) {
            throw new IllegalArgumentException("Pagina de vendas recentes deve ser maior ou igual a zero");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("Tamanho da pagina de vendas recentes deve estar entre 1 e 100");
        }
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Inicio do periodo nao pode ser posterior ao fim");
        }

        String statusNormalizado = normalizarStatus(status);
        return repository.buscarRecentesFiltrados(
                tenantId,
                filialId,
                clienteId,
                statusNormalizado,
                inicio,
                fim,
                PageRequest.of(pagina, tamanho));
    }

    private String normalizarStatus(String status) {
        if (status == null || status.isBlank()) return null;

        String normalizado = status.trim().toUpperCase(Locale.ROOT);
        if (!STATUS_VALIDOS.contains(normalizado)) {
            throw new IllegalArgumentException("Status de pedido de venda invalido");
        }
        return normalizado;
    }
}
