import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from './auth.service';
import { ContextoOperacionalService, FilialPermitida } from './contexto-operacional.service';
import { CrmService } from './crm.service';
import { ClienteFollowUp, ClienteInativo, ClienteInteracao, ClienteRfv } from './crm.models';
import { InventarioMobileComponent } from './inventario-mobile.component';
import { ContabilidadeComponent } from './contabilidade.component';
import { ConciliacaoFinanceiraComponent } from './conciliacao-financeira.component';
import { VendasComponent } from './vendas.component';
import { ComprasComponent } from './compras.component';
import { DashboardComponent } from './dashboard.component';
import { UiIconComponent } from './ui-icon.component';
import { TrialComponent } from './trial.component';

@Component({ selector: 'app-root', standalone: true, imports: [CommonModule, FormsModule, InventarioMobileComponent, ContabilidadeComponent, ConciliacaoFinanceiraComponent, VendasComponent, ComprasComponent, DashboardComponent, UiIconComponent, TrialComponent], templateUrl: './app.component.html', styleUrl: './app.component.css' })
export class AppComponent implements OnInit {
  area = 'dashboard'; menuRecolhido = false; lembrarAcesso = false; exibindoTrial = false;
  readonly menu = [
    { label: 'Visão Geral', icon: 'home', area: 'dashboard' }, { label: 'Vendas', icon: 'cart', area: 'vendas' }, { label: 'Produtos', icon: 'box', area: '' },
    { label: 'Inventário', icon: 'stock', area: 'inventario' }, { label: 'Clientes', icon: 'users', area: '' }, { label: 'CRM', icon: 'users', area: 'crm' },
    { label: 'Financeiro', icon: 'money', area: 'financeiro' }, { label: 'Fiscal', icon: 'file', area: '' }, { label: 'BI', icon: 'chart', area: '' },
    { label: 'Relatórios', icon: 'chart', area: '' }, { label: 'Assistente IA', icon: 'spark', area: '' }, { label: 'Compras', icon: 'file', area: 'compras' },
    { label: 'Contabilidade', icon: 'file', area: 'contabilidade' }, { label: 'Configurações', icon: 'settings', area: '' }
  ];
  autenticado = false; loginTenantId = ''; loginEmail = ''; loginSenha = ''; mostrarSenha = false; loginLoading = false; loginError = ''; logoutLoading = false;
  recuperacaoAberta = false; recuperacaoTenantId = ''; recuperacaoEmail = ''; recuperacaoLoading = false; recuperacaoMensagem = ''; recuperacaoError = '';
  filialId = ''; filiais: FilialPermitida[] = []; filialAtiva: FilialPermitida | null = null; selecionandoFilial = false; filiaisLoading = false; filiaisError = '';
  diasInatividade = 30; loading = false; error = ''; inativos: ClienteInativo[] = []; rfv: ClienteRfv[] = []; followups: ClienteFollowUp[] = []; interacoes: ClienteInteracao[] = [];

  constructor(private readonly crm: CrmService, private readonly auth: AuthService, private readonly contexto: ContextoOperacionalService) {}

  ngOnInit(): void {
    this.loginTenantId = this.auth.tenantId ?? ''; this.autenticado = this.auth.autenticado; this.filialAtiva = this.contexto.filialAtiva;
    this.selecionandoFilial = this.autenticado && !this.filialAtiva; if (this.selecionandoFilial) this.carregarFiliais();
    try { const salvo = JSON.parse(localStorage.getItem('traxup_login_hint') || 'null'); if (salvo && typeof salvo.tenantId === 'string' && typeof salvo.email === 'string') { this.loginTenantId = this.auth.tenantId || salvo.tenantId; this.loginEmail = salvo.email; this.lembrarAcesso = true; } } catch { localStorage.removeItem('traxup_login_hint'); }
  }

  abrirRecuperacao(): void { this.recuperacaoTenantId = this.loginTenantId.trim(); this.recuperacaoEmail = this.loginEmail.trim(); this.recuperacaoMensagem = ''; this.recuperacaoError = ''; this.recuperacaoAberta = true; }
  fecharRecuperacao(): void { if (!this.recuperacaoLoading) this.recuperacaoAberta = false; }
  @HostListener('document:keydown.escape')
  fecharRecuperacaoComEscape(): void { if (this.recuperacaoAberta) this.fecharRecuperacao(); }
  solicitarRecuperacao(): void {
    if (this.recuperacaoLoading || !this.recuperacaoTenantId.trim() || !this.recuperacaoEmail.trim()) return;
    this.recuperacaoLoading = true; this.recuperacaoError = ''; this.recuperacaoMensagem = '';
    this.auth.solicitarRecuperacao(this.recuperacaoTenantId.trim(), this.recuperacaoEmail.trim()).subscribe({
      next: () => { this.recuperacaoLoading = false; this.recuperacaoMensagem = 'Se os dados estiverem cadastrados, você receberá as instruções para criar uma nova senha.'; },
      error: () => { this.recuperacaoLoading = false; this.recuperacaoError = 'Não foi possível solicitar a recuperação agora. Tente novamente.'; }
    });
  }

  entrar(): void {
    if (this.loginLoading) return; this.loginLoading = true; this.loginError = '';
    this.auth.login({ tenantId: this.loginTenantId.trim(), email: this.loginEmail.trim(), senha: this.loginSenha }).subscribe({
      next: () => { this.loginLoading = false; this.loginSenha = ''; this.mostrarSenha = false; this.area = 'dashboard'; this.autenticado = true; this.contexto.limpar(); this.filialAtiva = null; this.selecionandoFilial = true; this.carregarFiliais(); if (this.lembrarAcesso) localStorage.setItem('traxup_login_hint', JSON.stringify({ tenantId: this.loginTenantId.trim(), email: this.loginEmail.trim() })); else localStorage.removeItem('traxup_login_hint'); },
      error: err => { this.loginLoading = false; this.loginError = err?.status === 401 || err?.status === 403 ? 'Empresa, e-mail ou senha inválidos.' : 'Não foi possível entrar. Verifique os dados e a disponibilidade do sistema.'; }
    });
  }

  carregarFiliais(): void { if (!this.auth.autenticado || this.filiaisLoading) return; this.filiaisLoading = true; this.filiaisError = ''; this.contexto.listarFiliais().subscribe({ next: filiais => { this.filiais = filiais; this.filiaisLoading = false; if (filiais.length === 1) this.escolherFilial(filiais[0]); }, error: () => { this.filiaisLoading = false; this.filiaisError = 'Não foi possível carregar as filiais permitidas para este usuário.'; } }); }
  escolherFilial(filial: FilialPermitida): void { this.contexto.selecionarFilial(filial); this.filialAtiva = filial; this.filialId = filial.id; this.selecionandoFilial = false; this.area = 'dashboard'; }
  trocarFilial(): void { this.selecionandoFilial = true; this.carregarFiliais(); }
  sair(): void { if (this.logoutLoading) return; this.logoutLoading = true; this.auth.logout().subscribe({ next: () => this.finalizarLogout(), error: () => this.finalizarLogout() }); }
  carregar(): void { if (!this.auth.autenticado) { this.autenticado = false; return; } this.loading = true; this.error = ''; const filial = this.filialAtiva?.id || this.filialId.trim() || undefined; this.crm.carregarPainel(filial, this.diasInatividade).subscribe({ next: dados => { this.inativos = dados.inativos; this.rfv = dados.rfv; this.followups = dados.followups; this.interacoes = dados.interacoes; this.loading = false; }, error: () => { this.loading = false; if (!this.auth.autenticado) { this.autenticado = false; this.loginError = 'Sua sessão expirou. Entre novamente.'; return; } this.error = 'Não foi possível carregar o CRM. Verifique a API e os filtros informados.'; } }); }
  navegar(area: string): void { if (!area || this.selecionandoFilial) return; this.area = area; if (area === 'crm') this.carregar(); }
  expirarSessao(): void { this.contexto.limpar(); this.filialAtiva = null; this.autenticado = false; this.loginError = 'Sua sessão expirou. Entre novamente.'; }
  trackId(_: number, item: { id: string }): string { return item.id; } trackCliente(_: number, item: { clienteId: string }): string { return item.clienteId; }
  private finalizarLogout(): void { this.contexto.limpar(); this.filialAtiva = null; this.filiais = []; this.selecionandoFilial = false; this.logoutLoading = false; this.area = 'dashboard'; this.menuRecolhido = false; this.autenticado = false; this.inativos = []; this.rfv = []; this.followups = []; this.interacoes = []; }
}
