import { UiIconComponent } from './ui-icon.component';
import { AuthService } from './auth.service';
import { ReenviarAtivacaoComponent } from './reenviar-ativacao.component';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AtivacaoAdminService } from './ativacao-admin.service';
@Component({selector:'app-ativacao-admin',standalone:true,imports:[UiIconComponent,ReenviarAtivacaoComponent,CommonModule,FormsModule],templateUrl:'./ativacao-admin.component.html',styleUrl:'./ativacao-admin.component.css'})
export class AtivacaoAdminComponent{
 @Input() recuperacao=false;
 @Input() codigoEmpresa=''; @Input() email=''; @Input({required:true}) token=''; @Output() concluida=new EventEmitter<void>();
 ajudaAberta=false;
 senha='';confirmacao='';loading=false;error='';tentou=false;mostrarSenha=false;mostrarConfirmacao=false;
 get minimo(){return this.senha.length>=8;}
 get letraNumero(){return /[A-Za-z]/.test(this.senha)&&/\d/.test(this.senha);}
 get forte(){return this.minimo&&this.letraNumero;}
 get coincide(){return !!this.confirmacao&&this.confirmacao===this.senha;}
 get simbolo(){return /[^A-Za-z0-9]/.test(this.senha);}
 get pontos(){let n=0;if(this.senha.length>=8)n++;if(this.letraNumero)n++;if(this.senha.length>=12)n++;if(/[^A-Za-z0-9]/.test(this.senha))n++;return n;}
 get forca(){return ['','Fraca','Razoável','Boa','Forte'][this.pontos];}
 get corForca(){return ['#d1d5db','#dc2626','#f97316','#eab308','#16a34a'][this.pontos];}
 constructor(private readonly service:AtivacaoAdminService, private readonly auth:AuthService){}
 ativar(){this.tentou=true;if(this.loading||!this.forte||!this.coincide)return;this.loading=true;this.error='';(this.recuperacao?this.auth.confirmarRecuperacao(this.token,this.senha):this.service.confirmar(this.token,this.senha)).subscribe({next:()=>{this.loading=false;this.concluida.emit();},error:()=>{this.loading=false;this.error='O link é inválido ou expirou. Solicite um novo acesso.';}});}
}
