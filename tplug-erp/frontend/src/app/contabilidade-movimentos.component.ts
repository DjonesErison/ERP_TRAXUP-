import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import {
  ContabilidadeService,
  InventarioConsultaResultado,
  InventarioPosicao,
  LivroCaixaLancamento,
  LivroCaixaResultado
} from './contabilidade.service';

@Component({
  selector: 'app-contabilidade-movimentos',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="movement-section">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Movimentação da competência</p>
          <h2>Livro-caixa</h2>
        </div>
        <button class="secondary compact" (click)="carregarLivroCaixa()" [disabled]="loadingLivro">
          {{ loadingLivro ? 'Atualizando...' : 'Atualizar' }}
        </button>
      </div>

      <div class="movement-alert" *ngIf="erroLivro">{{ erroLivro }}</div>

      <ng-container *ngIf="livro as dados">
        <section class="cash-metrics">
          <article class="card cash-card">
            <span>Entradas</span>
            <strong class="positive">{{ dados.totalEntradas | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong>
          </article>
          <article class="card cash-card">
            <span>Saídas</span>
            <strong>{{ dados.totalSaidas | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong>
          </article>
          <article class="card cash-card">
            <span>Saldo</span>
            <strong [class.negative]="dados.saldoPeriodo < 0">{{ dados.saldoPeriodo | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong>
          </article>
          <article class="card cash-card">
            <span>Lançamentos</span>
            <strong>{{ dados.totalDisponivel }}</strong>
          </article>
        </section>

        <section class="card table-panel">
          <div class="table-wrap">
            <table>
              <thead><tr><th>Data</th><th>Conta</th><th>Descrição</th><th>Origem</th><th>Entrada</th><th>Saída</th></tr></thead>
              <tbody>
                <tr *ngFor="let item of dados.lancamentos; trackBy: trackLancamento">
                  <td>{{ item.ocorridoEm | date:'dd/MM/yyyy HH:mm' }}</td>
                  <td><strong>{{ item.contaNome }}</strong><small>{{ item.contaTipo }}</small></td>
                  <td>{{ item.descricao || '—' }}</td>
                  <td>{{ nomeOrigem(item.origemTipo) }}</td>
                  <td class="money positive">{{ item.tipo === 'ENTRADA' ? (item.valor | currency:'BRL':'symbol':'1.2-2':'pt-BR') : '—' }}</td>
                  <td class="money">{{ item.tipo === 'SAIDA' ? (item.valor | currency:'BRL':'symbol':'1.2-2':'pt-BR') : '—' }}</td>
                </tr>
                <tr *ngIf="!loadingLivro && dados.lancamentos.length === 0">
                  <td colspan="6" class="empty">Nenhum lançamento no período.</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pagination">
            <button class="secondary compact" (click)="mudarPaginaLivro(-1)" [disabled]="dados.pagina <= 1 || loadingLivro">Anterior</button>
            <span>Página {{ dados.pagina }} de {{ dados.totalPaginas }}</span>
            <button class="secondary compact" (click)="mudarPaginaLivro(1)" [disabled]="dados.pagina >= dados.totalPaginas || loadingLivro">Próxima</button>
          </div>
        </section>
      </ng-container>
    </section>

    <section class="movement-section">
      <div class="section-heading">
        <div>
          <p class="eyebrow">Posição de estoque</p>
          <h2>Inventários concluídos</h2>
        </div>
        <button class="secondary compact" (click)="carregarInventarios()" [disabled]="loadingInventarios">
          {{ loadingInventarios ? 'Atualizando...' : 'Atualizar' }}
        </button>
      </div>

      <div class="movement-alert" *ngIf="erroInventarios">{{ erroInventarios }}</div>

      <section class="card table-panel" *ngIf="inventarios as dados">
        <div class="scope-result">
          {{ dados.totalDisponivel }} inventário(s) na competência ·
          {{ filialId ? 'filial selecionada' : 'todas as filiais autorizadas' }}
        </div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Inventário</th><th>Conclusão</th><th>Itens</th><th>Divergências</th><th>Ajuste</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of dados.inventarios; trackBy: trackInventario">
                <td><strong>{{ item.descricao }}</strong><small>{{ item.inventarioId | slice:0:8 }}</small></td>
                <td>{{ item.concluidoEm | date:'dd/MM/yyyy HH:mm' }}</td>
                <td>{{ item.totalItens }}</td>
                <td>
                  <span class="status" [class.danger]="item.itensDivergentes > 0" [class.ok]="item.itensDivergentes === 0">
                    {{ item.itensDivergentes }}
                  </span>
                </td>
                <td>
                  <span class="status" [class.ok]="item.ajustadoEm" [class.warning]="!item.ajustadoEm">
                    {{ item.ajustadoEm ? ('Ajustado em ' + (item.ajustadoEm | date:'dd/MM/yyyy')) : 'Pendente' }}
                  </span>
                </td>
              </tr>
              <tr *ngIf="!loadingInventarios && dados.inventarios.length === 0">
                <td colspan="5" class="empty">Nenhum inventário concluído no período.</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination">
          <button class="secondary compact" (click)="mudarPaginaInventario(-1)" [disabled]="dados.pagina <= 1 || loadingInventarios">Anterior</button>
          <span>Página {{ dados.pagina }} de {{ dados.totalPaginas }}</span>
          <button class="secondary compact" (click)="mudarPaginaInventario(1)" [disabled]="dados.pagina >= dados.totalPaginas || loadingInventarios">Próxima</button>
        </div>
      </section>
    </section>
  `,
  styleUrl: './contabilidade-movimentos.component.css'
})
export class ContabilidadeMovimentosComponent implements OnChanges {
  @Input() competencia = '';
  @Input() filialId = '';

  livro?: LivroCaixaResultado;
  inventarios?: InventarioConsultaResultado;
  loadingLivro = false;
  loadingInventarios = false;
  erroLivro = '';
  erroInventarios = '';
  private paginaLivro = 1;
  private paginaInventario = 1;

  constructor(private readonly contabilidade: ContabilidadeService) {}

  ngOnChanges(): void {
    this.paginaLivro = 1;
    this.paginaInventario = 1;
    this.carregarLivroCaixa();
    this.carregarInventarios();
  }

  carregarLivroCaixa(): void {
    if (!this.competencia) return;
    this.loadingLivro = true;
    this.erroLivro = '';
    this.contabilidade.consultarLivroCaixa(
      this.competencia,
      this.filialId || undefined,
      this.paginaLivro
    ).subscribe({
      next: (livro) => {
        this.livro = livro;
        this.loadingLivro = false;
      },
      error: (err) => {
        this.loadingLivro = false;
        this.erroLivro = this.mensagemErro(err, 'Não foi possível carregar o livro-caixa.');
      }
    });
  }

  carregarInventarios(): void {
    if (!this.competencia) return;
    this.loadingInventarios = true;
    this.erroInventarios = '';
    this.contabilidade.consultarInventarios(
      this.competencia,
      this.filialId || undefined,
      this.paginaInventario
    ).subscribe({
      next: (inventarios) => {
        this.inventarios = inventarios;
        this.loadingInventarios = false;
      },
      error: (err) => {
        this.loadingInventarios = false;
        this.erroInventarios = this.mensagemErro(err, 'Não foi possível carregar os inventários.');
      }
    });
  }

  mudarPaginaLivro(delta: number): void {
    const destino = this.paginaLivro + delta;
    if (destino < 1 || destino > Number(this.livro?.totalPaginas || 1)) return;
    this.paginaLivro = destino;
    this.carregarLivroCaixa();
  }

  mudarPaginaInventario(delta: number): void {
    const destino = this.paginaInventario + delta;
    if (destino < 1 || destino > Number(this.inventarios?.totalPaginas || 1)) return;
    this.paginaInventario = destino;
    this.carregarInventarios();
  }

  nomeOrigem(origem?: string | null): string {
    if (!origem) return 'Manual';
    return origem.replaceAll('_', ' ');
  }

  trackLancamento(_: number, item: LivroCaixaLancamento): string { return item.id; }
  trackInventario(_: number, item: InventarioPosicao): string { return item.inventarioId; }

  private mensagemErro(err: { status?: number }, fallback: string): string {
    if (err?.status === 403) return 'Seu perfil não possui acesso à filial selecionada.';
    return fallback;
  }
}
