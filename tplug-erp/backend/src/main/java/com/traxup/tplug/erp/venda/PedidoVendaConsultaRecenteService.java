package com.traxup.tplug.erp.venda;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PedidoVendaConsultaRecenteService {
    private static final int LIMITE_MAXIMO = 100;
    private static final Set<String> STATUS_VALIDOS = Set.of("RASCUNHO", "ABERTO", "FATURADO", "CANCELADO");

    private final PedidoVendaRepository repository;

    public PedidoVendaConsultaRecenteService(PedidoVendaRepository repository) {
        this.repository = repository;
    }

    public List<PedidoVenda> listar(UUID tenantId, int limite, UUID filialId, UUID clienteId, String status,
                                   Instant inicio, Instant fim) {
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new IllegalArgumentException("Limite de vendas recentes deve estar entre 1 e 100");
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
                PageRequest.of(0, limite));
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
