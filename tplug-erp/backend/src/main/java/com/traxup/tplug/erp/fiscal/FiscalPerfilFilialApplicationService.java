package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.filial.Filial;
import com.traxup.tplug.erp.filial.FilialRepository;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FiscalPerfilFilialApplicationService {
    private static final Set<String> REGIMES = Set.of("SIMPLES_NACIONAL", "REGIME_NORMAL");
    private static final Set<String> AMBIENTES = Set.of("HOMOLOGACAO", "PRODUCAO");

    private final FiscalPerfilFilialRepository repository;
    private final FilialRepository filialRepository;
    private final AuditoriaApplicationService auditoria;

    public FiscalPerfilFilialApplicationService(FiscalPerfilFilialRepository repository,
                                                FilialRepository filialRepository,
                                                AuditoriaApplicationService auditoria) {
        this.repository = repository;
        this.filialRepository = filialRepository;
        this.auditoria = auditoria;
    }

    public FiscalPerfilFilial buscar(UUID tenantId, UUID filialId) {
        validarFilial(tenantId, filialId);
        return repository.findByTenantIdAndFilialIdAndAtivoTrue(tenantId, filialId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Perfil fiscal nao encontrado para a filial informada"));
    }

    @Transactional
    public FiscalPerfilFilial salvar(UUID tenantId, UUID usuarioId, UUID filialId,
                                     String regimeTributario, short crt, String ambiente,
                                     int serieNfe, int serieNfce) {
        Filial filial = validarFilial(tenantId, filialId);
        String regimeNormalizado = normalizar(regimeTributario, REGIMES, "Regime tributario invalido");
        String ambienteNormalizado = normalizar(ambiente, AMBIENTES, "Ambiente fiscal invalido");
        validarCrt(regimeNormalizado, crt);
        validarSerie(serieNfe, "Serie da NFe");
        validarSerie(serieNfce, "Serie da NFCe");

        FiscalPerfilFilial perfil = repository.findByTenantIdAndFilialId(tenantId, filialId)
                .orElseGet(() -> new FiscalPerfilFilial(
                        tenantId, filialId, regimeNormalizado, crt,
                        ambienteNormalizado, serieNfe, serieNfce));
        perfil.atualizar(regimeNormalizado, crt, ambienteNormalizado, serieNfe, serieNfce);
        perfil = repository.saveAndFlush(perfil);

        auditoria.registrar(tenantId, usuarioId, filial.getEmpresa().getId(), filialId,
                "SALVAR_PERFIL", "FISCAL_PERFIL_FILIAL", perfil.getId(),
                "regime=" + regimeNormalizado + ";crt=" + crt + ";ambiente=" + ambienteNormalizado);
        return perfil;
    }

    private Filial validarFilial(UUID tenantId, UUID filialId) {
        Filial filial = filialRepository.findByIdAndTenantId(filialId, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Filial nao encontrada para o tenant informado"));
        if (!filial.isAtivo()) {
            throw new IllegalArgumentException("Filial inativa nao pode possuir perfil fiscal ativo");
        }
        return filial;
    }

    private String normalizar(String valor, Set<String> permitidos, String mensagem) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(mensagem);
        String normalizado = valor.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_').replace(' ', '_');
        if (!permitidos.contains(normalizado)) throw new IllegalArgumentException(mensagem);
        return normalizado;
    }

    private void validarCrt(String regime, short crt) {
        boolean valido = ("SIMPLES_NACIONAL".equals(regime) && (crt == 1 || crt == 2))
                || ("REGIME_NORMAL".equals(regime) && crt == 3);
        if (!valido) throw new IllegalArgumentException("CRT incompativel com o regime tributario");
    }

    private void validarSerie(int serie, String campo) {
        if (serie < 1 || serie > 999) {
            throw new IllegalArgumentException(campo + " deve estar entre 1 e 999");
        }
    }
}
