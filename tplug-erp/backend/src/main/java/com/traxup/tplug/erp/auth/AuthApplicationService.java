package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.shared.exception.AutenticacaoException;
import com.traxup.tplug.erp.usuario.Usuario;
import com.traxup.tplug.erp.usuario.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthApplicationService {

    private final com.traxup.tplug.erp.trial.TrialSaasRepository trialRepository;
    private final com.traxup.tplug.erp.tenant.TenantRepository tenantRepository;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RecuperacaoSenhaTokenRepository recuperacaoSenhaTokenRepository;
    private final AtivacaoAdminTokenRepository ativacaoAdminTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthApplicationService(UsuarioRepository usuarioRepository, RefreshTokenRepository refreshTokenRepository,
            RecuperacaoSenhaTokenRepository recuperacaoSenhaTokenRepository, AtivacaoAdminTokenRepository ativacaoAdminTokenRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, JwtProperties properties, com.traxup.tplug.erp.trial.TrialSaasRepository trialRepository, com.traxup.tplug.erp.tenant.TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
        this.trialRepository=trialRepository;
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.recuperacaoSenhaTokenRepository = recuperacaoSenhaTokenRepository;
        this.ativacaoAdminTokenRepository = ativacaoAdminTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    public java.util.Optional<UUID> resolverTenant(String codigoEmpresa, UUID tenantId) {
        return codigoEmpresa == null ? java.util.Optional.ofNullable(tenantId)
                : tenantRepository.findByCodigoEmpresa(codigoEmpresa)
                    .filter(com.traxup.tplug.erp.tenant.Tenant::isAtivo)
                    .map(com.traxup.tplug.erp.tenant.Tenant::getId);
    }

    public AuthTokens loginPorEmpresa(String codigoEmpresa, UUID tenantId, String email, String senha) {
        return login(resolverTenant(codigoEmpresa, tenantId).orElseThrow(this::credenciaisInvalidas), email, senha);
    }

    public AuthTokens login(UUID tenantId, String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        Usuario usuario = usuarioRepository.findByTenantIdAndEmailIgnoreCase(tenantId, emailNormalizado)
                .filter(Usuario::isAtivo).orElseThrow(this::credenciaisInvalidas);
        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) throw credenciaisInvalidas();
        return emitirTokens(usuario);
    }

    public AuthTokens refresh(String refreshToken) {
        Instant agora = Instant.now();
        RefreshToken tokenAtual = refreshTokenRepository.findByTokenHash(hash(refreshToken)).orElseThrow(this::refreshTokenInvalido);
        if (tokenAtual.estaRevogado() || tokenAtual.estaExpirado(agora)) throw refreshTokenInvalido();
        Usuario usuario = usuarioRepository.findByIdAndTenantId(tokenAtual.getUsuarioId(), tokenAtual.getTenantId())
                .filter(Usuario::isAtivo).orElseThrow(this::refreshTokenInvalido);
        tokenAtual.revogar();
        refreshTokenRepository.save(tokenAtual);
        return emitirTokens(usuario);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresent(token -> {
            token.revogar();
            refreshTokenRepository.save(token);
        });
    }

    /** Retorna o token somente para a camada de entrega; a API publica sempre resposta neutra. */
    public Optional<String> solicitarRecuperacao(UUID tenantId, String email) {
        return usuarioRepository.findByTenantIdAndEmailIgnoreCase(tenantId, email.trim().toLowerCase(Locale.ROOT))
                .filter(Usuario::isAtivo)
                .map(usuario -> {
                    String token = gerarTokenAleatorio();
                    recuperacaoSenhaTokenRepository.save(new RecuperacaoSenhaToken(
                            usuario.getTenant(), usuario, hash(token), Instant.now().plus(30, ChronoUnit.MINUTES)));
                    return token;
                });
    }

    public void confirmarRecuperacao(String token, String novaSenha) {
        Instant agora = Instant.now();
        RecuperacaoSenhaToken recuperacao = recuperacaoSenhaTokenRepository.findByTokenHash(hash(token))
                .filter(item -> item.podeUsar(agora))
                .orElseThrow(() -> new AutenticacaoException("Token de recuperacao invalido ou expirado"));
        Usuario usuario = recuperacao.getUsuario();
        if (!usuario.isAtivo()) throw new AutenticacaoException("Token de recuperacao invalido ou expirado");
        usuario.alterarSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        recuperacao.marcarUsado(agora);
        recuperacaoSenhaTokenRepository.save(recuperacao);
    }


    public String criarAtivacaoAdministrador(Usuario usuario) {
        String token = gerarTokenAleatorio();
        ativacaoAdminTokenRepository.save(new AtivacaoAdminToken(usuario.getTenant(), usuario, hash(token),
                Instant.now().plus(30, ChronoUnit.MINUTES)));
        return token;
    }

    public void confirmarAtivacaoAdministrador(String token, String novaSenha) {
        Instant agora = Instant.now();
        AtivacaoAdminToken ativacao = ativacaoAdminTokenRepository.findByTokenHash(hash(token))
                .filter(item -> item.podeUsar(agora))
                .orElseThrow(() -> new AutenticacaoException("Token de ativacao invalido ou expirado"));
        Usuario usuario = ativacao.getUsuario();
        var trial=trialRepository.lockByAdministradorId(usuario.getId()).orElseThrow(() -> new AutenticacaoException("Ativacao indisponivel"));
        if(!usuario.isAtivo() || trial.isAdminAtivado() || !trial.getExpiraEm().isAfter(agora)) throw new AutenticacaoException("Ativacao indisponivel");
        trial.marcarAdminAtivado(agora);
        trialRepository.save(trial);
        usuario.alterarSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        ativacao.marcarUsado(agora);
        ativacaoAdminTokenRepository.save(ativacao);
        ativacaoAdminTokenRepository.consumeAll(usuario.getId(),agora);
    }

    private AuthTokens emitirTokens(Usuario usuario) {
        String accessToken = jwtService.gerarAccessToken(usuario);
        String refreshToken = gerarTokenAleatorio();
        Instant expiraEm = Instant.now().plus(properties.refreshTokenDays(), ChronoUnit.DAYS);
        refreshTokenRepository.save(new RefreshToken(usuario.getTenant().getId(), usuario.getId(), hash(refreshToken), expiraEm));
        return new AuthTokens(accessToken, refreshToken, jwtService.accessTokenExpiresInSeconds());
    }

    private String gerarTokenAleatorio() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel", exception);
        }
    }

    private AutenticacaoException credenciaisInvalidas() { return new AutenticacaoException("Credenciais invalidas"); }
    private AutenticacaoException refreshTokenInvalido() { return new AutenticacaoException("Refresh token invalido ou expirado"); }
}
