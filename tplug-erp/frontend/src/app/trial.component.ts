import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TrialService, TrialResponse } from './trial.service';

@Component({
 selector:'app-trial', standalone:true, imports:[CommonModule,FormsModule],
 template:`
 <main class="trial-page">
  <header><div class="logo"><b>T</b><span>TRAXUP</span></div><span class="pill">TRIAL · 7 DIAS GRÁTIS</span></header>
  <section class="hero">
   <div class="copy">
    <p class="eyebrow">ERP + PDV PARA SUA EMPRESA</p>
    <h1>Comece agora. Configure sua empresa e teste o TraxUp por 7 dias.</h1>
    <p class="lead">Crie seu ambiente, faça a configuração inicial e avance até o primeiro acesso ao ERP e ao PDV.</p>
    <div class="benefits"><span>✓ ERP liberado</span><span>✓ Configuração guiada</span><span>✓ PDV para teste</span><span>✓ Sem compromisso</span></div>
    <div class="steps"><b>Depois do cadastro</b><ol><li>Seu ambiente é criado</li><li>Você acessa o ERP</li><li>Configura empresa e operação</li><li>Baixa e prepara o PDV</li></ol></div>
   </div>
   <form class="card" *ngIf="!resultado" (ngSubmit)="enviar()">
    <div><p class="eyebrow">COMECE SEU TESTE GRÁTIS</p><h2>Crie seu ambiente TraxUp</h2><p>Leva poucos minutos.</p></div>
    <div class="error" *ngIf="erro">{{erro}}</div>
    <div class="grid">
     <label class="full">Empresa / Razão Social<input name="empresa" [(ngModel)]="m.empresa" required maxlength="200"></label>
     <label>CNPJ/CPF<input name="documento" [(ngModel)]="m.documento" maxlength="18" placeholder="Opcional"></label>
     <label>Segmento<select name="segmento" [(ngModel)]="m.segmento"><option>Comércio</option><option>Serviços</option><option>Alimentação</option><option>Imobiliário</option><option>Outro</option></select></label>
     <label class="full">Nome do responsável<input name="responsavel" [(ngModel)]="m.responsavel" required maxlength="150"></label>
     <label>E-mail<input name="email" type="email" [(ngModel)]="m.email" required maxlength="254"></label>
     <label>WhatsApp / Telefone<input name="telefone" [(ngModel)]="m.telefone" required maxlength="30"></label>
     <label>Quantidade de lojas<input name="quantidadeLojas" type="number" min="1" max="999" [(ngModel)]="m.quantidadeLojas" required></label>
     <label>Crie sua senha<input name="senha" type="password" minlength="12" maxlength="72" [(ngModel)]="m.senha" required placeholder="Mínimo 12 caracteres"></label>
    </div>
    <label class="terms"><input type="checkbox" name="aceiteTermos" [(ngModel)]="m.aceiteTermos" required> Li e aceito os Termos de Uso e a Política de Privacidade.</label>
    <button [disabled]="enviando || !m.aceiteTermos">{{enviando?'Criando seu ambiente...':'Criar minha conta grátis →'}}</button>
    <small>Seus dados serão usados para criar e operar seu ambiente de avaliação.</small>
   </form>
   <section class="card success" *ngIf="resultado">
    <div class="ok">✓</div><p class="eyebrow">AMBIENTE CRIADO</p><h2>Seu trial começou.</h2>
    <p>Seu acesso está liberado por 7 dias. Guarde o código do ambiente para entrar no ERP.</p>
    <div class="tenant"><span>Tenant</span><code>{{resultado.tenantId}}</code></div>
    <div class="next"><b>Próximas etapas</b><span>1. Entrar no ERP com seu e-mail e senha</span><span>2. Revisar os dados da empresa e filial</span><span>3. Configurar operação e fiscal</span><span>4. Baixar e configurar o PDV</span></div>
    <button type="button" (click)="irParaErp()">Ir para o primeiro acesso →</button>
   </section>
  </section>
  <footer>TraxUp · Gestão conectada para vender, controlar e crescer.</footer>
 </main>`,
 styleUrl:'./trial.component.css'
})
export class TrialComponent {
 m={empresa:'',documento:'',responsavel:'',email:'',telefone:'',segmento:'Comércio',quantidadeLojas:1,senha:'',aceiteTermos:false};
 enviando=false; erro=''; resultado?:TrialResponse;
 constructor(private trial:TrialService){}
 enviar(){ if(this.enviando)return; this.enviando=true;this.erro='';this.trial.criar(this.m).subscribe({next:r=>{this.resultado=r;this.enviando=false},error:e=>{this.enviando=false;this.erro=e?.error?.message||e?.error?.detail||'Não foi possível criar o ambiente. Revise os dados e tente novamente.'}})}
 irParaErp(){ if(this.resultado){ localStorage.setItem('traxup_trial_tenant',this.resultado.tenantId); window.location.href='/'; } }
}
