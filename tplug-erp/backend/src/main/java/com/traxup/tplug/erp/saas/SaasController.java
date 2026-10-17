package com.traxup.tplug.erp.saas;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@RestController
@RequestMapping("/api/v1/saas")
@PreAuthorize("@saasAccess.permitido()")
public class SaasController {
 private final SaasService service;
 public SaasController(SaasService service){this.service=service;}
 @GetMapping("/painel") public Map<String,Object> painel(){return service.painel();}
 @PostMapping("/trials/{id}/prorrogar") public void prorrogar(@PathVariable UUID id,@Valid @RequestBody Dias d){service.prorrogar(id,d.dias());}
 @PostMapping("/trials/{id}/converter") public void converter(@PathVariable UUID id,@Valid @RequestBody Contrato d){service.converter(id,d);}
 @PostMapping("/trials/{id}/encerrar") public void encerrar(@PathVariable UUID id){service.encerrar(id);}
 @PostMapping("/clientes/{id}/plano") public void plano(@PathVariable UUID id,@Valid @RequestBody Contrato d){service.plano(id,d);}
 @PostMapping("/clientes/{id}/bloqueio") public void bloqueio(@PathVariable UUID id,@RequestBody Bloqueio d){service.bloqueio(id,d.bloqueado());}
 @PostMapping("/trials/{id}/notas") public void nota(@PathVariable UUID id,@Valid @RequestBody Nota d){service.nota(id,d.texto());}
 @PostMapping("/faturas") public void emitir(@Valid @RequestBody Emissao d){service.emitir(d);}
 @PostMapping("/faturas/{id}/pagamentos") public void pagamento(@PathVariable long id,@Valid @RequestBody Pagamento d){service.pagamento(id,d);}
 @PostMapping("/faturas/{id}/vencimento") public void vencimento(@PathVariable long id,@Valid @RequestBody Vencimento d){service.vencimento(id,d.data());}
 @PostMapping("/faturas/{id}/cancelar") public void cancelar(@PathVariable long id){service.cancelar(id);}
 @PutMapping("/planos/{nome}") public void catalogo(@PathVariable String nome,@Valid @RequestBody Plano d){service.catalogo(nome,d);}
 @PostMapping("/relatorios") public void relatorio(@Valid @RequestBody Relatorio d){service.relatorio(d);}
 @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
 public org.springframework.http.ProblemDetail erro(org.springframework.web.server.ResponseStatusException e){return e.getBody();}
 public record Dias(@Min(1) @Max(30) int dias){}
 public record Contrato(@NotBlank @Size(max=40) String plano,@NotEmpty List<@Pattern(regexp="ERP|PDV|Fiscal|CRM|BI") String> modulos,@DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal mensalidade){}
 public record Bloqueio(boolean bloqueado){}
 public record Nota(@NotBlank @Size(max=500) String texto){}
 public record Emissao(@NotNull UUID cliente,@NotNull @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal valor,@NotNull LocalDate vencimento,@NotNull UUID chave){}
 public record Pagamento(@NotNull @DecimalMin("0.01") @Digits(integer=12,fraction=2) BigDecimal valor,@NotNull @PastOrPresent LocalDate data,@Pattern(regexp="Pix|Boleto|Cartão") @NotNull String forma,@NotNull UUID chave){}
 public record Vencimento(@NotNull LocalDate data){}
 public record Plano(@NotNull @DecimalMin("0.00") @Digits(integer=12,fraction=2) BigDecimal preco,@Min(0) int usuarios,@Min(0) int lojas){}
 public record Relatorio(@NotBlank @Size(max=80) String nome,@NotNull @Size(max=40) String plano,@Min(3) @Max(6) int periodo){}
}
