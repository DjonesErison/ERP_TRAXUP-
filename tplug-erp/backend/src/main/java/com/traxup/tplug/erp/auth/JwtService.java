package com.traxup.tplug.erp.auth;

import com.traxup.tplug.erp.usuario.Usuario;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;
    private final AutorizacaoRepository autorizacaoRepository;

    public JwtService(
            JwtEncoder jwtEncoder,
            JwtProperties properties,
            AutorizacaoRepository autorizacaoRepository) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
        this.autorizacaoRepository = autorizacaoRepository;
    }

    public String gerarAccessToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(properties.accessTokenMinutes(), ChronoUnit.MINUTES);
        List<String> permissoes = autorizacaoRepository.listarPermissoesEfetivas(
                usuario.getTenant().getId(), usuario.getId());

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .subject(usuario.getId().toString())
                .claim("tenant_id", usuario.getTenant().getId().toString())
                .claim("email", usuario.getEmail())
                .claim("permissions", permissoes)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long accessTokenExpiresInSeconds() {
        return properties.accessTokenMinutes() * 60;
    }
}
