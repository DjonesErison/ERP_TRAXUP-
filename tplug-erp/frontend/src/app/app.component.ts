import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from './auth.service';
import { CrmService } from './crm.service';
import { ClienteFollowUp, ClienteInativo, ClienteInteracao, ClienteRfv } from './crm.models';
import { InventarioMobileComponent } from './inventario-mobile.component';
import { ContabilidadeComponent } from './contabilidade.component';
import { ConciliacaoFinanceiraComponent } from './conciliacao-financeira.component';
import { VendasComponent } from './vendas.component';
import { ComprasComponent } from './compras.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, InventarioMobileComponent, ContabilidadeComponent, ConciliacaoFinanceiraComponent, VendasComponent, ComprasComponent],
  template: `
    <div class="login-shell" *ngIf="!autenticado">
      <section class="login-brand" aria-label="TRAXUP ERP">
        <img class="login-logo" src="assets/traxup-logo.webp" alt="TRAXUP — Tecnologia que impulsiona negócios" width="240" height="160">
        <div class="brand-copy">
          <h1>Gestão completa<br>para o seu negócio crescer</h1>
          <p class="brand-description">ERP, estoque, financeiro e fiscal<br>trabalhando juntos.</p>
          <ul class="brand-features">
            <li><span class="feature-icon" aria-hidden="true">▦</span><div><strong>Gestão integrada</strong><span>Todos os processos em um só lugar</span></div></li>
            <li><span class="feature-icon" aria-hidden="true">◇</span><div><strong>Operação segura</strong><span>Um ambiente exclusivo para sua empresa</span></div></li>
            <li><span class="feature-icon" aria-hidden="true">▥</span><div><strong>Informação para decidir</strong><span>Mais clareza para acompanhar seu negócio</span></div></li>
            <li><span class="feature-icon" aria-hidden="true">⌁</span><div><strong>Acesso de qualquer lugar</strong><span>No computador, tablet ou celular</span></div></li>
          </ul>
        </div>
        <div class="growth-art" aria-hidden="true"><i></i><i></i><i></i><span>↗</span></div>
        <p class="brand-footer">MAIS CONTROLE. MAIS RESULTADOS.</p>
      </section>
      <section class="login-panel">
        <div class="login-content">
          <img class="mobile-logo" src="assets/traxup-logo.webp" alt="TRAXUP" width="180" height="120">
          <p class="environment-badge">Ambiente de homologação</p>
          <form class="login-card" (ngSubmit)="entrar()" [attr.aria-busy]="loginLoading">
            <div class="login-heading">
              <h2>Acesse seu ERP</h2>
              <p class="subtitle">Entre com seus dados para acessar o sistema.</p>
            </div>
            <div class="alert" role="alert" *ngIf="loginError">{{ loginError }}</div>
            <label for="login-company">Empresa
              <input id="login-company" name="tenantId" type="text" [(ngModel)]="loginTenantId" required placeholder="Identificador da empresa" autocomplete="organization" aria-describedby="company-help">
            </label>
            <small id="company-help" class="field-help">Use o código de empresa informado pelo administrador.</small>
            <label for="login-email">E-mail
              <input id="login-email" name="email" type="email" [(ngModel)]="loginEmail" required placeholder="Seu e-mail" autocomplete="username">
            </label>
            <label for="login-password">Senha</label>
            <div class="password-field">
              <input id="login-password" name="senha" [type]="mostrarSenha ? 'text' : 'password'" [(ngModel)]="loginSenha" required maxlength="72" placeholder="Sua senha" autocomplete="current-password">
              <button class="password-toggle" type="button" (click)="mostrarSenha = !mostrarSenha" [attr.aria-pressed]="mostrarSenha" [attr.aria-label]="mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'">{{ mostrarSenha ? 'Ocultar' : 'Mostrar' }}</button>
            </div>
            <button class="login-button" type="submit" [disabled]="loginLoading || !loginTenantId.trim() || !loginEmail.trim() || !loginSenha">
              {{ loginLoading ? 'Entrando...' : 'Entrar' }} <span *ngIf="!loginLoading" aria-hidden="true">→</span>
            </button>
            <p class="login-help">Precisa de acesso ou esqueceu sua senha?<br>Fale com o administrador da sua empresa.</p>
            <small class="security-note">Acesso seguro ao ambiente da sua empresa.</small>
          </form>
          <p class="login-footer">TRAXUP ERP · Tecnologia que impulsiona negócios</p>
        </div>
      </section>
    </div>

    <div class="app-shell" *ngIf="autenticado">
      <aside class="sidebar">
        <div class="brand"><img src="assets/traxup-logo.webp" alt="TRAXUP" width="186" height="124"></div>
        <p class="nav-caption">SEU NEGÓCIO</p>
        <nav aria-label="Menu principal">
          <button type="button" class="nav-link" [class.active]="area === 'crm'" [attr.aria-current]="area === 'crm' ? 'page' : null" (click)="area = 'crm'"><span aria-hidden="true">◉</span> CRM</button>
          <button type="button" class="nav-link" [class.active]="area === 'vendas'" [attr.aria-current]="area === 'vendas' ? 'page' : null" (click)="area = 'vendas'"><span aria-hidden="true">↗</span> Vendas</button>
          <button type="button" class="nav-link" [class.active]="area === 'compras'" [attr.aria-current]="area === 'compras' ? 'page' : null" (click)="area = 'compras'"><span aria-hidden="true">▤</span> Compras</button>
          <button type="button" class="nav-link" [class.active]="area === 'inventario'" [attr.aria-current]="area === 'inventario' ? 'page' : null" (click)="area = 'inventario'"><span aria-hidden="true">▦</span> Inventário</button>
          <button type="button" class="nav-link" [class.active]="area === 'financeiro'" [attr.aria-current]="area === 'financeiro' ? 'page' : null" (click)="area = 'financeiro'"><span aria-hidden="true">$</span> Financeiro</button>
          <button type="button" class="nav-link" [class.active]="area === 'contabilidade'" [attr.aria-current]="area === 'contabilidade' ? 'page' : null" (click)="area = 'contabilidade'"><span aria-hidden="true">▥</span> Contabilidade</button>
        </nav>
        <p class="sidebar-footer">Mais controle.<br>Mais resultados.</p>
        <button class="logout" (click)="sair()" [disabled]="logoutLoading">{{ logoutLoading ? 'Saindo...' : 'Sair' }}</button>
      </aside>

      <main>
        <div class="workspace-bar"><strong>TRAXUP <span>ERP</span></strong><span class="environment-badge">Homologação</span></div>
        <app-inventario-mobile *ngIf="area === 'inventario'"></app-inventario-mobile>
        <app-contabilidade *ngIf="area === 'contabilidade'"></app-contabilidade>
        <app-conciliacao-financeira *ngIf="area === 'financeiro'"></app-conciliacao-financeira>
        <app-vendas *ngIf="area === 'vendas'"></app-vendas>
        <app-compras *ngIf="area === 'compras'"></app-compras>
        <ng-container *ngIf="area === 'crm'">
        <header class="topbar">
          <div>
            <p class="eyebrow">Relacionamento com clientes</p>
            <h1>Painel de relacionamento</h1>
            <p class="subtitle">Clientes inativos, agenda, RFV e histórico de interações em uma única visão.</p>
          </div>
          <button (click)="carregar()" [disabled]="loading">{{ loading ? 'Atualizando...' : 'Atualizar' }}</button>
        </header>

        <section class="filters card">
          <label>
            Filial (UUID opcional)
            <input [(ngModel)]="filialId" placeholder="Todas as filiais">
          </label>
          <label>
            Dias sem comprar
            <input type="number" min="1" max="3650" [(ngModel)]="diasInatividade">
          </label>
          <button class="secondary" (click)="carregar()" [disabled]="loading">Aplicar filtros</button>
        </section>

        <div class="alert" *ngIf="error">{{ error }}</div>

        <section class="metrics">
          <article class="metric card"><span>Clientes inativos</span><strong>{{ inativos.length }}</strong><small>lista priorizada por maior inatividade</small></article>
          <article class="metric card"><span>Follow-ups pendentes</span><strong>{{ followups.length }}</strong><small>ações ainda abertas</small></article>
          <article class="metric card"><span>Interações recentes</span><strong>{{ interacoes.length }}</strong><small>últimos registros carregados</small></article>
          <article class="metric card"><span>Clientes no RFV</span><strong>{{ rfv.length }}</strong><small>ranking por valor comprado</small></article>
        </section>

        <section class="grid two-columns">
          <article class="card panel">
            <div class="panel-title"><div><p class="eyebrow">Retorno</p><h2>Clientes inativos</h2></div><span>{{ inativos.length }} registros</span></div>
            <div class="table-wrap">
              <table>
                <thead><tr><th>Cliente</th><th>Última compra</th><th>Compras</th><th>Contato</th></tr></thead>
                <tbody>
                  <tr *ngFor="let item of inativos; trackBy: trackCliente">
                    <td><strong>{{ item.nomeRazaoSocial }}</strong><small>{{ item.nomeFantasia || '—' }}</small></td>
                    <td>{{ item.ultimaCompraEm | date:'dd/MM/yyyy' }}</td>
                    <td>{{ item.quantidadeCompras }}</td>
                    <td>{{ item.telefone || item.email || 'Não informado' }}</td>
                  </tr>
                  <tr *ngIf="!loading && inativos.length === 0"><td colspan="4" class="empty">Nenhum cliente encontrado.</td></tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="card panel">
            <div class="panel-title"><div><p class="eyebrow">Agenda</p><h2>Próximos follow-ups</h2></div><span>{{ followups.length }} pendentes</span></div>
            <div class="stack">
              <div class="followup" *ngFor="let item of followups; trackBy: trackId">
                <div class="status-dot"></div>
                <div><strong>{{ item.assunto }}</strong><span>{{ item.agendadoPara | date:'dd/MM/yyyy HH:mm' }}</span><small>Cliente {{ item.clienteId | slice:0:8 }}</small></div>
              </div>
              <div class="empty" *ngIf="!loading && followups.length === 0">Nenhum follow-up pendente.</div>
            </div>
          </article>
        </section>

        <section class="grid two-columns">
          <article class="card panel">
            <div class="panel-title"><div><p class="eyebrow">RFV</p><h2>Clientes por valor</h2></div><span>Top {{ rfv.length }}</span></div>
            <div class="table-wrap">
              <table>
                <thead><tr><th>Cliente</th><th>Recência</th><th>Frequência</th><th>Valor</th></tr></thead>
                <tbody>
                  <tr *ngFor="let item of rfv; trackBy: trackCliente">
                    <td><strong>{{ item.nomeRazaoSocial }}</strong><small>Ticket {{ item.ticketMedio | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</small></td>
                    <td>{{ item.diasDesdeUltimaCompra }} dias</td>
                    <td>{{ item.quantidadeCompras }}</td>
                    <td>{{ item.valorTotalCompras | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</td>
                  </tr>
                  <tr *ngIf="!loading && rfv.length === 0"><td colspan="4" class="empty">Sem dados RFV para os filtros.</td></tr>
                </tbody>
              </table>
            </div>
          </article>

          <article class="card panel">
            <div class="panel-title"><div><p class="eyebrow">Histórico</p><h2>Interações recentes</h2></div><span>{{ interacoes.length }} registros</span></div>
            <div class="stack">
              <div class="interaction" *ngFor="let item of interacoes; trackBy: trackId">
                <span class="badge">{{ item.canal }}</span>
                <div><strong>{{ item.assunto }}</strong><span>{{ item.resultado.replaceAll('_', ' ') }}</span><small>{{ item.ocorridoEm | date:'dd/MM/yyyy HH:mm' }}</small></div>
              </div>
              <div class="empty" *ngIf="!loading && interacoes.length === 0">Nenhuma interação registrada.</div>
            </div>
          </article>
        </section>

        </ng-container>
      </main>
    </div>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  area: 'crm' | 'contabilidade' | 'financeiro' | 'vendas' | 'compras' | 'inventario' = 'crm';
  autenticado = false;
  loginTenantId = '';
  loginEmail = '';
  loginSenha = '';
  mostrarSenha = false;
  loginLoading = false;
  loginError = '';
  logoutLoading = false;
  filialId = '';
  diasInatividade = 30;
  loading = false;
  error = '';
  inativos: ClienteInativo[] = [];
  rfv: ClienteRfv[] = [];
  followups: ClienteFollowUp[] = [];
  interacoes: ClienteInteracao[] = [];

  constructor(private readonly crm: CrmService, private readonly auth: AuthService) {}

  ngOnInit(): void {
    this.loginTenantId = this.auth.tenantId ?? '';
    this.autenticado = this.auth.autenticado;
    if (this.autenticado) this.carregar();
  }

  entrar(): void {
    if (this.loginLoading) return;
    this.loginLoading = true;
    this.loginError = '';
    this.auth.login({ tenantId: this.loginTenantId.trim(), email: this.loginEmail.trim(), senha: this.loginSenha }).subscribe({
      next: () => {
        this.loginLoading = false;
        this.loginSenha = '';
        this.mostrarSenha = false;
        this.autenticado = true;
        this.carregar();
      },
      error: (err) => {
        this.loginLoading = false;
        this.loginError = err?.status === 401 || err?.status === 403
          ? 'Empresa, e-mail ou senha inválidos.'
          : 'Não foi possível entrar. Verifique os dados e a disponibilidade do sistema.';
      }
    });
  }

  sair(): void {
    if (this.logoutLoading) return;
    this.logoutLoading = true;
    this.auth.logout().subscribe({
      next: () => this.finalizarLogout(),
      error: () => this.finalizarLogout()
    });
  }

  carregar(): void {
    if (!this.auth.autenticado) {
      this.autenticado = false;
      return;
    }
    this.loading = true;
    this.error = '';
    const filial = this.filialId.trim() || undefined;
    this.crm.carregarPainel(filial, this.diasInatividade).subscribe({
      next: (dados) => {
        this.inativos = dados.inativos;
        this.rfv = dados.rfv;
        this.followups = dados.followups;
        this.interacoes = dados.interacoes;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        if (!this.auth.autenticado) {
          this.autenticado = false;
          this.loginError = 'Sua sessão expirou. Entre novamente.';
          return;
        }
        this.error = 'Não foi possível carregar o CRM. Verifique a API e os filtros informados.';
      }
    });
  }

  trackId(_: number, item: { id: string }): string { return item.id; }
  trackCliente(_: number, item: { clienteId: string }): string { return item.clienteId; }

  private finalizarLogout(): void {
    this.logoutLoading = false;
    this.area = 'crm';
    this.autenticado = false;
    this.inativos = [];
    this.rfv = [];
    this.followups = [];
    this.interacoes = [];
  }
}
