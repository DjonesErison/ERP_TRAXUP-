import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CrmService } from './crm.service';
import { ClienteFollowUp, ClienteInativo, ClienteInteracao, ClienteRfv } from './crm.models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <div class="brand-mark">T</div>
          <div><strong>TPlug ERP</strong><span>Gestão comercial</span></div>
        </div>
        <nav>
          <a class="active">CRM</a>
          <a>Vendas</a>
          <a>Compras</a>
          <a>Estoque</a>
          <a>Financeiro</a>
        </nav>
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
      </main>
    </div>
  `,
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  filialId = '';
  diasInatividade = 30;
  loading = false;
  error = '';
  inativos: ClienteInativo[] = [];
  rfv: ClienteRfv[] = [];
  followups: ClienteFollowUp[] = [];
  interacoes: ClienteInteracao[] = [];

  constructor(private readonly crm: CrmService) {}

  ngOnInit(): void { this.carregar(); }

  carregar(): void {
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
      error: (err) => {
        this.loading = false;
        this.error = err?.status === 401
          ? 'Sessão não autenticada. O painel espera um JWT válido em tplug_access_token.'
          : 'Não foi possível carregar o CRM. Verifique a API e os filtros informados.';
      }
    });
  }

  trackId(_: number, item: { id: string }): string { return item.id; }
  trackCliente(_: number, item: { clienteId: string }): string { return item.clienteId; }
}
