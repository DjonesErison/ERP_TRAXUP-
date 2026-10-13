import 'zone.js';
import {bootstrapApplication} from '@angular/platform-browser';
import {Component,signal} from '@angular/core';
import {CommonModule} from '@angular/common';
@Component({selector:'app-root',standalone:true,imports:[CommonModule],template:`
<main>
 <div class="canvas"><img src="assets/ui.webp" alt="UI-001 Dashboard do ERP TRAXUP">
  <button class="hot menu" aria-label="Abrir menu" (click)="toast('Menu lateral alternado')"></button>
  <button class="hot store" aria-label="Selecionar loja" (click)="panel.set('Loja Matriz')"></button>
  <button class="hot search" aria-label="Buscar" (click)="panel.set('Busca global')"></button>
  <button class="hot period" aria-label="Período" (click)="panel.set('Filtro de período')"></button>
  <button class="hot pdv" aria-label="Baixar PDV" (click)="toast('Download do PDV iniciado')"></button>
  <button class="hot remote" aria-label="Acesso remoto" (click)="toast('Solicitação de acesso remoto aberta')"></button>
  <button class="hot sales" aria-label="Ver vendas" (click)="panel.set('Últimas vendas')"></button>
  <button class="hot alerts" aria-label="Ver alertas" (click)="panel.set('Alertas e pendências')"></button>
  <button class="hot ai" aria-label="Assistente IA" (click)="panel.set('Assistente IA')"></button>
 </div>
 <section class="modal" *ngIf="panel()" (click)="panel.set('')"><article (click)="$event.stopPropagation()"><button class="close" (click)="panel.set('')">×</button><h2>{{panel()}}</h2>
  <ng-container [ngSwitch]="panel()"><div *ngSwitchCase="'Busca global'"><input autofocus placeholder="Buscar por produtos, clientes, pedidos..."><button (click)="toast('Busca executada')">Buscar</button></div><div *ngSwitchCase="'Filtro de período'"><button (click)="toast('Últimos 7 dias selecionado')">Últimos 7 dias</button><button (click)="toast('Últimos 30 dias selecionado')">Últimos 30 dias</button></div><div *ngSwitchCase="'Assistente IA'"><p>Olá! Como posso ajudar?</p><button (click)="toast('Resumo do dia preparado')">Resumir meu dia</button><button (click)="toast('Alertas analisados')">Analisar alertas</button></div><div *ngSwitchDefault><p>Visualização funcional de {{panel()}}.</p><button (click)="toast(panel()+' atualizado')">Confirmar</button></div></ng-container>
 </article></section><div class="toast" *ngIf="message()">✓ {{message()}}</div>
</main>`,styles:[]})
export class App{panel=signal('');message=signal('');toast(v:string){this.panel.set('');this.message.set(v);setTimeout(()=>this.message.set(''),2200)}}
bootstrapApplication(App).catch(console.error);
