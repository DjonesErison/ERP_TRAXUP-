import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from './auth.service';
import { CrmService } from './crm.service';
import { ClienteFollowUp, ClienteInativo, ClienteInteracao, ClienteRfv } from './crm.models';
import { InventarioMobileComponent } from './inventario-mobile.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, InventarioMobileComponent],
  template: `
    <div class="login-shell" *ngIf="!autenticado">
      <section class="login-brand">
        <div class="login-mark">T</div>
        <p class="eyebrow light">TPlug ERP</p>
        <h1>Gestão conectada.<br>Decisões mais rápidas.</h1>
        <p>Entre no ambiente da sua empresa para acessar CRM, vendas, compras, estoque e financeiro.</p>
      </section>
      <section class="login-panel">
        <form class="login-card" (ngSubmit)="entrar()">
          <div>
            <p class="eyebrow">Acesso seguro</p>
            <h2>Entrar no TPlug ERP</h2>
            <p class="subtitle">Use o tenant e as credenciais cadastradas no ERP.</p>
          </div>
          <div class="alert" *ngIf="loginError">{{ loginError }}</div>
          <label>
            Tenant
            <input name="tenantId" [(ngModel)]="loginTenantId" required placeholder="UUID do tenant" autocomplete="organization">
          </label>
          <label>
            E-mail
            <input name="email" type="email" [(ngModel)]="loginEmail" required placeholder="usuario@empresa.com.br" autocomplete="username">
          </label>
          <label>
            Senha
            <input name="senha" type="password" [(ngModel)]="loginSenha" required maxlength="72" placeholder="Sua senha" autocomplete="current-password">
          </label>
          <button class="login-button" type="submit" [disabled]="loginLoading || !loginTenantId || !loginEmail || !loginSenha">
            {{ loginLoading ? 'Entrando...' : 'Entrar' }}
          </button>
          <small class="security-note">O tenant é validado pelo backend e incorporado ao JWT. O frontend não define escopo por cabeçalho manual.</small>
        </form>
      </section>
    </div>

    <div class="app-shell" *ngIf="autenticado">
      <aside class="sidebar">
        <div class="brand">
          <div class="brand-mark">T</div>
          <div><strong>TPlug ERP</strong><span>Gestão comercial</span></div>
        </div>
        <nav>
          <a class="active">CRM</a>
          <a>Vendas</a>
          <a>Compras</a>
          <a href="#inventario">Inventário</a>
          <a>Financeiro</a>
        </nav>
        <button class="logout" (click)="sair()" [disabled]="logoutLoading">{{ logoutLoading ? 'Saindo...' : 'Sair' }}</button>
      </aside>

      <main>
        <header class="topbar">
          <div>
            <p class="eyebrow">Fase 5 · CRM</p>
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

        <app-inventario-mobile id="inventario"></app-inventario-mobile>
      </main>
    </div>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  autenticado = false;
  loginTenantId = '';
  loginEmail = '';
  loginSenha = '';
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
        this.autenticado = true;
        this.carregar();
      },
      error: (err) => {
        this.loginLoading = false;
        this.loginError = err?.status === 401 || err?.status === 403
          ? 'Tenant, e-mail ou senha inválidos.'
          : 'Não foi possível entrar. Verifique os dados e a disponibilidade da API.';
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
    this.autenticado = false;
    this.inativos = [];
    this.rfv = [];
    this.followups = [];
    this.interacoes = [];
  }
}
