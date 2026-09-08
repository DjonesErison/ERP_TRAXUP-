package com.traxup.tplug.erp.crm;

import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ClienteRfvApplicationService {
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 500;

    private final PessoaRepository pessoaRepository;
    private final FilialRepository filialRepository;

    public ClienteRfvApplicationService(PessoaRepository pessoaRepository, FilialRepository filialRepository) {
        this.pessoaRepository = pessoaRepository;
        this.filialRepository = filialRepository;
    }

    public List<ClienteRfvProjection> listar(UUID tenantId, UUID filialId, Instant inicio, Instant fim, Integer limite) {
        int tamanho = limite == null ? LIMITE_PADRAO : limite;
        if (tamanho < 1 || tamanho > LIMITE_MAXIMO) {
            throw new RegraNegocioException("Limite deve estar entre 1 e 500");
        }
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraNegocioException("Periodo RFV invalido: inicio deve ser anterior ou igual ao fim");
        }
        if (filialId != null && !filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
        return pessoaRepository.buscarMetricasRfv(tenantId, filialId, inicio, fim, PageRequest.of(0, tamanho));
    }
}
