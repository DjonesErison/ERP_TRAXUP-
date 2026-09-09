package com.traxup.tplug.erp.fiscal;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class FiscalTransmissaoSimuladaAdapter implements FiscalTransmissaoPort {
    @Override
    public Resultado transmitir(Comando comando) {
        if (comando.conteudoAssinado() == null || comando.conteudoAssinado().isBlank()) {
            throw new IllegalArgumentException("Conteudo assinado obrigatorio para transmissao simulada");
        }
        String hashCalculado = sha256(comando.conteudoAssinado());
        if (!hashCalculado.equals(comando.hashRequisicao())) {
            throw new IllegalArgumentException("Hash do conteudo assinado divergente");
        }

        String protocolo = "SIM-" + sha256(comando.documentoId() + ":"
                + comando.assinaturaId() + ":" + comando.hashRequisicao()).substring(0, 32);
        String codigo = "100-SIM";
        String mensagem = "Autorizacao simulada de homologacao; sem validade fiscal";
        String hashResposta = sha256(protocolo + ":" + codigo + ":" + mensagem);
        return new Resultado("SIMULADO", "AUTORIZADO_SIMULADO", codigo,
                mensagem, protocolo, hashResposta);
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
