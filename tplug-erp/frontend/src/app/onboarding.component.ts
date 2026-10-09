import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin, switchMap } from 'rxjs';
import { ConfiguracaoInicial, ConfigPdv, OnboardingService, PerfilFiscal, PerfilInicial, TerminalInicial, UsuarioInicial } from './onboarding.service';
import { UiIconComponent } from './ui-icon.component';
@Component({ selector: 'app-onboarding', standalone: true, imports: [CommonModule, FormsModule, UiIconComponent], templateUrl: './onboarding.component.html', styleUrl: './onboarding.component.css' })
export class OnboardingComponent implements OnInit {
 @Output() concluido = new EventEmitter<void>();
 @Output() sair = new EventEmitter<void>();
 dados?: ConfiguracaoInicial;
 etapa = 0; loading = false; error = ''; mensagem = ''; ajuda = false;
 nomeFilial = 'Matriz'; usuarioId = ''; perfilId = ''; terminalId = '';
 usuarios: UsuarioInicial[] = []; perfis: PerfilInicial[] = []; terminais: TerminalInicial[] = [];
 terminal = { codigo: 'PDV-01', nome: 'Caixa principal', serie: 1 };
 fiscal: PerfilFiscal = { regimeTributario: 'SIMPLES_NACIONAL', crt: 1, ambiente: 'HOMOLOGACAO', serieNfe: 1, serieNfce: 1 };
 pdv: ConfigPdv = { exigirJustificativaCancelamento: true, exigirAutorizacaoCancelamento: false, tamanhoImpressao: 'MEDIA', imprimirCaixa: true, imprimirCozinha: false };
 readonly etapas = [
  { titulo: 'Dados da empresa', descricao: 'Dados do trial preenchidos. Revise e complete o que falta.', icon: 'building', obrigatoria: true },
  { titulo: 'Configuração fiscal', descricao: 'Certificado digital, CSC, séries e ambiente', icon: 'file', obrigatoria: true },
  { titulo: 'Usuários e permissões', descricao: 'Equipe, funções e níveis de acesso', icon: 'users', obrigatoria: false },
  { titulo: 'Vendas e PDV', descricao: 'Operação, caixa, formas de pagamento e impressão', icon: 'cart', obrigatoria: false }
 ];
 constructor(private service: OnboardingService) {}
 ngOnInit(): void { this.carregar(); }
 get completas(): boolean[] { return [!!this.dados?.empresa?.empresaRevisada, !!this.dados?.fiscalConfigurado, !!this.dados?.empresa?.equipeRevisada, !!this.dados?.vendasConfiguradas]; }
 get progresso(): number { return this.completas.filter(Boolean).length * 25; }
 carregar(): void {
  this.loading = true; this.error = '';
  this.service.dados().subscribe({ next: dados => { this.dados = dados; this.nomeFilial = dados.filial?.nome || 'Matriz'; this.loading = false; }, error: err => this.falha(err) });
 }
 abrir(etapa: number): void {
  if (this.loading || !this.dados) return;
  this.error = ''; this.mensagem = '';
  if (etapa > 1 && !this.dados.filial) { this.etapa = 1; this.mensagem = 'Revise os dados da empresa antes de configurar as próximas etapas.'; return; }
  this.etapa = etapa;
  if (etapa === 2 && this.dados.fiscalConfigurado) {
   this.loading = true; this.service.fiscal(this.dados.filial!.id).subscribe({ next: f => { this.fiscal = f; this.loading = false; }, error: err => this.falha(err) });
  }
  if (etapa === 3) {
   this.loading = true; forkJoin({ usuarios: this.service.usuarios(), perfis: this.service.perfis() }).subscribe({ next: r => { this.usuarios = r.usuarios; this.perfis = r.perfis; this.loading = false; }, error: err => this.falha(err) });
  }
  if (etapa === 4) {
   this.loading = true; this.service.terminais(this.dados.filial!.id).subscribe({ next: ts => { this.terminais = ts; this.terminalId = ts[0]?.id || ''; this.loading = false; if (this.terminalId) this.carregarPdv(); }, error: err => this.falha(err) });
  }
 }
 voltar(): void { if (!this.loading) this.abrir(this.etapa - 1); }
 iniciar(): void { this.abrir(Math.max(0, this.completas.findIndex(v => !v)) + 1); }
 salvarEmpresa(): void {
  if (!this.dados || this.loading) return;
  this.loading = true; this.error = '';
  this.service.salvarEmpresa({ ...this.dados.empresa, nomeFilial: this.nomeFilial }).subscribe({ next: d => { this.dados = d; this.loading = false; this.abrir(2); }, error: err => this.falha(err) });
 }
 salvarFiscal(): void {
  if (!this.dados?.filial || this.loading) return;
  this.loading = true; this.error = '';
  this.service.salvarFiscal(this.dados.filial.id, this.fiscal).subscribe({ next: () => { this.dados!.fiscalConfigurado = true; this.loading = false; this.abrir(3); }, error: err => this.falha(err) });
 }
 atribuirPerfil(): void {
  if (!this.usuarioId || !this.perfilId || this.loading) return;
  this.loading = true; this.error = '';
  this.service.atribuirPerfil(this.usuarioId, this.perfilId).subscribe({ next: () => { this.loading = false; this.mensagem = 'Perfil atribuído ao usuário selecionado.'; }, error: err => this.falha(err) });
 }
 revisarEquipe(): void {
  if (this.loading || this.usuarios.length === 0) return;
  this.loading = true; this.error = '';
  this.service.revisarEquipe().subscribe({ next: d => { this.dados = d; this.loading = false; this.abrir(4); }, error: err => this.falha(err) });
 }
 carregarPdv(): void {
  if (!this.terminalId) return;
  this.loading = true; this.error = '';
  this.service.pdv(this.terminalId).subscribe({ next: p => { this.pdv = p; this.loading = false; }, error: err => this.falha(err) });
 }
 salvarPdv(): void {
  if (!this.dados?.filial || this.loading) return;
  this.loading = true; this.error = '';
  const salvar = (id: string) => this.service.salvarPdv(id, this.pdv);
  const request = this.terminalId ? salvar(this.terminalId) : this.service.criarTerminal(this.dados.filial.id, this.terminal).pipe(switchMap(t => { this.terminalId = t.id; return salvar(t.id); }));
  request.subscribe({ next: () => { this.dados!.vendasConfiguradas = true; this.loading = false; this.etapa = 0; this.mensagem = 'Configurações salvas.'; }, error: err => this.falha(err) });
 }
 continuarDepois(): void {
  if (this.loading) return;
  this.loading = true; this.error = '';
  this.service.adiar().subscribe({ next: () => { this.loading = false; this.concluido.emit(); }, error: err => this.falha(err) });
 }
 finalizar(): void {
  if (!this.dados || this.loading || this.progresso !== 100) return;
  this.loading = true; this.error = '';
  this.service.concluir(this.nomeFilial, this.dados.empresa.cnpj).subscribe({ next: () => { this.loading = false; this.concluido.emit(); }, error: err => this.falha(err) });
 }
 private falha(err: { status?: number }): void { this.loading = false; this.error = err?.status === 403 ? 'Seu usuário não tem permissão para esta configuração. Solicite acesso ao administrador.' : 'Não foi possível carregar ou salvar a configuração. Tente novamente.'; }
}
