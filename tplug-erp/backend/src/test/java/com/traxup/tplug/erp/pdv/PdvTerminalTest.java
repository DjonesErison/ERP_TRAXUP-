package com.traxup.tplug.erp.pdv;

import com.traxup.tplug.erp.pdv.api.PdvTerminalResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PdvTerminalTest {
    @Test
    void novoTerminalDeveNascerAtivoComIdentidadeEstavel() {
        UUID tenantId = UUID.randomUUID();
        UUID filialId = UUID.randomUUID();
        PdvTerminal terminal = new PdvTerminal(tenantId, filialId, "CAIXA-01", "Caixa 01", 1);

        PdvTerminalResponse response = PdvTerminalResponse.from(terminal);

        assertThat(response.id()).isEqualTo(terminal.getId());
        assertThat(response.filialId()).isEqualTo(filialId);
        assertThat(response.codigo()).isEqualTo("CAIXA-01");
        assertThat(response.serie()).isEqualTo(1);
        assertThat(response.ativo()).isTrue();
    }

    @Test
    void terminalDevePermitirDesativacaoEReativacaoSemTrocarIdentidade() {
        PdvTerminal terminal = new PdvTerminal(UUID.randomUUID(), UUID.randomUUID(), "PDV-02", "PDV 02", 2);
        UUID id = terminal.getId();

        terminal.desativar();
        assertThat(terminal.isAtivo()).isFalse();
        terminal.ativar();

        assertThat(terminal.isAtivo()).isTrue();
        assertThat(terminal.getId()).isEqualTo(id);
        assertThat(terminal.getSerie()).isEqualTo(2);
    }
}
