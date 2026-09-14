import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PROJECT_FEATURES, evidenceCoverage } from '../data/project-progress';
@Component({standalone:true,imports:[RouterLink],template:`<section class="page">
<div class="page-head"><div><h1>Funcionalidades e frentes</h1><p>Backlog rastreável e fonte única do cálculo automático de progresso</p></div><a class="primary" routerLink="/funcionalidades/nova">＋ Novo item</a></div>
<div class="card panel"><div class="panel-title"><h2>Evidências de implementação</h2><span class="badge">{{coverage.withEvidence}}/{{coverage.total}} itens · {{coverage.percent}}%</span></div><p>PRs, commits, CI, deploys e documentos podem comprovar o estado de cada item. A cobertura de evidências é mostrada separadamente do percentual de progresso.</p></div>
<div class="card filters"><input placeholder="🔎 Buscar item..."><select><option>Todos os módulos</option><option>Fiscal</option><option>PDV</option><option>Financeiro</option><option>SaaS</option><option>Infra/DevOps</option><option>Segurança</option></select><select><option>Todos os status</option><option>Concluída</option><option>Em implementação</option><option>Especificada</option><option>Planejada</option></select><select><option>Todas prioridades</option><option>Crítica</option><option>Alta</option><option>Média</option></select></div>
<div class="card panel"><table><thead><tr><th>Código</th><th>Item</th><th>Frente</th><th>Prioridade</th><th>Status</th><th>Evidências</th></tr></thead><tbody>
@for(f of features;track f.code){<tr><td>{{f.code}}</td><td><b>{{f.name}}</b><small>{{f.desc}}</small></td><td>{{f.module}}</td><td><span class="badge priority-high">{{f.priority}}</span></td><td><span class="badge">{{f.status}}</span></td><td>@if(f.evidence?.length){@for(e of f.evidence;track e.type+e.ref){<div><a [href]="e.url" target="_blank" rel="noopener">{{e.type}} · {{e.ref}}</a><small>{{e.label}}</small></div>}}@else{<small>Sem evidência vinculada</small>}</td></tr>}
</tbody></table></div></section>`})
export class FunctionalitiesComponent{features=PROJECT_FEATURES;coverage=evidenceCoverage();}
