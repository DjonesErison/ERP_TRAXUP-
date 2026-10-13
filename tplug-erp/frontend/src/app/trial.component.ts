import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { TrialResponse, TrialService } from './trial.service';

@Component({
 selector:'app-trial',standalone:true,imports:[CommonModule,FormsModule],
 template:`
 <main class="trial-page">
  <section class="showcase">
   <img src="/assets/traxup-logo.webp" alt="TRAXUP" class="trial-logo">
   <div class="showcase-copy"><h1>{{modoAtivacao?'Ative sua conta':'Transforme a gestão da sua empresa'}}<br><span>{{modoAtivacao?'e comece agora':'com a TraxUp'}}</span></h1>
    <div class="advantages"><b>✓ 7 dias grátis</b><b>✓ Sem compromisso</b><b>✓ Configuração guiada</b></div>
    <div class="solutions"><div><i>▥</i><b>ERP<small>Gestão completa do seu negócio</small></b></div><div><i>🛒</i><b>PDV<small>Vendas mais rápidas e seguras</small></b></div><div><i>▤</i><b>Fiscal<small>Conformidade e segurança</small></b></div><div><i>▣</i><b>Financeiro<small>Mais controle e lucratividade</small></b></div><div><i>♙</i><b>CRM<small>Relacionamento que gera vendas</small></b></div><div><i>▥</i><b>BI<small>Decisões baseadas em dados</small></b></div></div>
   </div>
   <img class="dashboard-preview" src="/assets/ui-001-dashboard-traxup-erp.webp" alt="Dashboard TRAXUP">
  </section>
  <section class="form-area">
   <section class="signup-card" *ngIf="!resultado">
    <ng-container *ngIf="!modoAtivacao && etapa===1; else passwordStep">
     <header><h2>Comece seu teste grátis</h2><p>Tenha acesso à plataforma por 7 dias</p></header>
     <form #captacaoForm="ngForm" (ngSubmit)="avancar(captacaoForm)" novalidate>
      <div class="fields">
       <label>Nome completo *<input name="responsavel" [(ngModel)]="m.responsavel" required maxlength="150" placeholder="Seu nome completo"></label>
       <label>Nome da empresa *<input name="empresa" [(ngModel)]="m.empresa" required maxlength="200" placeholder="Nome da empresa"></label>
       <label>Razão social *<input name="razaoSocial" [(ngModel)]="m.razaoSocial" required maxlength="200" placeholder="Razão social da empresa"></label>
       <label>CNPJ/CPF *<input name="documento" [(ngModel)]="m.documento" required maxlength="18" placeholder="00.000.000/0000-00"></label>
       <label>Telefone/WhatsApp *<input name="telefone" [(ngModel)]="m.telefone" required maxlength="30" placeholder="(11) 96123-4567"></label>
       <label>E-mail profissional *<input name="email" type="email" [(ngModel)]="m.email" required maxlength="254" placeholder="seu@empresa.com.br"></label>
       <label>Segmento da empresa *<select name="segmento" [(ngModel)]="m.segmento" required><option value="" disabled>Selecione o segmento</option><option>Comércio</option><option>Serviços</option><option>Alimentação</option><option>Imobiliário</option><option>Outro</option></select></label>
       <label>Quantidade de lojas *<select name="quantidadeLojas" [(ngModel)]="m.quantidadeLojas" required><option *ngFor="let q of quantidades" [ngValue]="q">{{q}}{{q===10?' ou mais':''}}</option></select></label>
      </div>
      <label class="terms"><input type="checkbox" name="aceiteTermos" [(ngModel)]="m.aceiteTermos" required> Li e aceito os <a href="#">Termos de Uso</a> e a <a href="#">Política de Privacidade</a>.</label>
      <p class="form-error" *ngIf="erro">{{erro}}</p><button class="primary-action" type="submit">Criar minha conta grátis →</button><p class="protected">🔒 Seus dados protegidos conforme a LGPD</p>
     </form>
    </ng-container>
    <ng-template #passwordStep><div class="password-step">
     <button class="back" *ngIf="!modoAtivacao" type="button" (click)="etapa=1">← Voltar</button><header><h2>{{modoAtivacao?'Ative sua conta':'Crie sua senha'}}</h2><p>{{modoAtivacao?'Defina sua senha para acessar o TRAXUP.':'Último passo para ativar seu ambiente TRAXUP.'}}</p></header>
     <form #senhaForm="ngForm" (ngSubmit)="enviar(senhaForm)">
      <label *ngIf="modoAtivacao">E-mail profissional<input name="emailAtivacao" type="email" [(ngModel)]="m.email" required placeholder="seu@empresa.com.br"></label>
      <label *ngIf="modoAtivacao">Código da empresa<input name="tenantAtivacao" [(ngModel)]="tenantAtivacao" required placeholder="Código recebido por e-mail"></label>
      <label>Nova senha<input name="senha" type="password" minlength="12" maxlength="72" [(ngModel)]="m.senha" required placeholder="Mínimo de 12 caracteres" autocomplete="new-password"></label>
      <label>Confirmar senha<input name="confirmacao" type="password" minlength="12" maxlength="72" [(ngModel)]="confirmacaoSenha" required placeholder="Repita a senha" autocomplete="new-password"></label>
      <p class="form-error" *ngIf="erro">{{erro}}</p><button class="primary-action" type="submit" [disabled]="enviando">{{enviando?'Ativando...':(modoAtivacao?'Ativar minha conta →':'Ativar meu teste grátis →')}}</button>
      <p class="protected">🔒 Seus dados estão protegidos</p>
     </form></div></ng-template>
   </section>
   <section class="signup-card success" *ngIf="resultado"><div class="success-icon">✓</div><h2>Seu TRAXUP está pronto.</h2><p>Seu teste gratuito de 7 dias começou.</p><button class="primary-action" (click)="irParaErp()">Continuar para o primeiro acesso</button></section>
  </section>
 </main>`,
 styleUrl:'./trial.component.css'
})
export class TrialComponent{
 @Input() modoAtivacao=false; readonly quantidades=[1,2,3,4,5,6,7,8,9,10]; etapa=1;tenantAtivacao='';confirmacaoSenha='';
 m={empresa:'',razaoSocial:'',documento:'',responsavel:'',email:'',telefone:'',segmento:'',quantidadeLojas:1,senha:'',aceiteTermos:false};enviando=false;erro='';resultado?:TrialResponse;
 constructor(private trial:TrialService){}
 avancar(form:NgForm){this.erro='';if(form.invalid){form.control.markAllAsTouched();this.erro='Preencha os campos obrigatórios e aceite os termos para continuar.';return}this.etapa=2}
 enviar(form:NgForm){this.erro='';if(form.invalid){form.control.markAllAsTouched();this.erro='Preencha os dados e crie uma senha com pelo menos 12 caracteres.';return}if(this.m.senha!==this.confirmacaoSenha){this.erro='As senhas informadas não coincidem.';return}if(this.modoAtivacao){localStorage.setItem('traxup_trial_tenant',this.tenantAtivacao.trim());window.location.href='/';return}if(this.enviando)return;this.enviando=true;this.trial.criar(this.m).subscribe({next:r=>{this.resultado=r;this.enviando=false},error:e=>{this.enviando=false;this.erro=e?.error?.message||e?.error?.detail||'Não foi possível criar o ambiente. Revise os dados e tente novamente.'}})}
 irParaErp(){if(!this.resultado)return;localStorage.setItem('traxup_trial_tenant',this.resultado.tenantId);window.location.href='/'}
}
