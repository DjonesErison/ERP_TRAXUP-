import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TrialService } from './trial.service';

@Component({selector:'app-reenviar-ativacao', standalone:true, imports:[CommonModule,FormsModule],
 template: `<section class="resend"><h3>Reenviar ativação</h3><p>Informe os dados recebidos no cadastro. Aguarde um minuto entre solicitações.</p>
 <form #form="ngForm" (ngSubmit)="form.valid && enviar()">
 <label>Empresa<input name="empresaAtivacao" [(ngModel)]="tenantId" required pattern="[0-9a-fA-F-]{36}" autocomplete="organization"></label>
 <label>E-mail<input name="emailAtivacao" [(ngModel)]="email" type="email" required email maxlength="254" autocomplete="email"></label>
 <button type="submit" [disabled]="loading || !form.valid">{{loading ? 'Solicitando...' : 'Reenviar link de ativação'}}</button>
 <p role="status" *ngIf="mensagem">{{mensagem}}</p><p role="alert" *ngIf="error">{{error}}</p></form></section>`,
 styles:[`:host{display:block}.resend{margin-top:20px;text-align:left;font:inherit}.resend h3{margin-bottom:8px}.resend p{line-height:1.5;overflow-wrap:anywhere}label{display:block;margin:12px 0}input{display:block;width:100%;box-sizing:border-box;padding:12px;border:1px solid #bdc9dc;border-radius:8px;font:inherit}button{padding:12px;border:0;border-radius:8px;background:#174d99;color:white;font:inherit;cursor:pointer}button:disabled{opacity:.6}`]})
export class ReenviarAtivacaoComponent {
 @Input() tenantId=''; @Input() email=''; loading=false; mensagem=''; error='';
 constructor(private readonly trial:TrialService){}
 enviar():void {
   if(this.loading)return;
   this.loading=true;this.mensagem='';this.error='';
   this.trial.reenviar(this.tenantId.trim(),this.email.trim()).subscribe({
     next:()=>{this.loading=false;this.mensagem='Se houver um cadastro aguardando ativação, enviaremos um novo link. Confira também a pasta de spam.';},
     error:()=>{this.loading=false;this.error='Não foi possível solicitar o envio. Confira os dados e tente novamente.';}
   });
 }
}
