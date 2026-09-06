package com.traxup.tplug.erp.financeiro.ofx;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OfxExtratoParserTest {
    private final OfxExtratoParser parser = new OfxExtratoParser();

    @Test
    void deveConverterCreditosEDebitosParaContratoDeConciliacao() {
        String ofx = "<OFX><BANKTRANLIST>" +
                "<STMTTRN><TRNTYPE>CREDIT<DTPOSTED>20260905120000<TRNAMT>150.25<FITID>CRED-1<MEMO>Venda</STMTTRN>" +
                "<STMTTRN><TRNTYPE>DEBIT<DTPOSTED>20260906103000<TRNAMT>-20.10<FITID>DEB-1<NAME>Tarifa</STMTTRN>" +
                "</BANKTRANLIST></OFX>";

        var itens = parser.parse(ofx);

        assertEquals(2, itens.size());
        assertEquals("OFX", itens.get(0).origem());
        assertEquals("CRED-1", itens.get(0).referenciaExterna());
        assertEquals("ENTRADA", itens.get(0).tipo());
        assertEquals(new BigDecimal("150.25"), itens.get(0).valor());
        assertEquals("SAIDA", itens.get(1).tipo());
        assertEquals(new BigDecimal("20.10"), itens.get(1).valor());
        assertEquals("Tarifa", itens.get(1).descricao());
    }

    @Test
    void deveRejeitarOfxSemFitid() {
        String ofx = "<OFX><BANKTRANLIST><STMTTRN><DTPOSTED>20260905<TRNAMT>10.00</STMTTRN></BANKTRANLIST></OFX>";
        assertThrows(IllegalArgumentException.class, () -> parser.parse(ofx));
    }
}
