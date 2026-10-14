import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ContextoOperacionalService } from './contexto-operacional.service';
import { Subscription } from 'rxjs';
import { AuthService } from './auth.service';
import { VendaRecente, VendasService } from './vendas.service';
import { UiIconComponent } from './ui-icon.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, UiIconComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  @Output() abrirVendas = new EventEmitter<MouseEvent>();
  @Output() sessaoExpirada = new EventEmitter<void>();
  readonly hoje = new Date();
  periodo = 0;
  busca = '';
  atualizadoEm?: Date;
  vendas: VendaRecente[] = [];
  totalPedidos: number | null = null;
  loading = false;
  erro = '';
  private request?: Subscription;

  constructor(private readonly service: VendasService, private readonly auth: AuthService, private readonly contexto: ContextoOperacionalService) {}

  ngOnInit(): void { this.carregar(); }
  ngOnDestroy(): void { this.request?.unsubscribe(); }

  carregar(): void {
    if (this.loading) return;
    this.loading = true;
    this.erro = '';
    this.totalPedidos = null; this.vendas = [];
    const fim = new Date(); const inicio = new Date(fim);
    inicio.setUTCDate(inicio.getUTCDate() - Math.max(0, this.periodo - 1));
    const filtros = {filialId: this.contexto.filialAtiva?.id, numero: this.busca.trim(),
      ...(this.periodo ? {inicio: inicio.toISOString().slice(0,10), fim: fim.toISOString().slice(0,10)} : {})};
    this.request = this.service.listarRecentes(0, 5, filtros).subscribe({
      next: pagina => {
        this.vendas = pagina.conteudo;
        this.totalPedidos = pagina.totalRegistros;
        this.atualizadoEm = new Date();
        this.loading = false;
      },
      error: erro => {
        this.loading = false;
        this.totalPedidos = null;
        this.vendas = [];
        if (!this.auth.autenticado) { this.sessaoExpirada.emit(); return; }
        this.erro = erro.status === 403
          ? 'Seu perfil não tem permissão para consultar as vendas.'
          : 'Não foi possível carregar as vendas. Tente novamente.';
      }
    });
  }

  status(venda: VendaRecente): string {
    return ({ RASCUNHO: 'Rascunho', ABERTO: 'Aberto', FATURADO: 'Faturado', CANCELADO: 'Cancelado' })[venda.status] || venda.status;
  }
}
