package com.traxup.tplug.erp.financeiro.integracao;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class IntegracaoFinanceiraAdapterRegistry {
    private final Map<String, IntegracaoFinanceiraAdapter> adapters;

    public IntegracaoFinanceiraAdapterRegistry(List<IntegracaoFinanceiraAdapter> adapters) {
        Map<String, IntegracaoFinanceiraAdapter> porProvedor = new HashMap<>();
        for (IntegracaoFinanceiraAdapter adapter : adapters) {
            String provedor = normalizar(adapter.provedor());
            IntegracaoFinanceiraAdapter anterior = porProvedor.putIfAbsent(provedor, adapter);
            if (anterior != null) {
                throw new IllegalStateException("Mais de um adapter financeiro registrado para o provedor " + provedor);
            }
        }
        this.adapters = Map.copyOf(porProvedor);
    }

    public IntegracaoFinanceiraAdapter obter(String provedor) {
        String normalizado = normalizar(provedor);
        IntegracaoFinanceiraAdapter adapter = adapters.get(normalizado);
        if (adapter == null) {
            throw new RecursoNaoEncontradoException("Adapter financeiro nao configurado para o provedor " + normalizado);
        }
        return adapter;
    }

    private String normalizar(String provedor) {
        if (provedor == null || provedor.isBlank()) {
            throw new IllegalArgumentException("Provedor do adapter e obrigatorio");
        }
        return provedor.trim().toUpperCase(Locale.ROOT);
    }
}
