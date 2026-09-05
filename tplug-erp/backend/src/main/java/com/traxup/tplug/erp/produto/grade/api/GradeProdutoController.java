package com.traxup.tplug.erp.produto.grade.api;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.auth.TenantContext;
import com.traxup.tplug.erp.produto.grade.GradeProduto;
import com.traxup.tplug.erp.produto.grade.GradeProdutoApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/produtos/{produtoId}/grades")
public class GradeProdutoController {

    private final GradeProdutoApplicationService service;
    private final AuditoriaApplicationService auditoriaApplicationService;
    private final TenantContext tenantContext;

    public GradeProdutoController(GradeProdutoApplicationService service,
                                  AuditoriaApplicationService auditoriaApplicationService,
                                  TenantContext tenantContext) {
        this.service = service;
        this.auditoriaApplicationService = auditoriaApplicationService;
        this.tenantContext = tenantContext;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GRADE_PRODUTO_LER')")
    public List<GradeProdutoResponse> listar(@PathVariable UUID produtoId) {
        UUID tenantId = tenantContext.tenantId();
        return service.listarPorProduto(tenantId, produtoId).stream().map(GradeProdutoResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GRADE_PRODUTO_CRIAR')")
    public ResponseEntity<GradeProdutoResponse> criar(@PathVariable UUID produtoId,
                                                      @Valid @RequestBody CriarGradeProdutoRequest request) {
        UUID tenantId = tenantContext.tenantId();
        GradeProduto grade = service.criar(tenantId, produtoId, request.codigoGrade(), request.descricaoGrade(),
                request.codigoBarra(), request.vendaPrc());

        auditoriaApplicationService.registrar(
                tenantId,
                tenantContext.usuarioIdOuNulo(),
                null,
                null,
                "CRIAR",
                "GRADE_PRODUTO",
                grade.getId(),
                "produtoId=" + produtoId);

        return ResponseEntity.created(URI.create("/api/v1/produtos/" + produtoId + "/grades/" + grade.getId()))
                .body(GradeProdutoResponse.from(grade));
    }

    @PatchMapping("/{gradeId}/desativar")
    @PreAuthorize("hasAuthority('GRADE_PRODUTO_DESATIVAR')")
    public GradeProdutoResponse desativar(@PathVariable UUID produtoId, @PathVariable UUID gradeId) {
        UUID tenantId = tenantContext.tenantId();
        GradeProduto grade = service.buscarPorId(tenantId, gradeId);
        if (!grade.getProduto().getId().equals(produtoId)) {
            throw new com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException("Grade nao pertence ao produto informado");
        }
        grade = service.desativar(tenantId, gradeId);

        auditoriaApplicationService.registrar(
                tenantId,
                tenantContext.usuarioIdOuNulo(),
                null,
                null,
                "DESATIVAR",
                "GRADE_PRODUTO",
                grade.getId(),
                "produtoId=" + produtoId);

        return GradeProdutoResponse.from(grade);
    }
}
