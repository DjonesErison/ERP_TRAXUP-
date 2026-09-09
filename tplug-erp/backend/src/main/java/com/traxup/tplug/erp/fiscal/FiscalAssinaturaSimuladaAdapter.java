package com.traxup.tplug.erp.fiscal;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class FiscalAssinaturaSimuladaAdapter implements FiscalAssinaturaPort {
    @Override
    public Resultado assinar(Comando comando) {
        if (comando.xml() == null || comando.xml().isBlank()) {
            throw new IllegalArgumentException("XML obrigatorio para assinatura simulada");
        }
        String marcador = sha256(comando.documentoId() + ":" + comando.certificadoId()
                + ":" + comando.thumbprintSha256() + ":" + sha256(comando.xml()));
        String conteudo = comando.xml() + "\n<!-- TraxUP: assinatura SIMULADA de homologacao; token="
                + marcador + " -->";
        return new Resultado(conteudo, sha256(conteudo), "SIMULADA", "SIMULADO_SHA256");
    }

    private String sha256(String valor) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }
}
