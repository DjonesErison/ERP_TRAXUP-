package com.traxup.tplug.erp.pessoa;

import com.traxup.tplug.erp.crm.ClienteInativoProjection;
import com.traxup.tplug.erp.venda.PedidoVenda;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PessoaRepository extends JpaRepository<Pessoa, UUID> {
    List<Pessoa> findAllByTenantIdOrderByNomeRazaoSocialAsc(UUID tenantId);
    List<Pessoa> findAllByTenantIdAndClienteTrueOrderByNomeRazaoSocialAsc(UUID tenantId);
    List<Pessoa> findAllByTenantIdAndFornecedorTrueOrderByNomeRazaoSocialAsc(UUID tenantId);
    Optional<Pessoa> findByIdAndTenantId(UUID id, UUID tenantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pessoa p where p.id = :id and p.tenant.id = :tenantId")
    Optional<Pessoa> findByIdAndTenantIdForUpdate(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query("""
            select p.id as clienteId,
                   p.nomeRazaoSocial as nomeRazaoSocial,
                   p.nomeFantasia as nomeFantasia,
                   p.email as email,
                   p.telefone as telefone,
                   max(v.criadoEm) as ultimaCompraEm,
                   count(v.id) as quantidadeCompras
            from Pessoa p, PedidoVenda v
            where p.id = v.clienteId
              and p.tenant.id = :tenantId
              and v.tenantId = :tenantId
              and p.cliente = true
              and p.ativo = true
              and v.status = 'FATURADO'
              and (:filialId is null or v.filialId = :filialId)
            group by p.id, p.nomeRazaoSocial, p.nomeFantasia, p.email, p.telefone
            having max(v.criadoEm) <= :limiteInatividade
            order by max(v.criadoEm) asc, p.nomeRazaoSocial asc, p.id asc
            """)
    List<ClienteInativoProjection> buscarClientesInativos(@Param("tenantId") UUID tenantId,
                                                           @Param("filialId") UUID filialId,
                                                           @Param("limiteInatividade") Instant limiteInatividade,
                                                           Pageable pageable);

    boolean existsByTenantIdAndCpfCnpj(UUID tenantId, String cpfCnpj);
}
