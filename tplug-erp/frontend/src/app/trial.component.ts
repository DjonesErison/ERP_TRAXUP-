import { ReenviarAtivacaoComponent } from './reenviar-ativacao.component';
import { CommonModule } from '@angular/common';
import { Component, ElementRef, EventEmitter, Output, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UiIconComponent } from './ui-icon.component';
import { TrialService } from './trial.service';

@Component({ selector: 'app-trial', standalone: true, imports: [ReenviarAtivacaoComponent, CommonModule, FormsModule, UiIconComponent], templateUrl: './trial.component.html', styleUrl: './trial.component.css' })
export class TrialComponent {
  @ViewChild('nomeInput') nomeInput?: ElementRef<HTMLInputElement>;
  focarCadastro(): void { this.nomeInput?.nativeElement.focus(); }
  @Output() voltarLogin = new EventEmitter<void>();
  @Output() ativarAdmin = new EventEmitter<string>();
  @Output() acessoCriado = new EventEmitter<{codigoEmpresa:string;email:string}>();
  nomeCompleto=''; nomeEmpresa=''; razaoSocial=''; documento=''; telefone=''; email=''; segmento=''; quantidadeLojas=1; aceitouTermos=false;
  duplicada=false; recuperando=false; recuperacaoMensagem='';
  loading=false; error=''; sucesso=false; codigoEmpresa=''; expiraEm=''; ativacaoToken='';
  constructor(private readonly trial: TrialService) {}
  cadastrar(): void {
    if (this.loading || !this.aceitouTermos) return;
    this.loading=true; this.error=''; this.duplicada=false;
    this.trial.cadastrar({nomeCompleto:this.nomeCompleto.trim(),nomeEmpresa:this.nomeEmpresa.trim(),razaoSocial:this.razaoSocial.trim(),
      documento:this.documento.replace(/\D/g,''),telefone:this.telefone.replace(/\D/g,''),email:this.email.trim(),segmento:this.segmento.trim()||undefined,
      quantidadeLojas:this.quantidadeLojas,aceitouTermos:true,termosVersao:'2026-09',idempotencyKey:this.idempotencyKey()}).subscribe({
      next:r=>{this.loading=false;this.sucesso=true;this.codigoEmpresa=r.codigoEmpresa;this.expiraEm=r.expiraEm;this.ativacaoToken=r.ativacaoToken||'';this.acessoCriado.emit({codigoEmpresa:r.codigoEmpresa,email:this.email.trim()});},
      error:e=>{this.loading=false;this.duplicada=e?.status===409;this.error=this.duplicada?'Empresa já cadastrada':e?.status===400?'Confira os dados informados e tente novamente.':'Não foi possível iniciar seu teste agora. Tente novamente.';}
    });
  }
  recuperar(): void {
    if(this.recuperando || !this.documento.trim() || !this.email.trim()) return;
    this.recuperando=true; this.recuperacaoMensagem='';
    this.trial.recuperar(this.documento,this.email.trim()).subscribe({
      next:()=>{this.recuperando=false;this.recuperacaoMensagem='Se os dados corresponderem ao cadastro, enviaremos as instruções ao e-mail cadastrado.';},
      error:()=>{this.recuperando=false;this.recuperacaoMensagem='Não foi possível solicitar a recuperação. Tente novamente.';}
    });
  }
  private idempotencyKey(): string {
    const key='traxup_trial_idempotency'; let value=sessionStorage.getItem(key);
    if(!value){value=crypto.randomUUID();sessionStorage.setItem(key,value);} return value;
  }
}
