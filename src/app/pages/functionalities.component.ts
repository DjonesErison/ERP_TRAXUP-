import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PROJECT_FEATURES } from '../data/project-progress';

@Component({standalone:true,imports:[RouterLink],template:`<section class="page">
<div class="page-head"><div><h1>Funcionalidades</h1><p>Backlog consolidado e fonte única do cálculo automático de progresso</p></div><a class="primary" routerLink="/funcionalidades/nova">＋ Nova Funcionalidade</a></div>
<div class="card filters"><input placeholder="🔎 Buscar funcionalidade..."><select><option>Todos os módulos</option><option>Fiscal</option><option>PDV</option><option>Financeiro</option><option>SaaS</option><option>CRM</option><option>BI</option></select><select><option>Todos os status</option><option>Concluída</option><option>Em implementação</option><option>Especificada</option><option>Planejada</option></select><select><option>Todas prioridades</option><option>Crítica</option><option>Alta</option><option>Média</option></select></div>
<div class="card panel"><table><thead><tr><th>Código</th><th>Funcionalidade</th><th>Módulo</th><th>Prioridade</th><th>Status</th><th>Ações</th></tr></thead><tbody>
@for(f of features;track f.code){<tr><td>{{f.code}}</td><td><b>{{f.name}}</b><small>{{f.desc}}</small></td><td>{{f.module}}</td><td><span class="badge priority-high">{{f.priority}}</span></td><td><span class="badge">{{f.status}}</span></td><td><button class="ghost">⋮</button></td></tr>}
</tbody></table></div></section>`})
export class FunctionalitiesComponent {
  features = PROJECT_FEATURES;
}
