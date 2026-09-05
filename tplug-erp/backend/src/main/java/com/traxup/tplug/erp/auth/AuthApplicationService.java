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
import java.util.UUID;

@Service
@Transactional
public class AuthApplicationService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthApplicationService(
            UsuarioRepository usuarioRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties properties) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.properties = properties;
    }

    public AuthTokens login(UUID tenantId, String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByTenantIdAndEmailIgnoreCase(tenantId, emailNormalizado)
                .filter(Usuario::isAtivo)
                .orElseThrow(this::credenciaisInvalidas);

        if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
            throw credenciaisInvalidas();
        }

        return emitirTokens(usuario);
    }

    public AuthTokens refresh(String refreshToken) {
        Instant agora = Instant.now();
        RefreshToken tokenAtual = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(this::refreshTokenInvalido);

        if (tokenAtual.estaRevogado() || tokenAtual.estaExpirado(agora)) {
            throw refreshTokenInvalido();
        }

        Usuario usuario = usuarioRepository.findByIdAndTenantId(tokenAtual.getUsuarioId(), tokenAtual.getTenantId())
                .filter(Usuario::isAtivo)
                .orElseThrow(this::refreshTokenInvalido);

        tokenAtual.revogar();
        refreshTokenRepository.save(tokenAtual);

        return emitirTokens(usuario);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .ifPresent(token -> {
                    token.revogar();
                    refreshTokenRepository.save(token);
                });
    }

    private AuthTokens emitirTokens(Usuario usuario) {
        String accessToken = jwtService.gerarAccessToken(usuario);
        String refreshToken = gerarRefreshToken();
        Instant expiraEm = Instant.now().plus(properties.refreshTokenDays(), ChronoUnit.DAYS);

        RefreshToken entidade = new RefreshToken(
                usuario.getTenant().getId(),
                usuario.getId(),
                hash(refreshToken),
                expiraEm);
        refreshTokenRepository.save(entidade);

        return new AuthTokens(accessToken, refreshToken, jwtService.accessTokenExpiresInSeconds());
    }

    private String gerarRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponivel", exception);
        }
    }

    private AutenticacaoException credenciaisInvalidas() {
        return new AutenticacaoException("Credenciais invalidas");
    }

    private AutenticacaoException refreshTokenInvalido() {
        return new AutenticacaoException("Refresh token invalido ou expirado");
    }
}
