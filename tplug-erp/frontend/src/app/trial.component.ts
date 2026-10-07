import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { TrialResponse, TrialService } from './trial.service';

@Component({
  selector: 'app-trial',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="trial-v2">
      <section class="trial-brand">
        <img src="/assets/traxup-logo.webp" alt="TRAXUP" class="trial-logo">
        <div class="trial-copy">
          <h1>Seu negócio<br><strong>mais simples,<br>mais eficiente.</strong></h1>
          <p>Experimente o TRAXUP por 7 dias<br>e descubra uma nova forma de crescer.</p>
          <ul><li>PDV completo</li><li>Gestão integrada</li><li>Acesso em qualquer lugar</li><li>Seguro e confiável</li></ul>
        </div>
        <div class="trial-preview" aria-hidden="true">
          <img src="/assets/ui-001-dashboard-traxup-erp.webp" alt="">
        </div>
      </section>

      <section class="trial-form-area">
        <section class="signup-card" *ngIf="!resultado">
          <ng-container *ngIf="etapa === 1; else passwordStep">
            <header><h2>Comece seu teste gratuito</h2><p>Preencha os dados abaixo para criar sua conta de 7 dias.</p></header>
            <form #captacaoForm="ngForm" (ngSubmit)="avancar(captacaoForm)" novalidate>
              <div class="fields">
                <input name="empresa" [(ngModel)]="m.empresa" required maxlength="200" placeholder="Nome fantasia">
                <input name="documento" [(ngModel)]="m.documento" required maxlength="18" placeholder="CNPJ">
                <input name="razaoSocial" [(ngModel)]="m.razaoSocial" required maxlength="200" placeholder="Razão social">
                <input name="email" type="email" [(ngModel)]="m.email" required maxlength="254" placeholder="E-mail">
                <input name="telefone" [(ngModel)]="m.telefone" required maxlength="30" placeholder="Telefone / WhatsApp">
                <select name="segmento" [(ngModel)]="m.segmento" required>
                  <option value="" disabled>Segmento do seu negócio</option><option>Comércio</option><option>Serviços</option><option>Alimentação</option><option>Imobiliário</option><option>Outro</option>
                </select>
                <input name="responsavel" [(ngModel)]="m.responsavel" required maxlength="150" placeholder="Nome do responsável">
                <select name="quantidadeLojas" [(ngModel)]="m.quantidadeLojas" required>
                  <option *ngFor="let quantidade of quantidades" [ngValue]="quantidade">{{quantidade}} {{quantidade === 1 ? 'loja' : 'lojas'}}{{quantidade === 10 ? ' ou mais' : ''}}</option>
                </select>
              </div>
              <p class="form-error" *ngIf="erro">{{erro}}</p>
              <button class="primary-action" type="submit">Criar conta de teste</button>
              <p class="legal">Ao se cadastrar você concorda com as <a href="https://institucional.locaweb.com.br/politicas/" target="_blank" rel="noopener noreferrer">Políticas de Privacidade</a> e com os <a href="https://www.connectplug.com.br/termos_de_uso" target="_blank" rel="noopener noreferrer">Termos de uso</a>.</p>
              <div class="reassurance"><span><b>7 dias</b><small>de teste gratuito</small></span><span><b>Sem cartão</b><small>de crédito</small></span><span><b>Acesso rápido</b><small>e sem burocracia</small></span></div>
            </form>
          </ng-container>
          <ng-template #passwordStep>
            <div class="password-step">
              <button class="back" type="button" (click)="etapa = 1">← Voltar</button>
              <h2>Crie sua senha</h2><p>Último passo para ativar seu ambiente TRAXUP.</p>
              <form #senhaForm="ngForm" (ngSubmit)="enviar(senhaForm)">
                <input name="senha" type="password" minlength="12" maxlength="72" [(ngModel)]="m.senha" required placeholder="Senha — mínimo 12 caracteres" autocomplete="new-password">
                <input name="confirmacao" type="password" minlength="12" maxlength="72" [(ngModel)]="confirmacaoSenha" required placeholder="Confirme sua senha" autocomplete="new-password">
                <p class="form-error" *ngIf="erro">{{erro}}</p>
                <button class="primary-action" type="submit" [disabled]="enviando">{{enviando ? 'Criando seu ambiente...' : 'Ativar meu teste grátis'}}</button>
              </form>
            </div>
          </ng-template>
        </section>
        <section class="signup-card success" *ngIf="resultado">
          <div class="success-icon">✓</div><h2>Seu TRAXUP está pronto.</h2><p>Seu teste gratuito de 7 dias começou.</p>
          <button class="primary-action" type="button" (click)="irParaErp()">Continuar para o primeiro acesso</button>
        </section>
      </section>
    </main>
  `,
  styleUrl: './trial.component.css'
})
export class TrialComponent {
  readonly quantidades = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
  etapa = 1;
  confirmacaoSenha = '';
  m = { empresa: '', razaoSocial: '', documento: '', responsavel: '', email: '', telefone: '', segmento: '', quantidadeLojas: 1, senha: '', aceiteTermos: false };
  enviando = false;
  erro = '';
  resultado?: TrialResponse;

  constructor(private trial: TrialService) {}

  avancar(form: NgForm) {
    this.erro = '';
    if (form.invalid) {
      form.control.markAllAsTouched();
      this.erro = 'Preencha os campos obrigatórios para continuar.';
      return;
    }
    this.etapa = 2;
  }

  enviar(form: NgForm) {
    this.erro = '';
    if (form.invalid) {
      form.control.markAllAsTouched();
      this.erro = 'Crie uma senha com pelo menos 12 caracteres.';
      return;
    }
    if (this.m.senha !== this.confirmacaoSenha) {
      this.erro = 'As senhas informadas não coincidem.';
      return;
    }
    if (this.enviando) return;
    this.enviando = true;
    this.trial.criar(this.m).subscribe({
      next: resultado => { this.resultado = resultado; this.enviando = false; },
      error: erro => {
        this.enviando = false;
        this.erro = erro?.error?.message || erro?.error?.detail || 'Não foi possível criar o ambiente. Revise os dados e tente novamente.';
      }
    });
  }

  irParaErp() {
    if (!this.resultado) return;
    localStorage.setItem('traxup_trial_tenant', this.resultado.tenantId);
    window.location.href = '/';
  }
}
