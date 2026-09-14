import { CommonModule } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
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
        <div class="heading-actions">
          <button class="secondary compact" (click)="carregarLivroCaixa()" [disabled]="loadingLivro">
            {{ loadingLivro ? 'Atualizando...' : 'Atualizar' }}
          </button>
          <button class="compact" (click)="exportarLivroCaixa()" [disabled]="exportandoLivro || !competencia">
            {{ exportandoLivro ? 'Gerando CSV...' : 'Exportar CSV' }}
          </button>
        </div>
      </div>

      <div class="movement-alert success" *ngIf="feedback">{{ feedback }}</div>
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
        <div class="heading-actions">
          <button class="secondary compact" (click)="carregarInventarios()" [disabled]="loadingInventarios">
            {{ loadingInventarios ? 'Atualizando...' : 'Atualizar' }}
          </button>
          <button class="compact" (click)="exportarInventarios()" [disabled]="exportandoInventarios || !competencia">
            {{ exportandoInventarios ? 'Gerando CSV...' : 'Exportar CSV' }}
          </button>
        </div>
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
  exportandoLivro = false;
  exportandoInventarios = false;
  feedback = '';
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


  exportarLivroCaixa(): void {
    if (!this.competencia) return;
    this.exportandoLivro = true;
    this.feedback = '';
    this.erroLivro = '';
    this.contabilidade.exportarLivroCaixaCsv(
      this.competencia,
      this.filialId || undefined
    ).subscribe({
      next: (response) => {
        this.exportandoLivro = false;
        this.salvarArquivo(
          response,
          `traxup-livro-caixa-${this.competencia}.csv`
        );
        this.feedback = 'Livro-caixa exportado com sucesso.';
      },
      error: (err) => {
        this.exportandoLivro = false;
        this.erroLivro = this.mensagemErro(
          err,
          'Não foi possível exportar o livro-caixa.'
        );
      }
    });
  }

  exportarInventarios(): void {
    if (!this.competencia) return;
    this.exportandoInventarios = true;
    this.feedback = '';
    this.erroInventarios = '';
    this.contabilidade.exportarInventariosCsv(
      this.competencia,
      this.filialId || undefined
    ).subscribe({
      next: (response) => {
        this.exportandoInventarios = false;
        this.salvarArquivo(
          response,
          `traxup-inventarios-${this.competencia}.csv`
        );
        this.feedback = 'Inventários exportados com sucesso.';
      },
      error: (err) => {
        this.exportandoInventarios = false;
        this.erroInventarios = this.mensagemErro(
          err,
          'Não foi possível exportar os inventários.'
        );
      }
    });
  }

  nomeOrigem(origem?: string | null): string {
    if (!origem) return 'Manual';
    return origem.replaceAll('_', ' ');
  }

  trackLancamento(_: number, item: LivroCaixaLancamento): string { return item.id; }
  trackInventario(_: number, item: InventarioPosicao): string { return item.inventarioId; }


  private salvarArquivo(response: HttpResponse<Blob>, fallback: string): void {
    if (!response.body) return;
    const url = URL.createObjectURL(response.body);
    const link = document.createElement('a');
    link.href = url;
    link.download = this.nomeArquivo(
      response.headers.get('Content-Disposition')
    ) || fallback;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  private nomeArquivo(disposition: string | null): string {
    if (!disposition) return '';
    const match = disposition.match(/filename\*?=(?:UTF-8''|")?([^";]+)/i);
    if (!match) return '';
    try {
      return decodeURIComponent(match[1].replace(/"$/, ''));
    } catch {
      return match[1].replace(/"$/, '');
    }
  }

  private mensagemErro(err: { status?: number }, fallback: string): string {
    if (err?.status === 403) return 'Seu perfil não possui acesso à filial selecionada.';
    return fallback;
  }
}
