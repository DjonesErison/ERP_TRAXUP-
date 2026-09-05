package com.traxup.tplug.erp.empresa;

import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EmpresaApplicationService {

    private final EmpresaRepository empresaRepository;
    private final TenantRepository tenantRepository;

    public EmpresaApplicationService(EmpresaRepository empresaRepository, TenantRepository tenantRepository) {
        this.empresaRepository = empresaRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<Empresa> listar(UUID tenantId) {
        validarTenant(tenantId);
        return empresaRepository.findAllByTenantId(tenantId);
    }

    public Empresa buscarPorId(UUID tenantId, UUID empresaId) {
        return empresaRepository.findByIdAndTenantId(empresaId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa nao encontrada para o tenant informado"));
    }

    @Transactional
    public Empresa criar(UUID tenantId, String razaoSocial, String nomeFantasia, String cnpj) {
        Tenant tenant = validarTenant(tenantId);
        Empresa empresa = new Empresa(tenant, razaoSocial, nomeFantasia, cnpj);
        return empresaRepository.save(empresa);
    }

    @Transactional
    public Empresa desativar(UUID tenantId, UUID empresaId) {
        Empresa empresa = buscarPorId(tenantId, empresaId);
        empresa.desativar();
        return empresaRepository.save(empresa);
    }

    private Tenant validarTenant(UUID tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant nao encontrado"));
    }
}
