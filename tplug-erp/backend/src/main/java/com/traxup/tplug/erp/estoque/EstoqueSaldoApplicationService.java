package com.traxup.tplug.erp.estoque;

import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EstoqueSaldoApplicationService {

    private final EstoqueSaldoRepository estoqueSaldoRepository;
    private final FilialRepository filialRepository;

    public EstoqueSaldoApplicationService(
            EstoqueSaldoRepository estoqueSaldoRepository,
            FilialRepository filialRepository) {
        this.estoqueSaldoRepository = estoqueSaldoRepository;
        this.filialRepository = filialRepository;
    }

    public List<EstoqueSaldo> listarPorFilial(UUID tenantId, UUID filialId) {
        validarFilial(tenantId, filialId);
        return estoqueSaldoRepository.findAllByTenantIdAndFilialIdOrderByTipoItemAscItemIdAsc(tenantId, filialId);
    }

    private void validarFilial(UUID tenantId, UUID filialId) {
        if (!filialRepository.existsByIdAndTenantId(filialId, tenantId)) {
            throw new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado");
        }
    }
}
