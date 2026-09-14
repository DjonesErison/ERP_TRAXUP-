import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PROJECT_FEATURES, progressByKind, progressByModule, progressFor } from '../data/project-progress';

interface BuildInfo { commit?:string; commitShort?:string; branch?:string; repository?:string; runId?:string; runNumber?:string; builtAt?:string; ci?:string; ciRunId?:string; }

@Component({
  standalone:true, imports:[RouterLink],
  template:`<section class="page">
    <div class="page-head"><div><h1>TRAXUP — Status da Implementação</h1><p>Progresso calculado a partir do backlog funcional e das frentes estruturais versionadas</p></div><a class="primary" routerLink="/roadmap">Ver roadmap →</a></div>
    <div class="kpis">@for(k of kpis;track k.label){<div class="card kpi"><span class="kicon">{{k.icon}}</span><small>{{k.label}}</small><strong>{{k.value}}</strong><a [routerLink]="k.link">Ver detalhes →</a></div>}</div>
    <div class="two">
      <div class="card panel"><div class="panel-title"><h2>Visão global automática</h2><a routerLink="/funcionalidades">Ver backlog</a></div>
        @for(k of kinds;track k.name){<div class="bar-row"><span>{{k.name}} <small>({{k.items}} itens)</small></span><div class="track"><i [style.width.%]="k.value"></i></div><b>{{k.value}}%</b></div>}
        <p><small>O global combina funcionalidades e frentes estruturais rastreadas. Metodologia atual: Concluída 100% · Em implementação 75% · Especificada 50% · Planejada 10%.</small></p>
      </div>
      <div class="card panel"><h2>Rastreabilidade do build</h2><p><b>Commit publicado:</b> {{buildInfo?.commitShort || 'carregando…'}}</p><p><b>Branch:</b> {{buildInfo?.branch || '—'}} · <b>Build:</b> #{{buildInfo?.runNumber || '—'}}</p><p><b>CI:</b> {{buildInfo?.ci || 'informação não registrada neste build'}}</p><p><b>Gerado em:</b> {{buildInfo?.builtAt || '—'}}</p><p><small>Os metadados são injetados no build sem expor token do repositório privado no navegador.</small></p></div>
    </div>
    <div class="card panel" style="margin-top:18px"><div class="panel-title"><h2>Progresso automático por frente</h2><span class="badge">{{modules.length}} frentes</span></div>@for(m of modules;track m.name){<div class="bar-row"><span>{{m.name}} <small>({{m.items}} itens)</small></span><div class="track"><i [style.width.%]="m.value"></i></div><b>{{m.value}}%</b></div>}</div>
    <div class="two bottom"><div class="card panel"><div class="panel-title"><h2>Em implementação / próximos marcos</h2><a routerLink="/funcionalidades">Ver funcionalidades</a></div><table><thead><tr><th>Código</th><th>Item</th><th>Frente</th><th>Status</th></tr></thead><tbody>@for(x of next;track x.code){<tr><td>{{x.code}}</td><td><b>{{x.name}}</b></td><td>{{x.module}}</td><td><span class="badge">{{x.status}}</span></td></tr>}</tbody></table></div><div class="card panel"><h2>Entregas consolidadas</h2>@for(a of activities;track a){<div class="activity">✓ <b>{{a}}</b><small>Consolidado</small></div>}</div></div>
  </section>`
})
export class DashboardComponent implements OnInit {
  buildInfo?: BuildInfo;
  private moduleProgress=progressByModule();
  private globalProgress=progressFor(PROJECT_FEATURES);
  kinds=progressByKind();
  kpis=[
    {icon:'◉',label:'Progresso global rastreado',value:`${this.globalProgress}%`,link:'/funcionalidades'},
    {icon:'⚙',label:'Estrutura técnica',value:`${this.kinds.find(x=>x.name==='Estrutura técnica')?.value ?? 0}%`,link:'/documentacao'},
    {icon:'▦',label:'Funcionalidades',value:`${this.kinds.find(x=>x.name==='Funcionalidades')?.value ?? 0}%`,link:'/funcionalidades'},
    {icon:'🧾',label:'Fiscal',value:`${this.progressOf('Fiscal')}%`,link:'/funcionalidades'},
    {icon:'🛒',label:'PDV',value:`${this.progressOf('PDV')}%`,link:'/funcionalidades'},
    {icon:'☁',label:'Infra / DevOps',value:`${this.progressOf('Infra/DevOps')}%`,link:'/documentacao'}
  ];
  modules=this.moduleProgress;
  next=PROJECT_FEATURES.filter(x=>x.status!=='Concluída').sort((a,b)=>this.priorityOrder(a.priority)-this.priorityOrder(b.priority)).slice(0,8);
  activities=['CI, Docker e deploy automático da Central validados','Documento Mestre e jornadas consolidados','Catálogo visual oficial publicado na Central','API de Perfil Fiscal por Filial implementada','Backlog funcional e estrutural unificado no cálculo automático'];
  ngOnInit():void{fetch('/build-info.json',{cache:'no-store'}).then(r=>r.ok?r.json():Promise.reject()).then(info=>this.buildInfo=info).catch(()=>this.buildInfo=undefined);}
  private progressOf(module:string):number{return this.moduleProgress.find(x=>x.name===module)?.value??0;}
  private priorityOrder(priority:string):number{return priority==='Crítica'?0:priority==='Alta'?1:2;}
}
