package com.traxup.tplug.erp.fiscal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class FiscalArquivoRetencaoDatabaseIntegrationTest {
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void triggerEstaInstaladoEBloqueiaExclusaoAntecipada() {
        Integer quantidade = jdbc.queryForObject("""
                SELECT count(*)
                FROM pg_trigger t
                JOIN pg_class c ON c.oid = t.tgrelid
                WHERE c.relname = 'fiscal_arquivos'
                  AND t.tgname = 'trg_proteger_retencao_arquivo_fiscal'
                  AND NOT t.tgisinternal
                """, Integer.class);
        assertThat(quantidade).isEqualTo(1);

        criarTabelaTemporaria("fiscal_retencao_delete_teste");
        jdbc.update("""
                INSERT INTO fiscal_retencao_delete_teste (retencao_ate)
                VALUES (CURRENT_DATE + INTERVAL '5 years')
                """);

        assertThatThrownBy(() -> jdbc.update(
                "DELETE FROM fiscal_retencao_delete_teste"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void bloqueiaReducaoDoPrazoRegistrado() {
        criarTabelaTemporaria("fiscal_retencao_update_teste");
        jdbc.update("""
                INSERT INTO fiscal_retencao_update_teste (retencao_ate)
                VALUES (CURRENT_DATE + INTERVAL '5 years')
                """);

        assertThatThrownBy(() -> jdbc.update("""
                UPDATE fiscal_retencao_update_teste
                SET retencao_ate = CURRENT_DATE + INTERVAL '4 years'
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private void criarTabelaTemporaria(String tabela) {
        jdbc.execute("CREATE TEMP TABLE " + tabela
                + " (retencao_ate DATE NOT NULL) ON COMMIT DROP");
        jdbc.execute("CREATE TRIGGER trg_retencao_teste "
                + "BEFORE UPDATE OF retencao_ate OR DELETE ON " + tabela
                + " FOR EACH ROW "
                + "EXECUTE FUNCTION proteger_retencao_arquivo_fiscal()");
    }
}
