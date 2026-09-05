import { Component } from '@angular/core'; import { FormsModule } from '@angular/forms'; import { RouterLink } from '@angular/router';
@Component({standalone:true,imports:[FormsModule,RouterLink],template:`<section class="page">
<div class="page-head"><div><h1>Nova Funcionalidade</h1><p>Transforme uma ideia em uma especificação organizada</p></div><a routerLink="/funcionalidades" class="secondary">Cancelar</a></div>
<form class="card form" (ngSubmit)="save()">
<div class="form-grid"><label>Nome *<input name="name" [(ngModel)]="item.name" required placeholder="Ex.: Devolução automática de venda"></label>
<label>Módulo *<select name="module" [(ngModel)]="item.module"><option>Vendas</option><option>Produtos</option><option>Estoque</option><option>Financeiro</option><option>Fiscal</option><option>CRM</option><option>PDV</option><option>Imobiliário</option></select></label>
<label>Prioridade<select name="priority" [(ngModel)]="item.priority"><option>Crítica</option><option>Alta</option><option>Média</option><option>Baixa</option></select></label>
<label>Status<select name="status" [(ngModel)]="item.status"><option>Ideia</option><option>Em análise</option><option>Especificada</option><option>Pronta para desenvolvimento</option></select></label></div>
<label>Objetivo<textarea name="goal" [(ngModel)]="item.goal" rows="3" placeholder="O que essa funcionalidade resolve?"></textarea></label>
<label>Descrição detalhada<textarea name="description" [(ngModel)]="item.description" rows="5" placeholder="Descreva o comportamento esperado."></textarea></label>
<div class="section-title">Especificação</div>
<div class="form-grid"><label>Problema que resolve<textarea name="problem" [(ngModel)]="item.problem" rows="4"></textarea></label><label>Fluxo<textarea name="flow" [(ngModel)]="item.flow" rows="4" placeholder="1. Entrada&#10;2. Validação&#10;3. Processamento&#10;4. Resultado"></textarea></label>
<label>Regras de negócio<textarea name="rules" [(ngModel)]="item.rules" rows="5" placeholder="RN-001 ..."></textarea></label><label>Dependências<textarea name="deps" [(ngModel)]="item.deps" rows="5" placeholder="Módulos ou funcionalidades relacionadas"></textarea></label></div>
<div class="section-title">Impactos</div><div class="checks">@for(x of impacts;track x){<label><input type="checkbox">{{x}}</label>}</div>
<div class="actions"><button type="submit" class="primary">Salvar funcionalidade</button><button type="button" class="secondary" (click)="generate()">Gerar documentação</button></div>
</form></section>`})
export class FeatureFormComponent {
item={name:'',module:'Vendas',priority:'Alta',status:'Ideia',goal:'',description:'',problem:'',flow:'',rules:'',deps:''}; impacts=['Frontend','Backend','Banco de dados','API','PDV','Estoque','Financeiro','Fiscal','CRM','BI'];
save(){alert('Funcionalidade salva no protótipo. Na versão integrada, será persistida no backend e vinculada ao GitHub.');}
generate(){alert('Ação preparada para gerar README, regra-negocio.md, fluxo.md, endpoints.md, banco.md e testes.md.');}
}