import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpResponse } from '@angular/common/http';
import {
  ContabilidadeService,
  SpedExportacao
} from './contabilidade.service';

@Component({
  selector: 'app-contabilidade-arquivos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="files-grid">
      <article class="card file-panel">
        <div class="panel-heading">
          <div>
            <p class="eyebrow">Repositório fiscal</p>
            <h2>Pacote de XML</h2>
          </div>
          <span class="status">ZIP + manifesto</span>
        </div>
        <div class="file-body">
          <p>Baixe os XML da competência em partes de até 500 documentos, já limitados às filiais autorizadas.</p>
          <div class="part-control" *ngIf="totalPartesXml > 1">
            <button class="secondary compact" (click)="alterarParte(-1)" [disabled]="parteXml <= 1 || baixandoXml">Anterior</button>
            <strong>Parte {{ parteXml }} de {{ totalPartesXml }}</strong>
            <button class="secondary compact" (click)="alterarParte(1)" [disabled]="parteXml >= totalPartesXml || baixandoXml">Próxima</button>
          </div>
          <button (click)="baixarXml()" [disabled]="baixandoXml || !competencia">
            {{ baixandoXml ? 'Preparando ZIP...' : 'Baixar XML da competência' }}
          </button>
          <small>{{ filialId ? 'Download da filial selecionada.' : 'Download de todas as filiais autorizadas.' }}</small>
        </div>
      </article>

      <article class="card file-panel">
        <div class="panel-heading">
          <div>
            <p class="eyebrow">Obrigações</p>
            <h2>Gerar SPED</h2>
          </div>
          <span class="status">Competência {{ competencia }}</span>
        </div>
        <div class="file-body">
          <p>Solicite o arquivo fiscal da filial e acompanhe o processamento até a liberação do download.</p>
          <label>
            Tipo de arquivo
            <select [(ngModel)]="tipoSped">
              <option value="EFD_ICMS_IPI">EFD ICMS/IPI</option>
              <option value="EFD_CONTRIBUICOES">EFD Contribuições</option>
            </select>
          </label>
          <button (click)="solicitarSped()" [disabled]="solicitandoSped || !filialId || !competencia">
            {{ solicitandoSped ? 'Solicitando...' : 'Solicitar geração do SPED' }}
          </button>
          <small *ngIf="!filialId">Selecione uma filial específica para gerar o SPED.</small>
          <small *ngIf="filialId">A solicitação será vinculada somente à filial selecionada.</small>
        </div>
      </article>
    </section>

    <div class="files-alert" *ngIf="error">{{ error }}</div>
    <div class="files-alert success" *ngIf="feedback">{{ feedback }}</div>

    <section class="card history">
      <div class="panel-heading">
        <div>
          <p class="eyebrow">Histórico da competência</p>
          <h2>Exportações SPED</h2>
        </div>
        <button class="secondary compact" (click)="carregarSpeds()" [disabled]="loadingSped || !filialId">
          {{ loadingSped ? 'Atualizando...' : 'Atualizar' }}
        </button>
      </div>

      <div class="select-branch" *ngIf="!filialId">
        Selecione uma filial para consultar e operar as exportações SPED.
      </div>

      <div class="table-wrap" *ngIf="filialId">
        <table>
          <thead>
            <tr><th>Tipo</th><th>Status</th><th>Atualização</th><th>Retenção</th><th>Ações</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of speds; trackBy: trackSped">
              <td><strong>{{ nomeTipo(item.tipo) }}</strong><small>{{ item.competencia }}</small></td>
              <td><span class="status" [ngClass]="classeStatus(item.status)">{{ nomeStatus(item.status) }}</span></td>
              <td>{{ item.atualizadoEm | date:'dd/MM/yyyy HH:mm' }}</td>
              <td>{{ item.retencaoAte ? (item.retencaoAte | date:'dd/MM/yyyy') : '—' }}</td>
              <td>
                <div class="actions">
                  <button class="compact" *ngIf="item.status === 'CONCLUIDO'" (click)="baixarSped(item)">Baixar</button>
                  <button class="secondary compact" *ngIf="item.status === 'FALHOU'" (click)="reprocessar(item)">Reprocessar</button>
                  <button class="danger compact" *ngIf="item.status === 'PENDENTE' || item.status === 'FALHOU'" (click)="cancelar(item)">Cancelar</button>
                </div>
                <small class="error-code" *ngIf="item.erroCodigo">{{ item.erroCodigo }}</small>
              </td>
            </tr>
            <tr *ngIf="!loadingSped && speds.length === 0">
              <td colspan="5" class="empty">Nenhuma exportação SPED nesta competência e filial.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  `,
  styleUrl: './contabilidade-arquivos.component.css'
})
export class ContabilidadeArquivosComponent implements OnChanges {
  @Input() competencia = '';
  @Input() filialId = '';

  tipoSped = 'EFD_ICMS_IPI';
  parteXml = 1;
  totalPartesXml = 1;
  speds: SpedExportacao[] = [];
  loadingSped = false;
  solicitandoSped = false;
  baixandoXml = false;
  error = '';
  feedback = '';

  constructor(private readonly contabilidade: ContabilidadeService) {}

  ngOnChanges(): void {
    this.parteXml = 1;
    this.totalPartesXml = 1;
    this.feedback = '';
    this.carregarSpeds();
  }

  carregarSpeds(): void {
    if (!this.filialId || !this.competencia) {
      this.speds = [];
      return;
    }
    this.loadingSped = true;
    this.error = '';
    this.contabilidade.listarSped(this.competencia, this.filialId).subscribe({
      next: (speds) => {
        this.speds = speds;
        this.loadingSped = false;
      },
      error: (err) => {
        this.loadingSped = false;
        this.error = this.mensagemErro(err, 'Não foi possível consultar as exportações SPED.');
      }
    });
  }

  baixarXml(): void {
    this.baixandoXml = true;
    this.error = '';
    this.feedback = '';
    this.contabilidade.baixarXml(
      this.competencia,
      this.filialId || undefined,
      this.parteXml
    ).subscribe({
      next: (response) => {
        this.baixandoXml = false;
        this.totalPartesXml = Number(response.headers.get('X-Total-Partes')) || 1;
        this.salvarArquivo(
          response,
          `traxup-xml-${this.competencia}-parte-${this.parteXml}.zip`
        );
        this.feedback = `Pacote XML baixado: parte ${this.parteXml} de ${this.totalPartesXml}.`;
      },
      error: (err) => {
        this.baixandoXml = false;
        this.error = this.mensagemErro(err, 'Não foi possível gerar o pacote de XML.');
      }
    });
  }

  alterarParte(delta: number): void {
    const novaParte = this.parteXml + delta;
    if (novaParte >= 1 && novaParte <= this.totalPartesXml) {
      this.parteXml = novaParte;
    }
  }

  solicitarSped(): void {
    if (!this.filialId || !this.competencia) return;
    this.solicitandoSped = true;
    this.error = '';
    this.feedback = '';
    this.contabilidade.solicitarSped(
      this.filialId,
      this.tipoSped,
      this.competencia
    ).subscribe({
      next: () => {
        this.solicitandoSped = false;
        this.feedback = 'Solicitação SPED registrada e enviada para processamento.';
        this.carregarSpeds();
      },
      error: (err) => {
        this.solicitandoSped = false;
        this.error = this.mensagemErro(err, 'Não foi possível solicitar o SPED.');
      }
    });
  }

  baixarSped(item: SpedExportacao): void {
    this.error = '';
    this.contabilidade.baixarSped(item.id).subscribe({
      next: (response) => {
        this.salvarArquivo(response, `traxup-${item.tipo}-${item.competencia}.txt`);
        this.feedback = 'Arquivo SPED baixado com sucesso.';
      },
      error: (err) => {
        this.error = this.mensagemErro(err, 'Não foi possível baixar o SPED.');
      }
    });
  }

  cancelar(item: SpedExportacao): void {
    this.error = '';
    this.contabilidade.cancelarSped(item.id).subscribe({
      next: () => {
        this.feedback = 'Exportação SPED cancelada.';
        this.carregarSpeds();
      },
      error: (err) => {
        this.error = this.mensagemErro(err, 'Não foi possível cancelar a exportação.');
      }
    });
  }

  reprocessar(item: SpedExportacao): void {
    this.error = '';
    this.contabilidade.reprocessarSped(item.id).subscribe({
      next: () => {
        this.feedback = 'Exportação SPED reenviada para processamento.';
        this.carregarSpeds();
      },
      error: (err) => {
        this.error = this.mensagemErro(err, 'Não foi possível reprocessar a exportação.');
      }
    });
  }

  nomeTipo(tipo: string): string {
    return tipo === 'EFD_CONTRIBUICOES' ? 'EFD Contribuições' : 'EFD ICMS/IPI';
  }

  nomeStatus(status: string): string {
    const nomes: Record<string, string> = {
      PENDENTE: 'Pendente',
      PROCESSANDO: 'Processando',
      CONCLUIDO: 'Concluído',
      FALHOU: 'Falhou',
      CANCELADO: 'Cancelado'
    };
    return nomes[status] ?? status;
  }

  classeStatus(status: string): string {
    if (status === 'CONCLUIDO') return 'ok';
    if (status === 'FALHOU') return 'danger-status';
    if (status === 'PROCESSANDO') return 'processing';
    return '';
  }

  trackSped(_: number, item: SpedExportacao): string {
    return item.id;
  }

  private salvarArquivo(response: HttpResponse<Blob>, fallback: string): void {
    if (!response.body) return;
    const url = URL.createObjectURL(response.body);
    const link = document.createElement('a');
    link.href = url;
    link.download = this.nomeArquivo(response.headers.get('Content-Disposition')) || fallback;
    link.click();
    URL.revokeObjectURL(url);
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
    if (err?.status === 403) return 'Seu perfil não possui permissão para esta operação.';
    if (err?.status === 404) return 'O arquivo solicitado não está disponível.';
    return fallback;
  }
}
