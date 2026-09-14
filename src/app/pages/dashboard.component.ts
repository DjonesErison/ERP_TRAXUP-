import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PROJECT_FEATURES, progressByModule, progressFor } from '../data/project-progress';

interface BuildInfo {
  commit?: string;
  commitShort?: string;
  branch?: string;
  repository?: string;
  runId?: string;
  runNumber?: string;
  builtAt?: string;
  ci?: string;
  ciRunId?: string;
}

@Component({
  standalone:true, imports:[RouterLink],
  template:`<section class="page">
    <div class="page-head">
      <div><h1>TRAXUP — Status da Implementação</h1><p>Progresso calculado automaticamente a partir do backlog rastreado no GitHub</p></div>
      <a class="primary" routerLink="/roadmap">Ver roadmap →</a>
    </div>

    <div class="kpis">
      @for (k of kpis; track k.label) {
        <div class="card kpi"><span class="kicon">{{k.icon}}</span><small>{{k.label}}</small><strong>{{k.value}}</strong><a [routerLink]="k.link">Ver detalhes →</a></div>
      }
    </div>

    <div class="two">
      <div class="card panel">
        <div class="panel-title"><h2>Progresso automático por módulo</h2><a routerLink="/funcionalidades">Ver backlog</a></div>
        @for (m of modules; track m.name) {
          <div class="bar-row"><span>{{m.name}} <small>({{m.items}} itens)</small></span><div class="track"><i [style.width.%]="m.value"></i></div><b>{{m.value}}%</b></div>
        }
        <p><small>Metodologia: Concluída 100% · Em implementação 75% · Especificada 50% · Planejada 10%. O percentual muda automaticamente quando o status de uma funcionalidade versionada muda.</small></p>
      </div>
      <div class="card panel">
        <h2>Rastreabilidade do build</h2>
        <p><b>Commit publicado:</b> {{buildInfo?.commitShort || 'carregando…'}}</p>
        <p><b>Branch:</b> {{buildInfo?.branch || '—'}} · <b>Build:</b> #{{buildInfo?.runNumber || '—'}}</p>
        <p><b>CI:</b> {{buildInfo?.ci || 'informação não registrada neste build'}}</p>
        <p><b>Gerado em:</b> {{buildInfo?.builtAt || '—'}}</p>
        <p><small>A Central não consulta o repositório privado diretamente do navegador. Os metadados públicos são injetados de forma segura durante o pipeline de build.</small></p>
      </div>
    </div>

    <div class="two bottom">
      <div class="card panel"><div class="panel-title"><h2>Em implementação / próximos marcos</h2><a routerLink="/funcionalidades">Ver funcionalidades</a></div>
        <table><thead><tr><th>Código</th><th>Funcionalidade</th><th>Módulo</th><th>Status</th></tr></thead><tbody>
        @for (x of next; track x.code) {<tr><td>{{x.code}}</td><td><b>{{x.name}}</b></td><td>{{x.module}}</td><td><span class="badge">{{x.status}}</span></td></tr>}
        </tbody></table>
      </div>
      <div class="card panel"><h2>Entregas consolidadas</h2>@for (a of activities; track a) {<div class="activity">✓ <b>{{a}}</b><small>Consolidado</small></div>}</div>
    </div>
  </section>`,
})
export class DashboardComponent implements OnInit {
  buildInfo?: BuildInfo;

  private moduleProgress = progressByModule();
  private globalProgress = progressFor(PROJECT_FEATURES);

  kpis = [
    {icon:'◉',label:'Progresso rastreado',value:`${this.globalProgress}%`,link:'/funcionalidades'},
    {icon:'🧾',label:'Fiscal',value:`${this.progressOf('Fiscal')}%`,link:'/funcionalidades'},
    {icon:'🛒',label:'PDV',value:`${this.progressOf('PDV')}%`,link:'/funcionalidades'},
    {icon:'📦',label:'Produtos',value:`${this.progressOf('Produtos')}%`,link:'/funcionalidades'},
    {icon:'💰',label:'Financeiro',value:`${this.progressOf('Financeiro')}%`,link:'/funcionalidades'},
    {icon:'🏢',label:'SaaS',value:`${this.progressOf('SaaS')}%`,link:'/funcionalidades'}
  ];

  modules = this.moduleProgress;

  next = PROJECT_FEATURES
    .filter(feature => feature.status !== 'Concluída')
    .sort((a, b) => this.priorityOrder(a.priority) - this.priorityOrder(b.priority))
    .slice(0, 6);

  activities = [
    'Central TRAXUP publicada em central.traxup.com.br com deploy automático',
    'Documento Mestre e jornadas do cliente/produto consolidados na documentação',
    'Catálogo visual oficial sincronizado na Central até UI-024',
    'API de Perfil Fiscal por Filial implementada',
    'Backlog de funcionalidades unificado como fonte do cálculo de progresso'
  ];

  ngOnInit(): void {
    fetch('/build-info.json', {cache:'no-store'})
      .then(response => response.ok ? response.json() : Promise.reject())
      .then(info => this.buildInfo = info)
      .catch(() => this.buildInfo = undefined);
  }

  private progressOf(module: string): number {
    return this.moduleProgress.find(item => item.name === module)?.value ?? 0;
  }

  private priorityOrder(priority: string): number {
    return priority === 'Crítica' ? 0 : priority === 'Alta' ? 1 : 2;
  }
}
