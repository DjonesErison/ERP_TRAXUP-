import { CommonModule } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { Component, Input, OnChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  ContabilidadeService,
  SpedExportacao,
  SpedProntidao,
  XmlArquivo
} from './contabilidade.service';

@Component({
  selector: 'app-contabilidade-arquivos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="files-grid">
      <article class="card file-panel">
        <div class="panel-heading">
          <div><p class="eyebrow">Repositório fiscal</p><h2>Pacote de XML</h2></div>
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
          <div><p class="eyebrow">Obrigações</p><h2>Gerar SPED</h2></div>
          <span class="status">Competência {{ competencia }}</span>
        </div>
        <div class="file-body">
          <p>Solicite o arquivo fiscal da filial e acompanhe o processamento até a liberação do download.</p>
          <div class="readiness" *ngIf="prontidao"
               [class.ready]="prontidao.prontoParaProcessar"
               [class.blocked]="!prontidao.prontoParaProcessar">
            <strong>{{ prontidao.prontoParaProcessar ? 'Ambiente pronto' : 'Geração indisponível' }}</strong>
            <span>{{ mensagemProntidao }}</span>
          </div>
          <div class="readiness" *ngIf="loadingProntidao"><span>Verificando ambiente SPED...</span></div>
          <label>
            Tipo de arquivo
            <select [(ngModel)]="tipoSped">
              <option value="EFD_ICMS_IPI">EFD ICMS/IPI</option>
              <option value="EFD_CONTRIBUICOES">EFD Contribuições</option>
            </select>
          </label>
          <button (click)="solicitarSped()"
                  [disabled]="solicitandoSped || !filialId || !competencia || !prontidao?.prontoParaProcessar">
            {{ solicitandoSped ? 'Solicitando...' : 'Solicitar geração do SPED' }}
          </button>
          <small *ngIf="!filialId">Selecione uma filial específica para gerar o SPED.</small>
          <small *ngIf="filialId">A solicitação será vinculada somente à filial selecionada.</small>
        </div>
      </article>
    </section>

    <div class="files-alert" *ngIf="error">{{ error }}</div>
    <div class="files-alert success" *ngIf="feedback">{{ feedback }}</div>

    <section class="card history xml-history">
      <div class="panel-heading">
        <div><p class="eyebrow">Documentos da competência</p><h2>Repositório de XML</h2></div>
        <div class="heading-actions">
          <select [(ngModel)]="statusXml" (change)="carregarXmls()">
            <option value="">Todos os status</option>
            <option value="ARQUIVADO">Arquivados</option>
            <option value="PENDENTE">Pendentes</option>
            <option value="ARQUIVANDO">Arquivando</option>
            <option value="FALHOU">Com falha</option>
          </select>
          <button class="secondary compact" (click)="carregarXmls()" [disabled]="loadingXml">
            {{ loadingXml ? 'Atualizando...' : 'Atualizar' }}
          </button>
        </div>
      </div>
      <div class="scope-result">
        {{ xmls.length }} documento(s) ·
        {{ filialId ? 'filial selecionada' : 'todas as filiais autorizadas' }}
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>Documento</th><th>Tipo</th><th>Status</th><th>Arquivamento</th><th>Retenção</th><th>Ação</th></tr>
          </thead>
          <tbody>
            <tr *ngFor="let item of xmls; trackBy: trackXml">
              <td>
                <strong>{{ item.modelo || 'Documento fiscal' }} {{ item.numero || '—' }}</strong>
                <small>Série {{ item.serie || '—' }}</small>
              </td>
              <td>{{ item.tipo }}</td>
              <td><span class="status" [ngClass]="classeStatusXml(item.status)">{{ nomeStatusXml(item.status) }}</span></td>
              <td>
                {{ item.arquivadoEm ? (item.arquivadoEm | date:'dd/MM/yyyy HH:mm') : '—' }}
                <small *ngIf="item.tentativasEnvio > 0">{{ item.tentativasEnvio }} tentativa(s)</small>
              </td>
              <td>{{ item.retencaoAte ? (item.retencaoAte | date:'dd/MM/yyyy') : '—' }}</td>
              <td>
                <button class="compact" *ngIf="item.downloadDisponivel" (click)="baixarXmlIndividual(item)">Baixar XML</button>
                <small *ngIf="!item.downloadDisponivel">Indisponível</small>
              </td>
            </tr>
            <tr *ngIf="!loadingXml && xmls.length === 0">
              <td colspan="6" class="empty">Nenhum XML encontrado para os filtros.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card history">
      <div class="panel-heading">
        <div><p class="eyebrow">Histórico da competência</p><h2>Exportações SPED</h2></div>
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
  statusXml = '';
  parteXml = 1;
  totalPartesXml = 1;
  speds: SpedExportacao[] = [];
  xmls: XmlArquivo[] = [];
  prontidao?: SpedProntidao;
  loadingSped = false;
  loadingXml = false;
  loadingProntidao = false;
  solicitandoSped = false;
  baixandoXml = false;
  error = '';
  feedback = '';

  constructor(private readonly contabilidade: ContabilidadeService) {}

  ngOnChanges(): void {
    this.parteXml = 1;
    this.totalPartesXml = 1;
    this.feedback = '';
    this.carregarXmls();
    this.carregarSpeds();
    if (!this.prontidao) this.carregarProntidao();
  }

  get mensagemProntidao(): string {
    if (this.prontidao?.prontoParaProcessar) {
      return 'Worker, gerador homologado e repositório estão disponíveis.';
    }
    const mensagens: Record<string, string> = {
      WORKER_DESABILITADO: 'O processamento automático está desabilitado.',
      GERADOR_HOMOLOGADO_NAO_CONFIGURADO: 'O gerador homologado ainda não foi configurado.',
      REPOSITORIO_NAO_CONFIGURADO: 'O repositório de arquivos ainda não foi configurado.',
      HOMOLOGACAO_NAO_CONFIGURADA: 'A homologação fiscal ainda não foi configurada.',
      PROVEDOR_NAO_HOMOLOGADO: 'O provedor configurado não está homologado.'
    };
    const codigo = this.prontidao?.pendenciaCodigo || '';
    return mensagens[codigo] || 'Não foi possível confirmar a prontidão do ambiente.';
  }

  carregarProntidao(): void {
    this.loadingProntidao = true;
    this.contabilidade.consultarProntidaoSped().subscribe({
      next: (prontidao) => {
        this.prontidao = prontidao;
        this.loadingProntidao = false;
      },
      error: (err) => {
        this.loadingProntidao = false;
        this.error = this.mensagemErro(err, 'Não foi possível verificar a prontidão do SPED.');
      }
    });
  }

  carregarXmls(): void {
    if (!this.competencia) {
      this.xmls = [];
      return;
    }
    this.loadingXml = true;
    this.error = '';
    this.contabilidade.listarXml(
      this.competencia,
      this.filialId || undefined,
      this.statusXml || undefined
    ).subscribe({
      next: (resultado) => {
        this.xmls = resultado.itens;
        this.loadingXml = false;
      },
      error: (err) => {
        this.loadingXml = false;
        this.error = this.mensagemErro(err, 'Não foi possível consultar o repositório XML.');
      }
    });
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
    this.contabilidade.baixarXml(this.competencia, this.filialId || undefined, this.parteXml).subscribe({
      next: (response) => {
        this.baixandoXml = false;
        this.totalPartesXml = Number(response.headers.get('X-Total-Partes')) || 1;
        this.salvarArquivo(response, `traxup-xml-${this.competencia}-parte-${this.parteXml}.zip`);
        this.feedback = `Pacote XML baixado: parte ${this.parteXml} de ${this.totalPartesXml}.`;
      },
      error: (err) => {
        this.baixandoXml = false;
        this.error = this.mensagemErro(err, 'Não foi possível gerar o pacote de XML.');
      }
    });
  }

  baixarXmlIndividual(item: XmlArquivo): void {
    this.error = '';
    this.contabilidade.baixarXmlIndividual(item.arquivoId).subscribe({
      next: (response) => {
        this.salvarArquivo(response, `traxup-fiscal-${item.documentoId}.xml`);
        this.feedback = 'XML baixado com sucesso.';
      },
      error: (err) => {
        this.error = this.mensagemErro(err, 'Não foi possível baixar o XML.');
      }
    });
  }

  alterarParte(delta: number): void {
    const novaParte = this.parteXml + delta;
    if (novaParte >= 1 && novaParte <= this.totalPartesXml) this.parteXml = novaParte;
  }

  solicitarSped(): void {
    if (!this.filialId || !this.competencia || !this.prontidao?.prontoParaProcessar) return;
    this.solicitandoSped = true;
    this.error = '';
    this.feedback = '';
    this.contabilidade.solicitarSped(this.filialId, this.tipoSped, this.competencia).subscribe({
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
      error: (err) => this.error = this.mensagemErro(err, 'Não foi possível baixar o SPED.')
    });
  }

  cancelar(item: SpedExportacao): void {
    if (!window.confirm('Cancelar esta exportação SPED?')) return;
    this.error = '';
    this.contabilidade.cancelarSped(item.id).subscribe({
      next: () => {
        this.feedback = 'Exportação SPED cancelada.';
        this.carregarSpeds();
      },
      error: (err) => this.error = this.mensagemErro(err, 'Não foi possível cancelar a exportação.')
    });
  }

  reprocessar(item: SpedExportacao): void {
    this.error = '';
    this.contabilidade.reprocessarSped(item.id).subscribe({
      next: () => {
        this.feedback = 'Exportação SPED reenviada para processamento.';
        this.carregarSpeds();
      },
      error: (err) => this.error = this.mensagemErro(err, 'Não foi possível reprocessar a exportação.')
    });
  }

  nomeTipo(tipo: string): string {
    return tipo === 'EFD_CONTRIBUICOES' ? 'EFD Contribuições' : 'EFD ICMS/IPI';
  }

  nomeStatus(status: string): string {
    const nomes: Record<string, string> = {
      PENDENTE: 'Pendente', PROCESSANDO: 'Processando', CONCLUIDO: 'Concluído',
      FALHOU: 'Falhou', CANCELADO: 'Cancelado'
    };
    return nomes[status] ?? status;
  }

  nomeStatusXml(status: string): string {
    const nomes: Record<string, string> = {
      PENDENTE: 'Pendente', ARQUIVANDO: 'Arquivando',
      ARQUIVADO: 'Arquivado', FALHOU: 'Falhou'
    };
    return nomes[status] ?? status;
  }

  classeStatus(status: string): string {
    if (status === 'CONCLUIDO') return 'ok';
    if (status === 'FALHOU') return 'danger-status';
    if (status === 'PROCESSANDO') return 'processing';
    return '';
  }

  classeStatusXml(status: string): string {
    if (status === 'ARQUIVADO') return 'ok';
    if (status === 'FALHOU') return 'danger-status';
    if (status === 'ARQUIVANDO') return 'processing';
    return '';
  }

  trackSped(_: number, item: SpedExportacao): string { return item.id; }
  trackXml(_: number, item: XmlArquivo): string { return item.arquivoId; }

  private salvarArquivo(response: HttpResponse<Blob>, fallback: string): void {
    if (!response.body) return;
    const url = URL.createObjectURL(response.body);
    const link = document.createElement('a');
    link.href = url;
    link.download = this.nomeArquivo(response.headers.get('Content-Disposition')) || fallback;
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
    if (err?.status === 403) return 'Seu perfil não possui permissão para esta operação.';
    if (err?.status === 404) return 'O arquivo solicitado não está disponível.';
    return fallback;
  }
}
