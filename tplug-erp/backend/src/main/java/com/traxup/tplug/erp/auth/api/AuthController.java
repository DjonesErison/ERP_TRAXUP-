package com.traxup.tplug.erp.auth.api;

import com.traxup.tplug.erp.auth.AuthApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthApplicationService authApplicationService;

    public AuthController(AuthApplicationService authApplicationService) {
        this.authApplicationService = authApplicationService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(authApplicationService.login(request.tenantId(), request.email(), request.senha()));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return TokenResponse.from(authApplicationService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authApplicationService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recuperacao-senha/solicitar")
    public ResponseEntity<Void> solicitarRecuperacao(@Valid @RequestBody RecuperacaoSenhaSolicitarRequest request) {
        // Resposta intencionalmente neutra: nao revela se o e-mail existe no tenant.
        authApplicationService.solicitarRecuperacao(request.tenantId(), request.email());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/recuperacao-senha/confirmar")
    public ResponseEntity<Void> confirmarRecuperacao(@Valid @RequestBody RecuperacaoSenhaConfirmarRequest request) {
        authApplicationService.confirmarRecuperacao(request.token(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
