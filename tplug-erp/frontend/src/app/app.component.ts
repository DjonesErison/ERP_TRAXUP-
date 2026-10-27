import {AdministracaoComponent} from './administracao/administracao.component';
import { ReenviarAtivacaoComponent } from './reenviar-ativacao.component';
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
import { ProdutosComponent } from './produtos.component';
import { DashboardComponent } from './dashboard.component';
import { UiIconComponent } from './ui-icon.component';
import { TrialComponent } from './trial.component';
import { AtivacaoAdminComponent } from './ativacao-admin.component';
import { OnboardingComponent } from './onboarding.component';
import { OnboardingService } from './onboarding.service';

@Component({ selector: 'app-root', standalone: true, imports: [AdministracaoComponent, ReenviarAtivacaoComponent, CommonModule, FormsModule, InventarioMobileComponent, ContabilidadeComponent, ConciliacaoFinanceiraComponent, VendasComponent, ComprasComponent, ProdutosComponent, DashboardComponent, UiIconComponent, TrialComponent, AtivacaoAdminComponent, OnboardingComponent], templateUrl: './app.component.html', styleUrl: './app.component.css' })
export class AppComponent implements OnInit {
  acessoAdministracao = false;
  administracao = window.location.pathname.replace(/\/+$/, '') === '/administracao';
  redefinindoSenha = false;
  readonly rotas: Record<string,string> = {dashboard:'/dashboard',produtos:'/produtos',vendas:'/vendas',compras:'/compras',inventario:'/inventario',financeiro:'/financeiro',contabilidade:'/contabilidade',crm:'/crm',configuracao:'/configuracao'};
  private destinoInicial = 'dashboard';
  area = 'dashboard'; menuRecolhido = false; lembrarAcesso = false; exibindoTrial = false; ativacaoAdminToken = ''; exibindoAtivacao = false;
  readonly menu = [
    { label: 'Visão Geral', icon: 'home', area: 'dashboard' }, { label: 'Vendas', icon: 'cart', area: 'vendas' }, { label: 'Produtos', icon: 'box', area: 'produtos' },
    { label: 'Inventário', icon: 'stock', area: 'inventario' }, { label: 'Clientes', icon: 'users', area: '' }, { label: 'CRM', icon: 'users', area: 'crm' },
    { label: 'Financeiro', icon: 'money', area: 'financeiro' }, { label: 'Fiscal', icon: 'file', area: '' }, { label: 'BI', icon: 'chart', area: '' },
    { label: 'Relatórios', icon: 'chart', area: '' }, { label: 'Assistente IA', icon: 'spark', area: '' }, { label: 'Compras', icon: 'file', area: 'compras' },
    { label: 'Contabilidade', icon: 'file', area: 'contabilidade' }, { label: 'Configurações', icon: 'settings', area: 'configuracao' }
  ];
  autenticado = false; loginCodigoEmpresa = ''; loginEmail = ''; loginSenha = ''; mostrarSenha = false; loginLoading = false; loginError = ''; logoutLoading = false;
  recuperacaoAberta = false; recuperacaoCodigoEmpresa = ''; recuperacaoEmail = ''; recuperacaoLoading = false; recuperacaoMensagem = ''; recuperacaoError = '';
  onboardingPendente = false; reenviarAtivacao = false; ativacaoMensagem = '';
  filialId = ''; filiais: FilialPermitida[] = []; filialAtiva: FilialPermitida | null = null; selecionandoFilial = false; filiaisLoading = false; filiaisError = '';
  filialSelecionada: FilialPermitida | null = null; configuracaoPendenteAberta = false;
  private filialPosOnboardingId = '';
  diasInatividade = 30; loading = false; error = ''; inativos: ClienteInativo[] = []; rfv: ClienteRfv[] = []; followups: ClienteFollowUp[] = []; interacoes: ClienteInteracao[] = [];

  constructor(private readonly crm: CrmService, private readonly auth: AuthService, private readonly contexto: ContextoOperacionalService, private readonly onboarding: OnboardingService) {}

  ngOnInit(): void {
    const publicPath = window.location.pathname.replace(/\/+$/, '');
    this.destinoInicial = this.areaDoCaminho(publicPath);
    this.area = this.destinoInicial;
    // Email links must not reuse credentials from a previous account in this browser.
    const linkParams = new URLSearchParams(window.location.hash.slice(1));
    if ((publicPath === '/ativar' || publicPath === '/recuperar') || (publicPath === '/entrar' && linkParams.has('empresa'))) {
      this.auth.limparSessao(); this.contexto.limpar();
    }
    if ((window.location.hostname === 'captacaoclientes.traxup.com.br' && !['/ativar','/entrar','/recuperar'].includes(publicPath)) || publicPath === '/teste' || publicPath === '/trial') this.exibindoTrial = true;
    this.loginCodigoEmpresa = ''; this.autenticado = this.auth.autenticado;
    if (this.autenticado) this.carregarAcessoAdministracao(); this.filialAtiva = this.contexto.filialAtiva;
    this.selecionandoFilial = this.autenticado && !this.filialAtiva && !this.administracao; if (this.autenticado && !this.administracao) this.verificarOnboarding(); else if (this.selecionandoFilial) this.carregarFiliais();
    try { const salvo = JSON.parse(localStorage.getItem('traxup_login_hint') || 'null'); if (salvo && typeof salvo.codigoEmpresa === 'string' && /^[0-9]{4}$/.test(salvo.codigoEmpresa) && typeof salvo.email === 'string') { this.loginCodigoEmpresa = salvo.codigoEmpresa; this.loginEmail = salvo.email; this.lembrarAcesso = true; } } catch { localStorage.removeItem('traxup_login_hint'); }
    if ((publicPath === '/ativar' || publicPath === '/recuperar') || publicPath === '/entrar') {
      const params = new URLSearchParams(window.location.hash.slice(1));
      const tenantId = params.get('empresa') || ''; const email = params.get('email') || '';
      if (/^[0-9]{4}$/.test(tenantId)) this.loginCodigoEmpresa = tenantId;
      if (params.has('email') && email.length <= 254) this.loginEmail = email;
      if ((publicPath === '/ativar' || publicPath === '/recuperar')) {
        this.redefinindoSenha = publicPath === '/recuperar';
        this.exibindoAtivacao = !this.redefinindoSenha;
        const token = params.get('token') || '';
        if (/^[A-Za-z0-9_-]{43}$/.test(token)) this.ativacaoAdminToken = token;
        else { this.reenviarAtivacao = !this.redefinindoSenha; this.loginError = 'Link incompleto. Solicite um novo link de acesso.'; }
      }
      // Keep credentials out of URLs, referrers and browser history after opening.
      window.history.replaceState(null, '', this.exibindoAtivacao ? '/ativar' : '/entrar');
    }
  }

  carregarAcessoAdministracao(): void { this.auth.acessoAdministracao().subscribe(ok => this.acessoAdministracao = ok); }

  iniciarAtivacaoAdmin(token: string): void { this.exibindoAtivacao = true; this.ativacaoAdminToken = token; this.exibindoTrial = false; }
  concluirAtivacaoAdmin(): void { this.exibindoAtivacao = false; window.history.replaceState(null, '', '/entrar'); this.ativacaoAdminToken = ''; this.exibindoTrial = false; this.loginError = ''; this.ativacaoMensagem = 'Senha criada! Entre para configurar sua empresa.'; }

  abrirRecuperacao(): void { this.recuperacaoCodigoEmpresa = this.loginCodigoEmpresa.trim(); this.recuperacaoEmail = this.loginEmail.trim(); this.recuperacaoMensagem = ''; this.recuperacaoError = ''; this.recuperacaoAberta = true; }
  fecharRecuperacao(): void { if (!this.recuperacaoLoading) this.recuperacaoAberta = false; }
  @HostListener('document:keydown.escape')
  fecharRecuperacaoComEscape(): void { if (this.recuperacaoAberta) this.fecharRecuperacao(); }
  solicitarRecuperacao(): void {
    if (this.recuperacaoLoading || !this.recuperacaoCodigoEmpresa.trim() || !this.recuperacaoEmail.trim()) return;
    this.recuperacaoLoading = true; this.recuperacaoError = ''; this.recuperacaoMensagem = '';
    this.auth.solicitarRecuperacao(this.recuperacaoCodigoEmpresa.trim(), this.recuperacaoEmail.trim()).subscribe({
      next: () => { this.recuperacaoLoading = false; this.recuperacaoMensagem = 'Se os dados estiverem cadastrados, você receberá as instruções para criar uma nova senha.'; },
      error: () => { this.recuperacaoLoading = false; this.recuperacaoError = 'Não foi possível solicitar a recuperação agora. Tente novamente.'; }
    });
  }

  entrar(): void {
    if (this.loginLoading || !/^[0-9]{4}$/.test(this.loginCodigoEmpresa)) return; this.loginLoading = true; this.loginError = '';
    this.auth.login({ codigoEmpresa: this.loginCodigoEmpresa.trim(), email: this.loginEmail.trim(), senha: this.loginSenha }).subscribe({
      next: () => { this.loginLoading = false; this.loginSenha = ''; this.mostrarSenha = false; this.area = 'dashboard'; this.autenticado = true; this.carregarAcessoAdministracao(); this.contexto.limpar(); this.filialAtiva = null; this.selecionandoFilial = !this.administracao; if (!this.administracao) this.verificarOnboarding(); if (this.lembrarAcesso) localStorage.setItem('traxup_login_hint', JSON.stringify({ codigoEmpresa: this.loginCodigoEmpresa.trim(), email: this.loginEmail.trim() })); else localStorage.removeItem('traxup_login_hint'); },
      error: err => { this.loginLoading = false; this.loginError = err?.status === 401 || err?.status === 403 ? 'Empresa, e-mail ou senha inválidos.' : 'Não foi possível entrar. Verifique os dados e a disponibilidade do sistema.'; }
    });
  }

  verificarOnboarding(): void { this.onboarding.status().subscribe({ next: s => { if (!s.empresaConfigurada) { this.auth.limparSessao(); this.contexto.limpar(); this.autenticado = false; this.onboardingPendente = false; this.loginError = 'Esta sessão não possui uma empresa cadastrada. Entre pelo link de acesso do e-mail de confirmação do seu cadastro.'; return; } this.onboardingPendente = !s.concluido || ['/configuracao', '/onboarding'].includes(window.location.pathname); if (this.onboardingPendente) window.history.replaceState(null, '', window.location.pathname === '/configuracao' && s.concluido ? '/configuracao' : '/onboarding'); if (!this.onboardingPendente) this.carregarFiliais(); }, error: () => { this.onboardingPendente = true; window.history.replaceState(null, '', window.location.pathname === '/configuracao' ? '/configuracao' : '/onboarding'); } }); }
  onboardingConcluido(filialId = ''): void { this.destinoInicial = 'dashboard'; this.area = 'dashboard'; this.filialPosOnboardingId = filialId; window.history.replaceState(null, '', '/dashboard'); this.onboardingPendente = false; this.selecionandoFilial = true; this.carregarFiliais(); }

  carregarFiliais(): void { if (!this.auth.autenticado || this.filiaisLoading) return; this.filiaisLoading = true; this.filiaisError = ''; this.contexto.listarFiliais().subscribe({ next: filiais => { this.filiais = filiais; this.filiaisLoading = false; const escolhida = this.filialPosOnboardingId ? filiais.find(f => f.id === this.filialPosOnboardingId) : undefined; this.filialPosOnboardingId = ''; if (escolhida) this.escolherFilial(escolhida); else if (filiais.length === 1) this.escolherFilial(filiais[0]); }, error: () => { this.filiaisLoading = false; this.filiaisError = 'Não foi possível carregar as filiais permitidas para este usuário.'; } }); }
  escolherFilial(filial: FilialPermitida): void {
    this.filialSelecionada = filial;
    if (this.pendenciasDaFilial(filial).length) { this.configuracaoPendenteAberta = true; return; }
    this.confirmarFilial(filial);
  }
  entrarComPendencias(): void { if (this.filialSelecionada) this.confirmarFilial(this.filialSelecionada); }
  completarConfiguracao(): void {
    if (!this.filialSelecionada) return;
    const filial = this.filialSelecionada;
    this.contexto.selecionarFilial(filial); this.filialAtiva = filial; this.filialId = filial.id;
    this.configuracaoPendenteAberta = false; this.selecionandoFilial = false;
    this.destinoInicial = 'configuracao'; this.area = 'configuracao'; this.onboardingPendente = true;
    window.history.pushState(null, '', '/configuracao');
  }
  fecharPendencias(): void { this.configuracaoPendenteAberta = false; this.filialSelecionada = null; }
  pendenciasDaFilial(filial: FilialPermitida): string[] {
    const pendencias: string[] = [];
    if (filial.dadosEmpresaCompletos === false) pendencias.push('Dados da empresa');
    if (filial.fiscalCompleto === false) pendencias.push('Configurações fiscais');
    if (filial.usuariosCompletos === false) pendencias.push('Usuários e permissões');
    if (filial.pdvCompleto === false) pendencias.push('Configuração do PDV');
    return pendencias;
  }
  percentualConfiguracao(filial: FilialPermitida): number { return (4 - this.pendenciasDaFilial(filial).length) * 25; }
  private confirmarFilial(filial: FilialPermitida): void { this.contexto.selecionarFilial(filial); this.filialAtiva = filial; this.filialId = filial.id; this.filialSelecionada = null; this.configuracaoPendenteAberta = false; this.selecionandoFilial = false; this.area = this.destinoInicial; this.atualizarEndereco(this.area, true); if (this.area === 'crm') this.carregar(); }
  trocarFilial(): void { this.filialSelecionada = null; this.configuracaoPendenteAberta = false; this.selecionandoFilial = true; this.carregarFiliais(); }
  sair(): void { if (this.logoutLoading) return; this.logoutLoading = true; this.auth.logout().subscribe({ next: () => this.finalizarLogout(), error: () => this.finalizarLogout() }); }
  carregar(): void { if (!this.auth.autenticado) { this.autenticado = false; return; } this.loading = true; this.error = ''; const filial = this.filialAtiva?.id || this.filialId.trim() || undefined; this.crm.carregarPainel(filial, this.diasInatividade).subscribe({ next: dados => { this.inativos = dados.inativos; this.rfv = dados.rfv; this.followups = dados.followups; this.interacoes = dados.interacoes; this.loading = false; }, error: () => { this.loading = false; if (!this.auth.autenticado) { this.autenticado = false; this.loginError = 'Sua sessão expirou. Entre novamente.'; return; } this.error = 'Não foi possível carregar o CRM. Verifique a API e os filtros informados.'; } }); }
  private areaDoCaminho(caminho: string): string { return Object.keys(this.rotas).find(area => this.rotas[area] === caminho) || 'dashboard'; }
  private atualizarEndereco(area: string, substituir = false): void {
    const caminho = this.rotas[area]; if (!caminho) return;
    if (substituir) window.history.replaceState(null, '', caminho);
    else if (window.location.pathname !== caminho) window.history.pushState(null, '', caminho);
  }
  navegar(area: string, evento?: MouseEvent): void {
    if (evento && (evento.ctrlKey || evento.metaKey || evento.shiftKey || evento.altKey || evento.button !== 0)) return;
    evento?.preventDefault();
    if (!this.rotas[area] || !this.autenticado || this.selecionandoFilial) return;
    this.destinoInicial = area;
    this.atualizarEndereco(area);
    if (area === 'configuracao') { this.onboardingPendente = true; return; }
    this.area = area; if (area === 'crm') this.carregar();
  }
  @HostListener('window:popstate')
  restaurarEndereco(): void {
    if (!this.autenticado) return;
    const caminho = window.location.pathname.replace(/\/+$/, '');
    this.administracao = caminho === '/administracao';
    if (this.administracao) return;
    this.destinoInicial = this.areaDoCaminho(caminho);
    if (['/configuracao', '/onboarding'].includes(caminho) || this.onboardingPendente) { this.verificarOnboarding(); return; }
    this.area = this.destinoInicial;
    if (this.area === 'crm' && !this.selecionandoFilial) this.carregar();
  }
  expirarSessao(): void { this.contexto.limpar(); this.filialAtiva = null; this.autenticado = false; window.history.replaceState(null, '', '/entrar'); this.loginError = 'Sua sessão expirou. Entre novamente.'; }
  trackId(_: number, item: { id: string }): string { return item.id; } trackCliente(_: number, item: { clienteId: string }): string { return item.clienteId; }
  private finalizarLogout(): void { this.acessoAdministracao = false; this.onboardingPendente = false; window.history.replaceState(null, '', this.administracao ? '/administracao' : '/entrar'); this.contexto.limpar(); this.filialAtiva = null; this.filiais = []; this.selecionandoFilial = false; this.logoutLoading = false; this.area = 'dashboard'; this.destinoInicial = 'dashboard'; this.menuRecolhido = false; this.autenticado = false; this.inativos = []; this.rfv = []; this.followups = []; this.interacoes = []; }
}
