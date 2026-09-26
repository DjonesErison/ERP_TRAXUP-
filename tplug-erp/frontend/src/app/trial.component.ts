import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { TrialResponse, TrialService } from './trial.service';

@Component({
  selector: 'app-trial',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="trial-page">
      <header class="brand-header">
        <img src="/assets/traxup-logo.webp" alt="TraxUp" class="brand-logo">
      </header>

      <section class="hero">
        <div class="presentation">
          <h1>Transforme a gestão<br>da sua empresa<br>com a <strong>TraxUp</strong></h1>

          <div class="benefits" aria-label="Benefícios do teste">
            <span><i>✓</i> 7 dias grátis</span>
            <span><i>✓</i> Sem compromisso</span>
            <span><i>✓</i> Configuração guiada</span>
          </div>

          <div class="modules" aria-label="Módulos disponíveis">
            <article><b class="module-icon blue">▥</b><div><strong>ERP</strong><small>Gestão completa<br>do seu negócio</small></div></article>
            <article><b class="module-icon green">🛒</b><div><strong>PDV</strong><small>Vendas mais<br>rápidas e seguras</small></div></article>
            <article><b class="module-icon orange">▤</b><div><strong>Fiscal</strong><small>Conformidade<br>e segurança</small></div></article>
            <article><b class="module-icon purple">◉</b><div><strong>Financeiro</strong><small>Mais controle<br>e lucratividade</small></div></article>
            <article><b class="module-icon cyan">♙</b><div><strong>CRM</strong><small>Relacionamento<br>que gera vendas</small></div></article>
            <article><b class="module-icon navy">▟</b><div><strong>BI</strong><small>Decisões baseadas<br>em dados</small></div></article>
          </div>

          <div class="growth-art" aria-hidden="true">
            <span class="bar bar-one"></span><span class="bar bar-two"></span><span class="bar bar-three"></span>
            <span class="arrow">➜</span>
          </div>

          <div class="device-stage">
            <div class="laptop">
              <div class="screen"><img src="/assets/ui-001-dashboard-traxup-erp.webp" alt="Dashboard do TraxUp ERP"></div>
              <div class="base"></div>
            </div>
            <div class="phone"><div class="phone-title">TraxUp PDV</div><div class="phone-search"></div><div class="product" *ngFor="let item of produtos"><span></span><small>{{item}}</small></div><button type="button" tabindex="-1">Finalizar venda</button></div>
          </div>
        </div>

        <section class="signup-card" *ngIf="!resultado">
          <ng-container *ngIf="etapa === 1; else passwordStep">
            <div class="card-heading"><h2>Comece seu teste grátis</h2><p>Tenha acesso à plataforma por 7 dias</p></div>
            <form #captacaoForm="ngForm" (ngSubmit)="avancar(captacaoForm)" novalidate>
              <div class="form-grid">
                <label>Nome completo <em>*</em><input name="responsavel" [(ngModel)]="m.responsavel" required maxlength="150" placeholder="Seu nome completo"></label>
                <label>Nome da empresa <em>*</em><input name="empresa" [(ngModel)]="m.empresa" required maxlength="200" placeholder="Nome da empresa"></label>
                <label>Razão social <em>*</em><input name="razaoSocial" [(ngModel)]="m.razaoSocial" required maxlength="200" placeholder="Razão social da empresa"></label>
                <label>CNPJ/CPF <em>*</em><input name="documento" [(ngModel)]="m.documento" required maxlength="18" placeholder="00.000.000/0000-00"></label>
                <label>Telefone/WhatsApp <em>*</em><input name="telefone" [(ngModel)]="m.telefone" required maxlength="30" placeholder="(11) 96123-4567"></label>
                <label>E-mail profissional <em>*</em><input name="email" type="email" [(ngModel)]="m.email" required maxlength="254" placeholder="seu@empresa.com.br"></label>
                <label>Segmento da empresa <em>*</em><select name="segmento" [(ngModel)]="m.segmento" required><option value="" disabled>Selecione o segmento</option><option>Comércio</option><option>Serviços</option><option>Alimentação</option><option>Imobiliário</option><option>Outro</option></select></label>
                <label>Quantidade de lojas <em>*</em><select name="quantidadeLojas" [(ngModel)]="m.quantidadeLojas" required><option *ngFor="let quantidade of quantidades" [ngValue]="quantidade">{{quantidade}}{{quantidade === 10 ? '+' : ''}}</option></select></label>
              </div>

              <label class="terms"><input type="checkbox" name="aceiteTermos" [(ngModel)]="m.aceiteTermos" required><span>Li e aceito os <a href="/termos" target="_blank">Termos de Uso</a> e a <a href="/privacidade" target="_blank">Política de Privacidade</a>.</span></label>
              <p class="form-error" *ngIf="erro">{{erro}}</p>
              <button class="primary-action" type="submit">Criar minha conta grátis <span>→</span></button>
              <div class="privacy-note"><b>♙</b> Seus dados protegidos conforme a LGPD</div>
            </form>
          </ng-container>

          <ng-template #passwordStep>
            <div class="password-step">
              <button class="back" type="button" (click)="etapa = 1">← Voltar</button>
              <p class="step-label">ÚLTIMO PASSO</p>
              <h2>Proteja seu acesso</h2>
              <p>Crie a senha do administrador para entrar no seu novo ambiente TraxUp.</p>
              <form #senhaForm="ngForm" (ngSubmit)="enviar(senhaForm)">
                <label>Senha <em>*</em><input name="senha" type="password" minlength="12" maxlength="72" [(ngModel)]="m.senha" required placeholder="Mínimo 12 caracteres" autocomplete="new-password"></label>
                <label>Confirme a senha <em>*</em><input name="confirmacao" type="password" minlength="12" maxlength="72" [(ngModel)]="confirmacaoSenha" required autocomplete="new-password"></label>
                <p class="form-error" *ngIf="erro">{{erro}}</p>
                <button class="primary-action" type="submit" [disabled]="enviando">{{enviando ? 'Criando seu ambiente...' : 'Ativar meu teste grátis →'}}</button>
              </form>
            </div>
          </ng-template>
        </section>

        <section class="signup-card success" *ngIf="resultado">
          <div class="success-icon">✓</div><p class="step-label">AMBIENTE CRIADO</p><h2>Seu trial começou.</h2>
          <p>Seu acesso está liberado por sete dias. Entre no ERP com seu e-mail e a senha criada.</p>
          <div class="tenant"><span>Ambiente</span><code>{{resultado.tenantId}}</code></div>
          <div class="next"><b>Próximas etapas</b><span>1. Revisar os dados da empresa e filial</span><span>2. Configurar operação e fiscal</span><span>3. Baixar e configurar o PDV</span></div>
          <button class="primary-action" type="button" (click)="irParaErp()">Ir para o primeiro acesso →</button>
        </section>
      </section>
    </main>
  `,
  styleUrl: './trial.component.css'
})
export class TrialComponent {
  readonly produtos = ['Camiseta', 'Calça Jeans', 'Tênis', 'Boné'];
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
    if (form.invalid || !this.m.aceiteTermos) {
      form.control.markAllAsTouched();
      this.erro = 'Preencha os campos obrigatórios e aceite os termos para continuar.';
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
