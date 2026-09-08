import { CommonModule } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InventarioContagem, InventarioDivergencia, InventarioItemLeitura, InventarioSessao } from './inventario.models';
import { InventarioService } from './inventario.service';

type BarcodeDetectorLike = {
  detect(source: CanvasImageSource): Promise<Array<{ rawValue?: string }>>;
};

type BarcodeDetectorConstructor = new () => BarcodeDetectorLike;

@Component({
  selector: 'app-inventario-mobile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="inventory-shell">
      <header class="inventory-header">
        <div>
          <p class="eyebrow">Fase 6 · Inventário mobile</p>
          <h2>Contagem rápida por código de barras</h2>
          <p>Fluxo operacional separado da Central, consumindo apenas a API tenant-safe do TPlug ERP.</p>
        </div>
        <button type="button" class="secondary" (click)="carregarSessoes()" [disabled]="loading">Atualizar</button>
      </header>

      <div class="alert" *ngIf="erro">{{ erro }}</div>
      <div class="success" *ngIf="mensagem">{{ mensagem }}</div>

      <div class="inventory-grid">
        <article class="card">
          <h3>Sessões</h3>
          <div class="form-row">
            <input [(ngModel)]="filialId" placeholder="UUID da filial">
            <input [(ngModel)]="descricao" placeholder="Descrição opcional">
            <button type="button" (click)="criarSessao()" [disabled]="loading || !filialId.trim()">Abrir inventário</button>
          </div>
          <div class="session-list">
            <button type="button" class="session" *ngFor="let sessao of sessoes" (click)="selecionar(sessao)" [class.selected]="sessaoSelecionada?.id === sessao.id">
              <strong>{{ sessao.descricao || 'Inventário' }}</strong>
              <span>{{ sessao.status }} · {{ sessao.id | slice:0:8 }}</span>
            </button>
            <p class="empty" *ngIf="!loading && sessoes.length === 0">Nenhuma sessão encontrada.</p>
          </div>
        </article>

        <article class="card scan-card">
          <div class="scan-title">
            <div>
              <h3>Leitura e contagem</h3>
              <small *ngIf="sessaoSelecionada">Sessão {{ sessaoSelecionada.id | slice:0:8 }} · {{ sessaoSelecionada.status }}</small>
            </div>
            <span class="status" *ngIf="sessaoSelecionada">{{ contagens.length }} itens</span>
          </div>

          <form (ngSubmit)="localizarItem()" class="scan-form">
            <label>
              Código de barras
              <input name="codigoBarras" [(ngModel)]="codigoBarras" autocomplete="off" inputmode="numeric" placeholder="Leia ou digite o código" [disabled]="!podeContar">
            </label>
            <button type="submit" [disabled]="!podeContar || !codigoBarras.trim() || loading">Localizar</button>
          </form>

          <div class="camera-actions" *ngIf="podeContar">
            <button type="button" class="secondary" *ngIf="cameraSuportada && !cameraAtiva" (click)="iniciarCamera()" [disabled]="loading">Usar câmera</button>
            <button type="button" class="secondary" *ngIf="cameraAtiva" (click)="pararCamera()">Fechar câmera</button>
            <small *ngIf="!cameraSuportada">Leitura por câmera indisponível neste navegador. Digite ou use um leitor físico.</small>
          </div>

          <div class="camera-box" *ngIf="cameraAtiva">
            <video #cameraVideo autoplay playsinline muted></video>
            <span>Aponte a câmera para o código de barras. A imagem é processada localmente no navegador.</span>
          </div>

          <div class="item-card" *ngIf="itemLocalizado">
            <div>
              <span class="badge">{{ itemLocalizado.tipoItem }}</span>
              <h4>{{ itemLocalizado.descricao }}</h4>
              <small>{{ itemLocalizado.codigo }} · {{ itemLocalizado.codigoBarra || 'sem código de barras' }}</small>
            </div>
            <label>
              Quantidade física
              <input type="number" min="0" step="0.0001" [(ngModel)]="quantidadeContada" [ngModelOptions]="{standalone: true}">
            </label>
            <button type="button" (click)="registrarContagem()" [disabled]="loading || quantidadeContada === null || quantidadeContada < 0">Registrar contagem</button>
          </div>

          <div class="actions" *ngIf="sessaoSelecionada">
            <button type="button" class="secondary" (click)="carregarContagens()">Recarregar contagens</button>
            <button type="button" class="warning" *ngIf="sessaoSelecionada.status === 'ABERTO'" (click)="concluir()" [disabled]="loading || contagens.length === 0">Concluir</button>
            <button type="button" class="danger" *ngIf="sessaoSelecionada.status === 'ABERTO'" (click)="cancelar()" [disabled]="loading">Cancelar</button>
            <button type="button" *ngIf="sessaoSelecionada.status === 'CONCLUIDO' && !sessaoSelecionada.ajustadoEm" (click)="ajustarEstoque()" [disabled]="loading">Aplicar ajuste de estoque</button>
          </div>
        </article>
      </div>

      <article class="card" *ngIf="sessaoSelecionada">
        <div class="table-title"><h3>Contagens</h3><button type="button" class="secondary" (click)="carregarDivergencias()">Ver divergências</button></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Tipo</th><th>Item</th><th>Sistema</th><th>Contado</th><th>Divergência</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of contagens">
                <td>{{ item.tipoItem }}</td>
                <td>{{ item.itemId | slice:0:8 }}</td>
                <td>{{ item.quantidadeSistema }}</td>
                <td>{{ item.quantidadeContada }}</td>
                <td [class.negative]="item.divergencia < 0" [class.positive]="item.divergencia > 0">{{ item.divergencia }}</td>
              </tr>
              <tr *ngIf="contagens.length === 0"><td colspan="5" class="empty">Nenhuma contagem registrada.</td></tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="card" *ngIf="divergencias.length > 0">
        <h3>Divergências</h3>
        <div class="divergence-list">
          <div class="divergence" *ngFor="let item of divergencias">
            <div><strong>{{ item.descricaoItem }}</strong><small>{{ item.codigoItem }} · {{ item.tipoItem }}</small></div>
            <span [class.negative]="item.divergencia < 0" [class.positive]="item.divergencia > 0">{{ item.divergencia > 0 ? '+' : '' }}{{ item.divergencia }}</span>
          </div>
        </div>
      </article>
    </section>
  `,
  styles: [`
    .inventory-shell{display:grid;gap:18px;margin-top:28px}.inventory-header,.scan-title,.table-title{display:flex;align-items:center;justify-content:space-between;gap:16px}.inventory-header h2{margin:4px 0 6px}.inventory-header p{margin:0;color:#64748b}.eyebrow{font-size:12px;font-weight:700;letter-spacing:.08em;text-transform:uppercase;color:#475569}.inventory-grid{display:grid;grid-template-columns:minmax(280px,.8fr) minmax(0,1.4fr);gap:18px}.card{background:#fff;border:1px solid #e2e8f0;border-radius:16px;padding:18px;box-shadow:0 8px 24px rgba(15,23,42,.04)}.form-row,.scan-form{display:grid;gap:10px}.form-row input,.scan-form input,.item-card input{width:100%;box-sizing:border-box;border:1px solid #cbd5e1;border-radius:10px;padding:11px 12px;font:inherit}.session-list{display:grid;gap:8px;margin-top:14px;max-height:320px;overflow:auto}.session{display:flex;flex-direction:column;align-items:flex-start;text-align:left;border:1px solid #e2e8f0;background:#f8fafc;border-radius:12px;padding:12px}.session.selected{border-color:#0f172a;background:#f1f5f9}.session span,.item-card small,.scan-title small,.divergence small,.camera-actions small{color:#64748b}.scan-form{grid-template-columns:1fr auto;align-items:end}.scan-form label{display:grid;gap:6px}.camera-actions{display:flex;align-items:center;gap:10px;margin-top:10px}.camera-box{margin-top:10px;display:grid;gap:8px}.camera-box video{width:100%;max-height:300px;object-fit:cover;border-radius:14px;background:#0f172a}.camera-box span{font-size:12px;color:#64748b}.item-card{margin-top:14px;border:1px solid #cbd5e1;border-radius:14px;padding:14px;display:grid;grid-template-columns:1fr minmax(130px,180px) auto;gap:14px;align-items:end}.item-card h4{margin:6px 0}.badge,.status{display:inline-flex;border-radius:999px;background:#e2e8f0;padding:4px 8px;font-size:12px;font-weight:700}.actions{display:flex;flex-wrap:wrap;gap:8px;margin-top:14px}button{border:0;border-radius:10px;background:#0f172a;color:#fff;padding:10px 14px;font-weight:700;cursor:pointer}button:disabled{opacity:.5;cursor:not-allowed}.secondary{background:#e2e8f0;color:#0f172a}.warning{background:#b45309}.danger{background:#b91c1c}.alert,.success{border-radius:12px;padding:12px 14px}.alert{background:#fef2f2;color:#991b1b}.success{background:#f0fdf4;color:#166534}.table-wrap{overflow:auto}table{width:100%;border-collapse:collapse;margin-top:10px}th,td{text-align:left;padding:10px;border-bottom:1px solid #e2e8f0}.empty{color:#64748b;text-align:center}.positive{color:#166534;font-weight:700}.negative{color:#b91c1c;font-weight:700}.divergence-list{display:grid;gap:8px}.divergence{display:flex;justify-content:space-between;align-items:center;padding:12px;border:1px solid #e2e8f0;border-radius:10px}.divergence div{display:flex;flex-direction:column;gap:3px}@media(max-width:820px){.inventory-grid{grid-template-columns:1fr}.inventory-header{align-items:flex-start;flex-direction:column}.scan-form{grid-template-columns:1fr}.item-card{grid-template-columns:1fr}.actions button{flex:1 1 140px}.camera-actions{align-items:flex-start;flex-direction:column}}
  `]
})
export class InventarioMobileComponent implements OnInit, OnDestroy {
  @ViewChild('cameraVideo') cameraVideo?: ElementRef<HTMLVideoElement>;

  sessoes: InventarioSessao[] = [];
  sessaoSelecionada?: InventarioSessao;
  contagens: InventarioContagem[] = [];
  divergencias: InventarioDivergencia[] = [];
  itemLocalizado?: InventarioItemLeitura;
  filialId = '';
  descricao = '';
  codigoBarras = '';
  quantidadeContada: number | null = 1;
  loading = false;
  erro = '';
  mensagem = '';
  cameraAtiva = false;
  readonly cameraSuportada = this.temSuporteCamera();

  private cameraStream?: MediaStream;
  private detector?: BarcodeDetectorLike;
  private detectorTimer?: number;

  constructor(private readonly inventario: InventarioService) {}

  ngOnInit(): void { this.carregarSessoes(); }

  ngOnDestroy(): void { this.pararCamera(); }

  get podeContar(): boolean {
    return !!this.sessaoSelecionada && this.sessaoSelecionada.status === 'ABERTO';
  }

  carregarSessoes(): void {
    this.executar(() => this.inventario.listar(undefined, undefined, 50), sessoes => {
      this.sessoes = sessoes;
      if (this.sessaoSelecionada) {
        this.sessaoSelecionada = sessoes.find(s => s.id === this.sessaoSelecionada?.id) ?? this.sessaoSelecionada;
      }
    });
  }

  criarSessao(): void {
    const filialId = this.filialId.trim();
    if (!filialId) return;
    this.executar(() => this.inventario.criar(filialId, this.descricao), sessao => {
      this.sessoes = [sessao, ...this.sessoes];
      this.selecionar(sessao);
      this.descricao = '';
      this.mensagem = 'Inventário aberto com sucesso.';
    });
  }

  selecionar(sessao: InventarioSessao): void {
    this.pararCamera();
    this.sessaoSelecionada = sessao;
    this.itemLocalizado = undefined;
    this.divergencias = [];
    this.carregarContagens();
  }

  localizarItem(): void {
    if (!this.podeContar || !this.codigoBarras.trim()) return;
    this.executar(() => this.inventario.localizarPorCodigoBarras(this.codigoBarras), item => {
      this.itemLocalizado = item;
      this.quantidadeContada = 1;
      this.mensagem = '';
    });
  }

  async iniciarCamera(): Promise<void> {
    if (!this.podeContar || !this.cameraSuportada || this.cameraAtiva) return;
    this.erro = '';
    try {
      const Detector = this.barcodeDetectorConstructor();
      if (!Detector) return;
      this.detector = new Detector();
      this.cameraStream = await navigator.mediaDevices.getUserMedia({
        audio: false,
        video: { facingMode: { ideal: 'environment' } }
      });
      this.cameraAtiva = true;
      window.setTimeout(() => {
        const video = this.cameraVideo?.nativeElement;
        if (!video || !this.cameraStream) return;
        video.srcObject = this.cameraStream;
        void video.play().then(() => this.agendarDeteccao());
      });
    } catch {
      this.pararCamera();
      this.erro = 'Não foi possível acessar a câmera. Autorize o navegador ou continue com a leitura manual.';
    }
  }

  pararCamera(): void {
    if (this.detectorTimer !== undefined) {
      window.clearTimeout(this.detectorTimer);
      this.detectorTimer = undefined;
    }
    this.cameraStream?.getTracks().forEach(track => track.stop());
    this.cameraStream = undefined;
    this.detector = undefined;
    this.cameraAtiva = false;
    const video = this.cameraVideo?.nativeElement;
    if (video) video.srcObject = null;
  }

  registrarContagem(): void {
    if (!this.sessaoSelecionada || !this.itemLocalizado || this.quantidadeContada === null || this.quantidadeContada < 0) return;
    const item = this.itemLocalizado;
    this.executar(() => this.inventario.registrarContagem(this.sessaoSelecionada!.id, item.tipoItem, item.itemId, this.quantidadeContada!), contagem => {
      const indice = this.contagens.findIndex(c => c.id === contagem.id);
      this.contagens = indice >= 0 ? this.contagens.map(c => c.id === contagem.id ? contagem : c) : [contagem, ...this.contagens];
      this.itemLocalizado = undefined;
      this.codigoBarras = '';
      this.quantidadeContada = 1;
      this.mensagem = 'Contagem registrada.';
    });
  }

  carregarContagens(): void {
    if (!this.sessaoSelecionada) return;
    this.executar(() => this.inventario.listarContagens(this.sessaoSelecionada!.id, 200), itens => this.contagens = itens);
  }

  carregarDivergencias(): void {
    if (!this.sessaoSelecionada) return;
    this.executar(() => this.inventario.listarDivergencias(this.sessaoSelecionada!.id, 200), itens => {
      this.divergencias = itens;
      if (itens.length === 0) this.mensagem = 'Nenhuma divergência encontrada.';
    });
  }

  concluir(): void { this.pararCamera(); this.mudarEstado(() => this.inventario.concluir(this.sessaoSelecionada!.id), 'Inventário concluído para conferência.'); }
  cancelar(): void { this.pararCamera(); this.mudarEstado(() => this.inventario.cancelar(this.sessaoSelecionada!.id), 'Inventário cancelado.'); }
  ajustarEstoque(): void { this.mudarEstado(() => this.inventario.ajustarEstoque(this.sessaoSelecionada!.id), 'Ajuste de estoque aplicado e auditado.'); }

  private agendarDeteccao(): void {
    if (!this.cameraAtiva || !this.detector) return;
    this.detectorTimer = window.setTimeout(() => void this.detectarCodigo(), 300);
  }

  private async detectarCodigo(): Promise<void> {
    const video = this.cameraVideo?.nativeElement;
    if (!this.cameraAtiva || !this.detector || !video || video.readyState < HTMLMediaElement.HAVE_CURRENT_DATA) {
      this.agendarDeteccao();
      return;
    }
    try {
      const codigos = await this.detector.detect(video);
      const codigo = codigos.find(item => item.rawValue?.trim())?.rawValue?.trim();
      if (codigo) {
        this.codigoBarras = codigo;
        this.pararCamera();
        this.localizarItem();
        return;
      }
    } catch {
      // Falhas transitórias de detecção não encerram a câmera; a leitura manual continua disponível.
    }
    this.agendarDeteccao();
  }

  private temSuporteCamera(): boolean {
    return typeof navigator !== 'undefined'
      && !!navigator.mediaDevices?.getUserMedia
      && !!this.barcodeDetectorConstructor();
  }

  private barcodeDetectorConstructor(): BarcodeDetectorConstructor | undefined {
    return (globalThis as typeof globalThis & { BarcodeDetector?: BarcodeDetectorConstructor }).BarcodeDetector;
  }

  private mudarEstado(acao: () => import('rxjs').Observable<InventarioSessao>, mensagem: string): void {
    if (!this.sessaoSelecionada) return;
    this.executar(acao, sessao => {
      this.sessaoSelecionada = sessao;
      this.sessoes = this.sessoes.map(s => s.id === sessao.id ? sessao : s);
      this.mensagem = mensagem;
      this.carregarContagens();
    });
  }

  private executar<T>(acao: () => import('rxjs').Observable<T>, sucesso: (valor: T) => void): void {
    if (this.loading) return;
    this.loading = true;
    this.erro = '';
    this.mensagem = '';
    acao().subscribe({
      next: valor => { this.loading = false; sucesso(valor); },
      error: err => {
        this.loading = false;
        this.erro = err?.error?.message || err?.error?.detail || 'Não foi possível concluir a operação de inventário.';
      }
    });
  }
}
