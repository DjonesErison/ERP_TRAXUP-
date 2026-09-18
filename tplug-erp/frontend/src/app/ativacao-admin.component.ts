import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AtivacaoAdminService } from './ativacao-admin.service';
@Component({selector:'app-ativacao-admin',standalone:true,imports:[CommonModule,FormsModule],templateUrl:'./ativacao-admin.component.html',styleUrl:'./ativacao-admin.component.css'})
export class AtivacaoAdminComponent{
 @Input({required:true}) token=''; @Output() concluida=new EventEmitter<void>();
 senha='';confirmacao='';loading=false;error='';
 constructor(private readonly service:AtivacaoAdminService){}
 ativar(){if(this.loading||this.senha.length<8||this.senha!==this.confirmacao)return;this.loading=true;this.error='';this.service.confirmar(this.token,this.senha).subscribe({next:()=>{this.loading=false;this.concluida.emit();},error:()=>{this.loading=false;this.error='O link de ativação é inválido ou expirou. Solicite um novo acesso.';}});}
}
