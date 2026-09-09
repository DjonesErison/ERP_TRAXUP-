package com.traxup.tplug.erp.fiscal;

import com.traxup.tplug.erp.auditoria.AuditoriaApplicationService;
import com.traxup.tplug.erp.shared.exception.RecursoNaoEncontradoException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Service
public class FiscalTentativaTransmissaoApplicationService {
    private final JdbcTemplate jdbc;
    private final FiscalTransmissaoPort transmissoror;
    private final AuditoriaApplicationService auditoria;

    public FiscalTentativaTransmissaoApplicationService(JdbcTemplate jdbc,
            FiscalTransmissaoPort transmissor, AuditoriaApplicationService auditoria) {
        this.jdbc = jdbc; this.transmissor = transmissor; this.auditoria = auditoria;
    }

    @Transactional
    public Resultado transmitir(UUID tenantId, UUID usuarioId, UUID tentativaId) {
        var existente = buscar(tenantId, tentativaId);
        if (!existente.isEmpty()) return existente.getFirst().comRepetida(true);
        Origem o = jdbc.query("""
                SELECT t.numero, t.status, t.solicitacao_id, t.documento_id,
                       d.filial_id, d.ambiente, a.id assinatura_id,
                       a.conteudo_assinado, a.hash_sha256, a.tipo_assinatura
                FROM fiscal_tentativas_emissao t
                JOIN fiscal_documentos d ON d.tenant_id=t.tenant_id AND d.id=t.documento_id
                JOIN fiscal_documentos_assinaturas a
                  ON a.tenant_id=t.tenant_id AND a.tentativa_id=t.id
                WHERE t.tenant_id=? AND t.id=? FOR UPDATE OF t
                """, (rs,n)->new Origem(rs.getInt("numero"),rs.getString("status"),
                        rs.getObject("solicitacao_id",UUID.class),
                        rs.getObject("documento_id",UUID.class),
                        rs.getObject("filial_id",UUID.class),rs.getString("ambiente"),
                        rs.getObject("assinatura_id",UUID.class),
                        rs.getString("conteudo_assinado"),rs.getString("hash_sha256"),
                        rs.getString("tipo_assinatura")),tenantId,tentativaId)
                .stream().findFirst().orElseThrow(()->new RecursoNaoEncontradoException(
                        "Assinatura da tentativa nao encontrada para o tenant"));
        validar(o.status(),o.ambiente(),o.tipo());
        var resposta=transmissor.transmitir(new FiscalTransmissaoPort.Comando(
                o.documentoId(),o.assinaturaId(),o.conteudo(),o.hash()));
        UUID id=UUID.nameUUIDFromBytes((tentativaId+":transmissao:simulada")
                .getBytes(StandardCharsets.UTF_8));
        int inseridos=jdbc.update("""
                INSERT INTO fiscal_transmissoes
                  (id,tenant_id,solicitacao_id,documento_id,assinatura_id,tentativa_id,
                   ambiente,provedor,status,codigo_resposta,mensagem_resposta,protocolo,
                   hash_requisicao,hash_resposta)
                VALUES (?,?,?,?,?,?,'HOMOLOGACAO',?,?,?,?,?,?,?) ON CONFLICT DO NOTHING
                """,id,tenantId,o.solicitacaoId(),o.documentoId(),o.assinaturaId(),
                tentativaId,resposta.provedor(),resposta.status(),resposta.codigoResposta(),
                resposta.mensagemResposta(),resposta.protocolo(),o.hash(),resposta.hashResposta());
        if(inseridos==0) return buscar(tenantId,tentativaId).stream().findFirst()
                .map(r->r.comRepetida(true)).orElseThrow();
        jdbc.update("""
                UPDATE fiscal_tentativas_emissao SET status='CONCLUIDA',
                  concluida_em=CURRENT_TIMESTAMP
                WHERE tenant_id=? AND id=? AND status='EM_PROCESSAMENTO'
                """,tenantId,tentativaId);
        auditoria.registrar(tenantId,usuarioId,null,o.filialId(),
                "TRANSMITIR_TENTATIVA_FISCAL_SIMULADA","FISCAL_TRANSMISSAO",id,
                "documentoId="+o.documentoId()+";tentativaId="+tentativaId+
                ";numero="+o.numero()+";status=AUTORIZADO_SIMULADO;protocolo="+resposta.protocolo());
        return new Resultado(id,o.documentoId(),tentativaId,o.numero(),o.assinaturaId(),
                resposta.status(),resposta.codigoResposta(),resposta.protocolo(),
                resposta.hashResposta(),false);
    }

    private List<Resultado> buscar(UUID tenantId,UUID tentativaId){
        return jdbc.query("""
                SELECT tr.id,tr.documento_id,tr.tentativa_id,t.numero,tr.assinatura_id,
                       tr.status,tr.codigo_resposta,tr.protocolo,tr.hash_resposta
                FROM fiscal_transmissoes tr JOIN fiscal_tentativas_emissao t
                  ON t.tenant_id=tr.tenant_id AND t.id=tr.tentativa_id
                WHERE tr.tenant_id=? AND tr.tentativa_id=?
                """,(rs,n)->new Resultado(rs.getObject("id",UUID.class),
                        rs.getObject("documento_id",UUID.class),
                        rs.getObject("tentativa_id",UUID.class),rs.getInt("numero"),
                        rs.getObject("assinatura_id",UUID.class),rs.getString("status"),
                        rs.getString("codigo_resposta"),rs.getString("protocolo"),
                        rs.getString("hash_resposta"),false),tenantId,tentativaId);
    }

    static void validar(String status,String ambiente,String tipo){
        if(!"EM_PROCESSAMENTO".equals(status)) throw new IllegalArgumentException("Transmissao exige tentativa EM_PROCESSAMENTO");
        if(!"HOMOLOGACAO".equals(ambiente)) throw new IllegalArgumentException("Transmissao simulada permitida somente em HOMOLOGACAO");
        if(!"SIMULADA".equals(tipo)) throw new IllegalArgumentException("Transmissao simulada exige assinatura SIMULADA");
    }

    record Origem(int numero,String status,UUID solicitacaoId,UUID documentoId,
                  UUID filialId,String ambiente,UUID assinaturaId,String conteudo,
                  String hash,String tipo){}
    public record Resultado(UUID transmissaoId,UUID documentoId,UUID tentativaId,
            int tentativaNumero,UUID assinaturaId,String status,String codigoResposta,
            String protocolo,String hashResposta,boolean repetida){
        Resultado comRepetida(boolean v){return new Resultado(transmissaoId,documentoId,
                tentativaId,tentativaNumero,assinaturaId,status,codigoResposta,protocolo,
                hashResposta,v);}
    }
}
