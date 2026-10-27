import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ConfiguracaoInicial, OnboardingService, UnidadeOnboarding } from './onboarding.service';
import { UiIconComponent } from './ui-icon.component';

@Component({ selector: 'app-onboarding', standalone: true, imports: [CommonModule, FormsModule, UiIconComponent], templateUrl: './onboarding.component.html', styleUrl: './onboarding.component.css' })
export class OnboardingComponent implements OnInit {
 @Output() concluido = new EventEmitter<string>();
 @Output() sair = new EventEmitter<void>();
 dados?: ConfiguracaoInicial;
 etapa = 1; loading = false; error = ''; mensagem = ''; unidadeAtual = 0;
 unidades: UnidadeOnboarding[] = [];
 readonly ufs = ['AC','AL','AP','AM','BA','CE','DF','ES','GO','MA','MT','MS','MG','PA','PB','PR','PE','PI','RJ','RN','RS','RO','RR','SC','SP','SE','TO'];
 readonly etapas = ['Empresa','Lojas','Unidade','Revisão','Acesso'];

 constructor(private service: OnboardingService) {}
 ngOnInit(): void { this.carregar(); }
 get progresso(): number { return this.etapa * 20; }
 get atual(): UnidadeOnboarding { return this.unidades[this.unidadeAtual]; }
 get configuradas(): number { return this.unidades.filter(u => this.unidadeValida(u)).length; }
 carregar(): void {
  this.loading = true; this.error = '';
  this.service.dados().subscribe({ next: d => { this.dados = d; this.prepararUnidades(d); this.etapa = d.empresa.empresaRevisada ? (this.configuradas === d.empresa.quantidadeLojas ? 4 : 2) : 1; this.loading = false; }, error: e => this.falha(e) });
 }
 prepararUnidades(d: ConfiguracaoInicial): void {
  const existentes = d.unidades || [];
  this.unidades = Array.from({ length: Math.max(1, d.empresa.quantidadeLojas || 1) }, (_, i) => existentes[i] ? {
   ...existentes[i], cep: existentes[i].cep || (i === 0 ? d.empresa.cep || '' : ''), endereco: existentes[i].endereco || (i === 0 ? d.empresa.endereco || '' : ''),
   numero: existentes[i].numero || (i === 0 ? d.empresa.numero || '' : ''), complemento: existentes[i].complemento || (i === 0 ? d.empresa.complemento || '' : ''),
   bairro: existentes[i].bairro || (i === 0 ? d.empresa.bairro || '' : ''), cidade: existentes[i].cidade || (i === 0 ? d.empresa.cidade || '' : ''), uf: existentes[i].uf || (i === 0 ? d.empresa.uf || '' : '')
  } : ({
   codigo: String(i + 1).padStart(3, '0'), nome: i === 0 ? 'Matriz' : `Filial ${String(i).padStart(2, '0')}`,
   cnpj: i === 0 ? d.empresa.cnpj : '', matriz: i === 0,
   cep: i === 0 ? d.empresa.cep || '' : '', endereco: i === 0 ? d.empresa.endereco || '' : '', numero: i === 0 ? d.empresa.numero || '' : '',
   complemento: i === 0 ? d.empresa.complemento || '' : '', bairro: i === 0 ? d.empresa.bairro || '' : '', cidade: i === 0 ? d.empresa.cidade || '' : '', uf: i === 0 ? d.empresa.uf || '' : '',
   estoqueAtivo: true, pdvAtivo: true, fiscalAtivo: i === 0, configuracaoConcluida: false
  }));
 }
 salvarEmpresa(): void {
  if (!this.dados || this.loading) return; this.loading = true; this.error = '';
  const e = this.dados.empresa;
  this.service.salvarEmpresa({ ...e, nomeFilial: this.unidades[0]?.nome || 'Matriz' }).subscribe({ next: d => { this.dados = d; this.prepararUnidades(d); this.loading = false; this.etapa = 2; }, error: err => this.falha(err) });
 }
 abrirUnidade(index: number): void { this.unidadeAtual = index; this.etapa = 3; this.error = ''; this.mensagem = ''; }
 copiarMatriz(): void {
  if (this.unidadeAtual === 0) return; const m = this.unidades[0]; const u = this.atual;
  Object.assign(u, { estoqueAtivo: m.estoqueAtivo, pdvAtivo: m.pdvAtivo, fiscalAtivo: m.fiscalAtivo });
  this.mensagem = 'Configurações operacionais da matriz copiadas. Os dados fiscais e o endereço foram preservados.';
 }
 salvarUnidade(): void { if (!this.unidadeValida(this.atual)) { this.error = 'Preencha os campos obrigatórios da unidade.'; return; } this.atual.configuracaoConcluida = true; this.error = ''; this.etapa = 2; }
 revisar(): void {
  if (this.configuradas !== this.unidades.length) { this.error = 'Configure todas as unidades antes de revisar.'; return; }
  this.loading = true; this.error = '';
  this.service.salvarUnidades(this.unidades).subscribe({ next: d => { this.dados = d; this.prepararUnidades(d); this.loading = false; this.etapa = 4; }, error: e => this.falha(e) });
 }
 finalizar(): void {
  if (this.loading || !this.unidades.length) return; this.loading = true; this.error = '';
  this.service.concluir(this.unidades[0].nome, this.unidades[0].cnpj).subscribe({ next: () => { this.loading = false; this.etapa = 5; }, error: e => this.falha(e) });
 }
 entrarNoErp(unidade: UnidadeOnboarding): void { if (unidade.id) this.concluido.emit(unidade.id); }
 continuarDepois(): void { if (this.loading) return; this.loading = true; this.service.adiar().subscribe({ next: () => { this.loading = false; this.sair.emit(); }, error: e => this.falha(e) }); }
 unidadeValida(u: UnidadeOnboarding): boolean { return !!u && /^\d{3}$/.test(u.codigo) && !!u.nome.trim() && /^\d{5}-?\d{3}$/.test(u.cep) && !!u.endereco.trim() && !!u.numero.trim() && !!u.bairro.trim() && !!u.cidade.trim() && this.ufs.includes(u.uf); }
 identificar(_: number, u: UnidadeOnboarding): string { return u.id || u.codigo; }
 private falha(err: { status?: number; error?: { message?: string } }): void { this.loading = false; this.error = err?.status === 403 ? 'Seu usuário não tem permissão para esta configuração.' : err?.error?.message || 'Não foi possível carregar ou salvar. Tente novamente.'; }
}
