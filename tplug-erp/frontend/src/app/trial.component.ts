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
  nomeCompleto=''; nomeEmpresa=''; razaoSocial=''; documento=''; telefone=''; email=''; segmento=''; quantidadeLojas=1;
  duplicada=false; recuperando=false; recuperacaoMensagem='';
  loading=false; error=''; sucesso=false; codigoEmpresa=''; expiraEm=''; ativacaoToken='';
  constructor(private readonly trial: TrialService) {}

  fieldErrors: Record<string,string>={};
  atualizarCnpj(valor:string):void {
    const n=String(valor||'').replace(/\D/g,'').slice(0,14);
    let v=n.slice(0,2);
    if(n.length>2)v+='.'+n.slice(2,5);
    if(n.length>5)v+='.'+n.slice(5,8);
    if(n.length>8)v+='/'+n.slice(8,12);
    if(n.length>12)v+='-'+n.slice(12,14);
    this.documento=v;
    delete this.fieldErrors['documento'];
  }
  atualizarTelefone(valor:string):void {
    const n=String(valor||'').replace(/\D/g,'').slice(0,11);
    const ddd=n.slice(0,2);
    const local=n.slice(2);
    const split=local.length>8?5:4;
    this.telefone=n.length<=2?n:(`(${ddd}) `+local.slice(0,split)+(local.length>split?'-'+local.slice(split):''));
    delete this.fieldErrors['telefone'];
  }
  private validar():boolean {
    const e:Record<string,string>={};
    const n=this.documento.replace(/\D/g,'');
    const dig=(len:number)=>{let sum=0,w=len-7;for(let i=0;i<len;i++){sum+=Number(n[i])*w--;if(w<2)w=9;}const r=sum%11;return r<2?0:11-r;};
    if(!this.nomeEmpresa.trim())e['nomeEmpresa']='Informe o nome fantasia.';
    if(n.length!==14||/^(\d)\1{13}$/.test(n)||dig(12)!==Number(n[12])||dig(13)!==Number(n[13]))e['documento']='CNPJ inválido. Confira os dígitos.';
    if(!this.razaoSocial.trim())e['razaoSocial']='Informe a razão social.';
    if(!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(this.email.trim()))e['email']='Informe um e-mail válido.';
    if(![10,11].includes(this.telefone.replace(/\D/g,'').length))e['telefone']='Informe telefone com DDD.';
    if(!this.segmento.trim())e['segmento']='Selecione um segmento.';
    if(!this.nomeCompleto.trim())e['nomeCompleto']='Informe o responsável.';
    this.fieldErrors=e;return Object.keys(e).length===0;
  }
  cadastrar(): void {
    if (this.loading) return;
    this.error=''; this.duplicada=false;
    if(!this.validar())return;
    this.loading=true;
    this.trial.cadastrar({nomeCompleto:this.nomeCompleto.trim(),nomeEmpresa:this.nomeEmpresa.trim(),razaoSocial:this.razaoSocial.trim(),
      documento:this.documento.replace(/\D/g,''),telefone:this.telefone.replace(/\D/g,''),email:this.email.trim(),segmento:this.segmento.trim()||undefined,
      quantidadeLojas:this.quantidadeLojas,aceitouTermos:true,termosVersao:'2026-09',idempotencyKey:this.idempotencyKey()}).subscribe({
      next:r=>{this.loading=false;this.sucesso=true;this.codigoEmpresa=r.codigoEmpresa;this.expiraEm=r.expiraEm;this.ativacaoToken=r.ativacaoToken||'';this.acessoCriado.emit({codigoEmpresa:r.codigoEmpresa,email:this.email.trim()});},
      error:e=>{this.loading=false;this.duplicada=e?.status===409;const detail=String(e?.error?.detail||'');
        if(this.duplicada&&detail==='CNPJ_JA_CADASTRADO'){
          this.fieldErrors['documento']='Este CNPJ já está cadastrado.';
          this.error='CNPJ já cadastrado. Você pode recuperar seu acesso.';
        }else if(this.duplicada&&detail==='EMAIL_JA_CADASTRADO'){
          this.fieldErrors['email']='Este e-mail já está cadastrado.';
          this.error='E-mail já cadastrado. Você pode recuperar seu acesso.';
        }else if(this.duplicada){
          this.error='Já existe um cadastro com esses dados. Recupere seu acesso.';
        }else if(e?.status===400){
          const erros=e?.error?.erros;
          if(erros&&typeof erros==='object')for(const [campo,mensagem] of Object.entries(erros))this.fieldErrors[campo]=String(mensagem);
          this.error='Dados recusados pelo servidor. Confira as informações.';
        }else this.error='Não foi possível iniciar seu teste agora. Tente novamente.';}
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
