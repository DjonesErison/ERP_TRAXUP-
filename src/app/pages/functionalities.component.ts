import { Component } from '@angular/core'; import { RouterLink } from '@angular/router';
@Component({standalone:true,imports:[RouterLink],template:`<section class="page">
<div class="page-head"><div><h1>Funcionalidades</h1><p>Especificações e evolução do produto</p></div><a class="primary" routerLink="/funcionalidades/nova">＋ Nova Funcionalidade</a></div>
<div class="card filters"><input placeholder="🔎 Buscar funcionalidade..."><select><option>Todos os módulos</option><option>Vendas</option><option>Estoque</option><option>Fiscal</option></select><select><option>Todos os status</option><option>Ideia</option><option>Especificada</option><option>Em desenvolvimento</option><option>Concluída</option></select><select><option>Todas prioridades</option><option>Crítica</option><option>Alta</option><option>Média</option><option>Baixa</option></select></div>
<div class="card panel"><table><thead><tr><th>Código</th><th>Funcionalidade</th><th>Módulo</th><th>Prioridade</th><th>Status</th><th>Ações</th></tr></thead><tbody>
@for(f of features;track f.code){<tr><td>{{f.code}}</td><td><b>{{f.name}}</b><small>{{f.desc}}</small></td><td>{{f.module}}</td><td><span class="badge priority-high">{{f.priority}}</span></td><td><span class="badge">{{f.status}}</span></td><td><button class="ghost">⋮</button></td></tr>}
</tbody></table></div></section>`})
export class FunctionalitiesComponent {
features=[
{code:'DEV-00045',name:'Devolução automática de venda',desc:'Localizar venda, validar fiscal, estoque e financeiro.',module:'Vendas',priority:'Alta',status:'Pronta'},
{code:'COM-00021',name:'Entrada inteligente de compras',desc:'XML → fornecedor → produtos → estoque → financeiro.',module:'Compras',priority:'Alta',status:'Concluída'},
{code:'EST-00033',name:'Inventário mobile',desc:'Contagem, divergência, aprovação e ajuste.',module:'Estoque',priority:'Média',status:'Em desenvolvimento'},
{code:'FIS-00012',name:'NCM automático',desc:'Sugestão por EAN, fornecedor, histórico e confiança.',module:'Fiscal',priority:'Alta',status:'Em análise'},
{code:'FIN-00022',name:'Conciliação bancária automática',desc:'Importação, correspondência e exceções.',module:'Financeiro',priority:'Alta',status:'Especificada'},
{code:'CRM-00018',name:'Pipeline de vendas',desc:'Funil, tarefas, histórico e automações.',module:'CRM',priority:'Média',status:'Especificada'}];
}