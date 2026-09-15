import { CommonModule } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  ChecklistFechamento,
  ContabilidadeService,
  FechamentoMensal,
  FilialContabilidade
} from './contabilidade.service';
import { ContabilidadeArquivosComponent } from './contabilidade-arquivos.component';
import { ContabilidadeMovimentosComponent } from './contabilidade-movimentos.component';

@Component({
  selector: 'app-contabilidade',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ContabilidadeArquivosComponent,
    ContabilidadeMovimentosComponent
  ],
  template: `
    <section class="accounting-page">
      <header class="accounting-head">
        <div>
          <p class="eyebrow">Área da contabilidade</p>
          <h1>Fechamento mensal</h1>
          <p class="subtitle">XML, SPED, livro-caixa e inventário consolidados por empresa e filial.</p>
        </div>
        <button (click)="carregarFechamento()" [disabled]="loading || !competencia">
          {{ loading ? 'Atualizando...' : 'Atualizar fechamento' }}
        </button>
      </header>

      <section class="accounting-filters card">
        <label>
          Competência
          <input type="month" [(ngModel)]="competencia" (change)="carregarFechamento()">
        </label>
        <label>
          Empresa / filial
          <select [(ngModel)]="filialId" (change)="carregarFechamento()">
            <option value="">Todas as filiais autorizadas</option>
            <option *ngFor="let filial of filiais; trackBy: trackFilial" [value]="filial.id">
              {{ filial.empresaNome }} — {{ filial.nome }}
            </option>
          </select>
        </label>
        <div class="scope-note">
          <strong>Escopo protegido</strong>
          <span>{{ descricaoEscopo }}</span>
        </div>
      </section>

      <div class="accounting-alert warning" *ngIf="!loadingFiliais && filiais.length === 0 && !error">
        Nenhuma filial foi liberada para este usuário. Solicite o vínculo ao administrador.
      </div>
      <div class="accounting-alert" *ngIf="error">{{ error }}</div>

      <section class="accounting-loading card" *ngIf="loading && !resumo">
        Carregando dados da competência...
      </section>


      <section class="checklist card" *ngIf="checklist as lista">
        <div class="checklist-head">
          <div>
            <p class="eyebrow">Conferência da competência</p>
            <h2>Checklist do fechamento</h2>
            <p>{{ mensagemChecklist }}</p>
          </div>
          <span class="checklist-status" [ngClass]="classeChecklist(lista.statusGeral)">
            {{ nomeStatusChecklist(lista.statusGeral) }}
          </span>
        </div>
        <div class="checklist-items">
          <article *ngFor="let item of lista.itens">
            <span class="item-state" [ngClass]="classeChecklist(item.status)"></span>
            <div>
              <strong>{{ item.titulo }}</strong>
              <small>{{ item.mensagem }}</small>
            </div>
            <span class="item-total">{{ item.total }}</span>
          </article>
        </div>
        <div class="checklist-foot" [class.available]="lista.podeGerarPacote">
          <div>
            <strong>{{ lista.podeGerarPacote ? 'Competência elegível para o pacote contábil' : 'Pacote bloqueado por pendências' }}</strong>
            <span>{{ lista.totalPendencias }} pendência(s) identificada(s)</span>
            <small *ngIf="lista.podeGerarPacote && !filialId">Selecione uma filial para gerar o pacote consolidado.</small>
            <small class="package-feedback" *ngIf="pacoteFeedback">{{ pacoteFeedback }}</small>
          </div>
          <button
            class="package-button"
            *ngIf="lista.podeGerarPacote"
            (click)="baixarPacote()"
            [disabled]="gerandoPacote || !filialId">
            {{ gerandoPacote ? 'Gerando pacote...' : 'Baixar pacote contábil' }}
          </button>
        </div>
      </section>

      <ng-container *ngIf="resumo as dados">
        <section class="accounting-metrics">
          <article class="card accounting-metric">
            <span>XML arquivados</span>
            <strong>{{ dados.xml.arquivados | number:'1.0-0':'pt-BR' }}</strong>
            <small>{{ dados.xml.total }} documentos · {{ dados.xml.pendentes }} pendentes</small>
          </article>
          <article class="card accounting-metric">
            <span>SPED concluídos</span>
            <strong>{{ dados.sped.concluidos | number:'1.0-0':'pt-BR' }}</strong>
            <small>{{ dados.sped.total }} solicitações · {{ dados.sped.pendentes }} pendentes</small>
          </article>
          <article class="card accounting-metric">
            <span>Saldo do livro-caixa</span>
            <strong [class.negative]="saldoLivroCaixa < 0">
              {{ saldoLivroCaixa | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}
            </strong>
            <small>{{ dados.livroCaixa.lancamentos }} lançamentos</small>
          </article>
          <article class="card accounting-metric">
            <span>Inventários concluídos</span>
            <strong>{{ dados.inventario.concluidos | number:'1.0-0':'pt-BR' }}</strong>
            <small>{{ dados.inventario.comDivergencias }} com divergências</small>
          </article>
        </section>

        <section class="accounting-grid">
          <article class="card accounting-panel">
            <div class="panel-heading">
              <div><p class="eyebrow">Documentos fiscais</p><h2>XML e SPED</h2></div>
              <span class="status ok" *ngIf="dados.xml.falhas + dados.sped.falhas === 0">Sem falhas</span>
              <span class="status danger" *ngIf="dados.xml.falhas + dados.sped.falhas > 0">
                {{ dados.xml.falhas + dados.sped.falhas }} falhas
              </span>
            </div>
            <div class="summary-row"><span>XML arquivados</span><strong>{{ dados.xml.arquivados }}</strong></div>
            <div class="summary-row"><span>XML aguardando arquivamento</span><strong>{{ dados.xml.pendentes }}</strong></div>
            <div class="summary-row"><span>SPED concluídos</span><strong>{{ dados.sped.concluidos }}</strong></div>
            <div class="summary-row"><span>SPED em processamento</span><strong>{{ dados.sped.pendentes }}</strong></div>
            <div class="summary-row"><span>SPED cancelados</span><strong>{{ dados.sped.cancelados }}</strong></div>
          </article>

          <article class="card accounting-panel">
            <div class="panel-heading">
              <div><p class="eyebrow">Movimentação</p><h2>Livro-caixa e inventário</h2></div>
              <span class="status">Competência {{ competencia }}</span>
            </div>
            <div class="summary-row"><span>Entradas</span><strong class="positive">{{ dados.livroCaixa.entradas | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></div>
            <div class="summary-row"><span>Saídas</span><strong>{{ dados.livroCaixa.saidas | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></div>
            <div class="summary-row total"><span>Saldo do período</span><strong [class.negative]="saldoLivroCaixa < 0">{{ saldoLivroCaixa | currency:'BRL':'symbol':'1.2-2':'pt-BR' }}</strong></div>
            <div class="summary-row"><span>Inventários ajustados</span><strong>{{ dados.inventario.ajustados }}</strong></div>
            <div class="summary-row"><span>Inventários com divergência</span><strong>{{ dados.inventario.comDivergencias }}</strong></div>
          </article>
        </section>
      </ng-container>

      <app-contabilidade-movimentos
        [competencia]="competencia"
        [filialId]="filialId">
      </app-contabilidade-movimentos>

      <app-contabilidade-arquivos
        [competencia]="competencia"
        [filialId]="filialId">
      </app-contabilidade-arquivos>
    </section>
  `,
  styleUrl: './contabilidade.component.css'
})
export class ContabilidadeComponent implements OnInit {
  competencia = this.competenciaAtual();
  filialId = '';
  filiais: FilialContabilidade[] = [];
  resumo?: FechamentoMensal;
  checklist?: ChecklistFechamento;
  loadingFiliais = false;
  loading = false;
  gerandoPacote = false;
  pacoteFeedback = '';
  error = '';

  constructor(private readonly contabilidade: ContabilidadeService) {}

  ngOnInit(): void {
    this.carregarFiliais();
  }

  get saldoLivroCaixa(): number {
    const caixa = this.resumo?.livroCaixa;
    return caixa ? Number(caixa.entradas) - Number(caixa.saidas) : 0;
  }

  get descricaoEscopo(): string {
    if (!this.filialId) return 'Somente as filiais autorizadas no seu perfil';
    const filial = this.filiais.find(item => item.id === this.filialId);
    return filial ? `${filial.empresaNome} · ${filial.nome}` : 'Filial selecionada';
  }

  carregarFiliais(): void {
    this.loadingFiliais = true;
    this.error = '';
    this.contabilidade.listarFiliais().subscribe({
      next: (filiais) => {
        this.filiais = filiais;
        this.loadingFiliais = false;
        this.carregarFechamento();
      },
      error: (err) => {
        this.loadingFiliais = false;
        this.error = err?.status === 403
          ? 'Seu perfil não possui acesso à área contábil.'
          : 'Não foi possível carregar as filiais autorizadas.';
      }
    });
  }

  carregarFechamento(): void {
    if (!this.competencia || this.loadingFiliais) return;
    this.loading = true;
    this.pacoteFeedback = '';
    this.error = '';
    const filial = this.filialId || undefined;
    forkJoin({
      resumo: this.contabilidade.consultarFechamento(
        this.competencia,
        filial
      ),
      checklist: this.contabilidade.consultarChecklist(
        this.competencia,
        filial
      )
    }).subscribe({
      next: (dados) => {
        this.resumo = dados.resumo;
        this.checklist = dados.checklist;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.status === 403
          ? 'Você não possui acesso à filial selecionada.'
          : 'Não foi possível carregar o fechamento desta competência.';
      }
    });
  }


  baixarPacote(): void {
    if (!this.filialId || !this.competencia || !this.checklist?.podeGerarPacote) return;
    this.gerandoPacote = true;
    this.pacoteFeedback = '';
    this.contabilidade.baixarPacoteMensal(
      this.competencia,
      this.filialId
    ).subscribe({
      next: (response) => {
        this.gerandoPacote = false;
        this.salvarArquivo(
          response,
          `traxup-pacote-contabil-${this.competencia}.zip`
        );
        this.pacoteFeedback = 'Pacote contábil gerado com sucesso.';
      },
      error: (err) => {
        this.gerandoPacote = false;
        this.pacoteFeedback = err?.status === 403
          ? 'Seu perfil não reúne todas as permissões necessárias.'
          : 'Não foi possível gerar o pacote. Atualize o checklist e tente novamente.';
      }
    });
  }

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
    const match = disposition.match(/filename\\*?=(?:UTF-8''|")?([^";]+)/i);
    if (!match) return '';
    try {
      return decodeURIComponent(match[1].replace(/"$/, ''));
    } catch {
      return match[1].replace(/"$/, '');
    }
  }

  get mensagemChecklist(): string {
    const status = this.checklist?.statusGeral;
    if (status === 'PRONTO') return 'Todos os itens da competência foram conferidos.';
    if (status === 'ATENCAO') return 'A competência pode seguir, mas contém pontos para revisão.';
    if (status === 'PENDENTE') return 'Existem arquivos ainda em processamento.';
    return 'Existem falhas que impedem a geração do pacote contábil.';
  }

  nomeStatusChecklist(status: string): string {
    const nomes: Record<string, string> = {
      PRONTO: 'Pronto',
      ATENCAO: 'Atenção',
      PENDENTE: 'Pendente',
      FALHA: 'Falha',
      BLOQUEADO: 'Bloqueado'
    };
    return nomes[status] ?? status;
  }

  classeChecklist(status: string): string {
    if (status === 'PRONTO') return 'ready';
    if (status === 'ATENCAO') return 'attention';
    if (status === 'PENDENTE') return 'pending';
    return 'blocked';
  }

  trackFilial(_: number, filial: FilialContabilidade): string {
    return filial.id;
  }

  private competenciaAtual(): string {
    const agora = new Date();
    return `${agora.getFullYear()}-${String(agora.getMonth() + 1).padStart(2, '0')}`;
  }
}
