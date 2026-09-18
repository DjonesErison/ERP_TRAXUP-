import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TrialService } from './trial.service';

@Component({ selector: 'app-trial', standalone: true, imports: [CommonModule, FormsModule], templateUrl: './trial.component.html', styleUrl: './trial.component.css' })
export class TrialComponent {
  @Output() voltarLogin = new EventEmitter<void>();
  nomeCompleto=''; nomeEmpresa=''; razaoSocial=''; documento=''; telefone=''; email=''; segmento=''; quantidadeLojas=1; aceitouTermos=false;
  loading=false; error=''; sucesso=false; tenantId=''; expiraEm='';
  constructor(private readonly trial: TrialService) {}
  cadastrar(): void {
    if (this.loading || !this.aceitouTermos) return;
    this.loading=true; this.error='';
    this.trial.cadastrar({nomeCompleto:this.nomeCompleto.trim(),nomeEmpresa:this.nomeEmpresa.trim(),razaoSocial:this.razaoSocial.trim(),
      documento:this.documento.replace(/\D/g,''),telefone:this.telefone.replace(/\D/g,''),email:this.email.trim(),segmento:this.segmento.trim()||undefined,
      quantidadeLojas:this.quantidadeLojas,aceitouTermos:true,termosVersao:'2026-09',idempotencyKey:this.idempotencyKey()}).subscribe({
      next:r=>{this.loading=false;this.sucesso=true;this.tenantId=r.tenantId;this.expiraEm=r.expiraEm;},
      error:e=>{this.loading=false;this.error=e?.status===400?'Confira os dados informados e tente novamente.':'Não foi possível iniciar seu teste agora. Tente novamente.';}
    });
  }
  private idempotencyKey(): string {
    const key='traxup_trial_idempotency'; let value=sessionStorage.getItem(key);
    if(!value){value=crypto.randomUUID();sessionStorage.setItem(key,value);} return value;
  }
}
