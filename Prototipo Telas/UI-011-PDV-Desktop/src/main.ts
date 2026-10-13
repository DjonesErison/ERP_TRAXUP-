import 'zone.js';
import {bootstrapApplication} from '@angular/platform-browser';
import {Component,signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
@Component({selector:'app-root',standalone:true,imports:[CommonModule,FormsModule],template:`
<main><div class="canvas"><img src="assets/ui.webp" alt="UI-011 PDV Desktop TRAXUP">
 <button class="hot search" (click)="dialog.set('produto')" aria-label="Buscar produto"></button><button class="hot keyboard" (click)="dialog.set('teclado')" aria-label="Teclado virtual"></button><button class="hot customer" (click)="dialog.set('cliente')" aria-label="Identificar consumidor"></button>
 <button class="hot qty" (click)="dialog.set('quantidade')" aria-label="Alterar quantidade"></button><button class="hot discount" (click)="dialog.set('desconto')" aria-label="Aplicar desconto"></button><button class="hot history" (click)="toast('Últimas vendas abertas')" aria-label="Últimas vendas"></button>
 <button class="hot cancelitem" (click)="toast('Item selecionado cancelado')" aria-label="Cancelar item"></button><button class="hot cancel" (click)="dialog.set('cancelar')" aria-label="Cancelar venda"></button><button class="hot receive" (click)="dialog.set('receber')" aria-label="Receber"></button><button class="hot settings" (click)="toast('Configurações abertas')" aria-label="Configurações"></button>
 </div>
 <section class="modal" *ngIf="dialog()" (click)="dialog.set('')"><article (click)="$event.stopPropagation()"><button class="close" (click)="dialog.set('')">×</button>
  <div *ngIf="dialog()==='produto'"><h2>Localizar produto</h2><input [(ngModel)]="value" autofocus placeholder="Código de barras ou descrição"><button (click)="toast('Produto adicionado à venda')">Adicionar produto</button></div>
  <div *ngIf="dialog()==='cliente'"><h2>Identificar consumidor</h2><input [(ngModel)]="value" placeholder="CPF, CNPJ ou nome"><button (click)="toast('Cliente vinculado à venda')">Identificar cliente</button></div>
  <div *ngIf="dialog()==='quantidade'"><h2>Alterar quantidade</h2><input type="number" [(ngModel)]="value" min="1"><button (click)="toast('Quantidade atualizada')">Aplicar</button></div>
  <div *ngIf="dialog()==='desconto'"><h2>Aplicar desconto</h2><input [(ngModel)]="value" placeholder="R$ 0,00"><button (click)="toast('Desconto aplicado')">Aplicar desconto</button></div>
  <div *ngIf="dialog()==='receber'"><h2>Receber R$ 189,70</h2><div class="pay"><button (click)="toast('Pagamento em dinheiro selecionado')">Dinheiro</button><button (click)="toast('Pagamento PIX selecionado')">PIX</button><button (click)="toast('Cartão selecionado')">Cartão</button></div></div>
  <div *ngIf="dialog()==='cancelar'"><h2>Cancelar venda?</h2><p>Esta ação limpa todos os itens da venda atual.</p><button class="danger" (click)="toast('Venda cancelada')">Confirmar cancelamento</button></div>
  <div *ngIf="dialog()==='teclado'"><h2>Teclado virtual</h2><div class="keys">@for(k of keys;track k){<button (click)="value=value+k">{{k}}</button>}</div><input [(ngModel)]="value"><button (click)="toast('Código confirmado')">Confirmar</button></div>
 </article></section><div class="toast" *ngIf="message()">✓ {{message()}}</div>
</main>`,styles:[]})
export class App{dialog=signal('');message=signal('');value:any='';keys=['1','2','3','4','5','6','7','8','9','0','←'];toast(v:string){this.dialog.set('');this.message.set(v);setTimeout(()=>this.message.set(''),2200)}}
bootstrapApplication(App).catch(console.error);
