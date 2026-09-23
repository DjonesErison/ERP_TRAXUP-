import { AuthService } from './auth.service';
import { ReenviarAtivacaoComponent } from './reenviar-ativacao.component';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AtivacaoAdminService } from './ativacao-admin.service';
@Component({selector:'app-ativacao-admin',standalone:true,imports:[ReenviarAtivacaoComponent,CommonModule,FormsModule],templateUrl:'./ativacao-admin.component.html',styleUrl:'./ativacao-admin.component.css'})
export class AtivacaoAdminComponent{
 @Input() recuperacao=false;
 @Input() codigoEmpresa=''; @Input() email=''; @Input({required:true}) token=''; @Output() concluida=new EventEmitter<void>();
 senha='';confirmacao='';loading=false;error='';
 constructor(private readonly service:AtivacaoAdminService, private readonly auth:AuthService){}
 ativar(){if(this.loading||this.senha.length<8||this.senha!==this.confirmacao)return;this.loading=true;this.error='';(this.recuperacao?this.auth.confirmarRecuperacao(this.token,this.senha):this.service.confirmar(this.token,this.senha)).subscribe({next:()=>{this.loading=false;this.concluida.emit();},error:()=>{this.loading=false;this.error='O link é inválido ou expirou. Solicite um novo acesso.';}});}
}
