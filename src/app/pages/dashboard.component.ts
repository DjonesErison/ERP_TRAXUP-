import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone:true, imports:[RouterLink],
  template:`<section class="page">
    <div class="page-head"><div><h1>Dashboard</h1><p>Visão geral do produto</p></div><a class="primary" routerLink="/funcionalidades/nova">＋ Nova Funcionalidade</a></div>
    <div class="kpis">
      @for (k of kpis; track k.label) { <div class="card kpi"><span class="kicon">{{k.icon}}</span><small>{{k.label}}</small><strong>{{k.value}}</strong><a [routerLink]="k.link">Ver detalhes →</a></div> }
    </div>
    <div class="two">
      <div class="card panel"><div class="panel-title"><h2>Funcionalidades por Módulo</h2><a routerLink="/modulos">Ver todas</a></div>
        @for (m of modules; track m.name) { <div class="bar-row"><span>{{m.name}}</span><div class="track"><i [style.width.%]="m.value"></i></div><b>{{m.value}}%</b></div> }
      </div>
      <div class="card panel"><h2>Funcionalidades por Prioridade</h2><div class="priority"><div class="donut"></div><div class="legend"><p>🔴 Crítica <b>28 (13%)</b></p><p>🟠 Alta <b>78 (38%)</b></p><p>🟡 Média <b>62 (30%)</b></p><p>🟢 Baixa <b>40 (19%)</b></p></div></div></div>
    </div>
    <div class="two bottom">
      <div class="card panel"><div class="panel-title"><h2>Próximas Funcionalidades</h2><a routerLink="/roadmap">Ver roadmap</a></div>
        <table><thead><tr><th>Código</th><th>Funcionalidade</th><th>Módulo</th><th>Status</th><th>Previsão</th></tr></thead><tbody>
        @for (x of next; track x.code) {<tr><td>{{x.code}}</td><td><b>{{x.name}}</b></td><td>{{x.module}}</td><td><span class="badge">{{x.status}}</span></td><td>{{x.date}}</td></tr>}
        </tbody></table>
      </div>
      <div class="card panel"><h2>Atividades Recentes</h2>@for (a of activities; track a) {<div class="activity">● <b>{{a}}</b><small>Hoje</small></div>}</div>
    </div>
  </section>`,
})
export class DashboardComponent {
  kpis=[{icon:'💡',label:'Ideias',value:37,link:'/ideias'},{icon:'◷',label:'Em análise',value:18,link:'/funcionalidades'},{icon:'▤',label:'Especificadas',value:42,link:'/funcionalidades'},{icon:'🚀',label:'Prontas',value:25,link:'/funcionalidades'},{icon:'〈/〉',label:'Em desenvolvimento',value:8,link:'/funcionalidades'},{icon:'⚗',label:'Em teste',value:5,link:'/testes'},{icon:'✓',label:'Concluídas',value:73,link:'/funcionalidades'}];
  modules=[{name:'Produtos',value:85},{name:'Vendas',value:72},{name:'Estoque',value:60},{name:'Financeiro',value:55},{name:'Fiscal',value:48},{name:'CRM',value:40},{name:'PDV',value:65}];
  next=[{code:'DEV-00045',name:'Devolução automática de venda',module:'Vendas',status:'Pronta',date:'15/09/2026'},{code:'FIS-00012',name:'NCM automático',module:'Fiscal',status:'Análise',date:'20/09/2026'},{code:'EST-00033',name:'Inventário mobile',module:'Estoque',status:'Em desenvolvimento',date:'30/09/2026'},{code:'FIN-00022',name:'Conciliação bancária automática',module:'Financeiro',status:'Especificada',date:'10/10/2026'}];
  activities=['Devolução automática de venda criada','Entrada inteligente de estoque atualizada','Inventário mobile passou para desenvolvimento','Nova regra RN-045 adicionada'];
}