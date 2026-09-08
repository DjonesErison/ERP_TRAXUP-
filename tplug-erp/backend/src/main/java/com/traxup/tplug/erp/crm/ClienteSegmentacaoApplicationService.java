package com.traxup.tplug.erp.crm;

import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.pessoa.PessoaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.shared.exception.RegraNegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ClienteSegmentacaoApplicationService {
    private static final int DIAS_PADRAO = 365;
    private static final int DIAS_MAXIMO = 3650;
    private static final int LIMITE_PADRAO = 100;
    private static final int LIMITE_MAXIMO = 500;

    private final PessoaRepository pessoaRepository;
    private final FilialRepository filialRepository;

    public ClienteSegmentacaoApplicationService(PessoaRepository pessoaRepository, FilialRepository filialRepository) {
        this.pessoaRepository = pessoaRepository;
        this.filialRepository = filialRepository;
    }

    public List<ClienteRfmProjection> listarRfm(UUID tenantId, UUID filialId, Integer diasHistorico, Integer limite) {
        int dias = diasHistorico == null ? DIAS_PADRAO : diasHistorico;
        int tamanho = limite == null ? LIMITE_PADRAO : limite;

        if (dias < 1 || dias > DIAS_MAXIMO) {
            throw new RegraNegocioException("Dias de historico deve estar entre 1 e 3650");
        }
        if (tamanho < 1 || tamanho > LIMITE_MAXIMO) {
            throw new RegraNegocioException("Limite deve estar entre 1 e 500");
        }
        if (filialId != null && !filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }

        Instant desde = Instant.now().minus(dias, ChronoUnit.DAYS);
        return pessoaRepository.buscarMetricasRfm(tenantId, filialId, desde, PageRequest.of(0, tamanho));
    }
}
