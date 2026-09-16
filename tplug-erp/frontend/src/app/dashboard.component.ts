import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { AuthService } from './auth.service';
import { VendaRecente, VendasService } from './vendas.service';
import { UiIconComponent } from './ui-icon.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, UiIconComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  @Output() abrirVendas = new EventEmitter<void>();
  @Output() sessaoExpirada = new EventEmitter<void>();
  readonly hoje = new Date();
  vendas: VendaRecente[] = [];
  totalPedidos: number | null = null;
  loading = false;
  erro = '';
  private request?: Subscription;

  constructor(private readonly service: VendasService, private readonly auth: AuthService) {}

  ngOnInit(): void { this.carregar(); }
  ngOnDestroy(): void { this.request?.unsubscribe(); }

  carregar(): void {
    if (this.loading) return;
    this.loading = true;
    this.erro = '';
    this.request = this.service.listarRecentes(0, 5, {}).subscribe({
      next: pagina => {
        this.vendas = pagina.conteudo;
        this.totalPedidos = pagina.totalRegistros;
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
