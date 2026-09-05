package com.traxup.tplug.erp.filial;

import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.empresa.EmpresaRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FilialApplicationService {

    private final FilialRepository filialRepository;
    private final EmpresaRepository empresaRepository;
    private final TenantRepository tenantRepository;

    public FilialApplicationService(
            FilialRepository filialRepository,
            EmpresaRepository empresaRepository,
            TenantRepository tenantRepository) {
        this.filialRepository = filialRepository;
        this.empresaRepository = empresaRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<Filial> listar(UUID tenantId) {
        validarTenant(tenantId);
        return filialRepository.findAllByTenantId(tenantId);
    }

    public List<Filial> listarPorEmpresa(UUID tenantId, UUID empresaId) {
        buscarEmpresaDoTenant(tenantId, empresaId);
        return filialRepository.findAllByTenantIdAndEmpresaId(tenantId, empresaId);
    }

    public Filial buscarPorId(UUID tenantId, UUID filialId) {
        return filialRepository.findByIdAndTenantId(filialId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Filial nao encontrada para o tenant informado"));
    }

    @Transactional
    public Filial criar(UUID tenantId, UUID empresaId, String nome, String cnpj) {
        Tenant tenant = validarTenant(tenantId);
        Empresa empresa = buscarEmpresaDoTenant(tenantId, empresaId);
        Filial filial = new Filial(tenant, empresa, nome, cnpj);
        return filialRepository.save(filial);
    }

    @Transactional
    public Filial desativar(UUID tenantId, UUID filialId) {
        Filial filial = buscarPorId(tenantId, filialId);
        filial.desativar();
        return filialRepository.save(filial);
    }

    private Tenant validarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant nao encontrado"));
    }

    private Empresa buscarEmpresaDoTenant(UUID tenantId, UUID empresaId) {
        return empresaRepository.findByIdAndTenantId(empresaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada para o tenant informado"));
    }
}
