package com.traxup.tplug.erp.trial;

import com.traxup.tplug.erp.empresa.Empresa;
import com.traxup.tplug.erp.auth.AuthApplicationService;
import com.traxup.tplug.erp.empresa.EmpresaRepository;
import com.traxup.tplug.erp.tenant.Tenant;
import com.traxup.tplug.erp.tenant.TenantRepository;
import com.traxup.tplug.erp.trial.api.TrialCadastroRequest;
import com.traxup.tplug.erp.trial.api.TrialCadastroResponse;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
public class TrialProvisioningService {
    private final TenantRepository tenantRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TrialSaasRepository trialRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthApplicationService authApplicationService;
    private final Clock clock;
    private final com.traxup.tplug.erp.trial.mail.TrialEmailQueue emails;
    private final boolean emailEnabled;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public TrialProvisioningService(TenantRepository tenantRepository, EmpresaRepository empresaRepository,
                                    UsuarioRepository usuarioRepository, TrialSaasRepository trialRepository,
                                    PasswordEncoder passwordEncoder, AuthApplicationService authApplicationService,
                                    com.traxup.tplug.erp.trial.mail.TrialEmailQueue emails, @org.springframework.beans.factory.annotation.Value("${trial.mail.enabled:false}") boolean emailEnabled) {
        this(tenantRepository, empresaRepository, usuarioRepository, trialRepository, passwordEncoder, authApplicationService, Clock.systemUTC(), emails, emailEnabled);
    }

    TrialProvisioningService(TenantRepository tenantRepository, EmpresaRepository empresaRepository,
                             UsuarioRepository usuarioRepository, TrialSaasRepository trialRepository,
                             PasswordEncoder passwordEncoder, AuthApplicationService authApplicationService, Clock clock, com.traxup.tplug.erp.trial.mail.TrialEmailQueue emails, boolean emailEnabled) {
        this.emails=emails; this.emailEnabled=emailEnabled;
        this.tenantRepository = tenantRepository;
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.trialRepository = trialRepository;
        this.passwordEncoder = passwordEncoder;
        this.authApplicationService = authApplicationService;
        this.clock = clock;
    }

    @Transactional
    public TrialCadastroResponse provisionar(TrialCadastroRequest request) {
        String idempotencyKey = request.idempotencyKey().trim();
        return trialRepository.findByIdempotencyKey(idempotencyKey)
                .map(trial -> response(trial, null))
                .orElseGet(() -> criar(request, idempotencyKey));
    }

    private TrialCadastroResponse criar(TrialCadastroRequest request, String idempotencyKey) {
        String documento = somenteDigitos(request.documento());
        String telefone = somenteDigitos(request.telefone());
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        Instant agora = clock.instant();

        Tenant tenant = tenantRepository.save(new Tenant(request.nomeEmpresa().trim()));
        Empresa empresa = empresaRepository.save(new Empresa(tenant, request.razaoSocial().trim(),
                request.nomeEmpresa().trim(), documento.length() == 14 ? documento : null));

        // O administrador nasce sem senha conhecida pelo solicitante. A etapa de ativacao
        // substitui este segredo aleatorio por uma senha definida via token temporario.
        Usuario admin = usuarioRepository.save(new Usuario(tenant, request.nomeCompleto().trim(), email,
                passwordEncoder.encode(segredoAleatorio())));

        TrialSaas trial = trialRepository.saveAndFlush(new TrialSaas(tenant, empresa, admin, email, documento, telefone,
                normalizarOpcional(request.segmento()), request.quantidadeLojas(), request.termosVersao().trim(),
                idempotencyKey, agora));
        emails.enqueue(trial);
        String ativacaoToken = emailEnabled ? null : authApplicationService.criarAtivacaoAdministrador(admin);
        return response(trial, ativacaoToken);
    }

    private TrialCadastroResponse response(TrialSaas trial, String ativacaoToken) {
        return new TrialCadastroResponse(trial.getId(), trial.getTenantId(), trial.getExpiraEm(),
                trial.getStatus(), emailEnabled ? "VERIFICAR_EMAIL" : "ATIVAR_ADMINISTRADOR", ativacaoToken);
    }

    private String segredoAleatorio() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String somenteDigitos(String valor) { return valor == null ? "" : valor.replaceAll("\\D", ""); }
    private static String normalizarOpcional(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
}
