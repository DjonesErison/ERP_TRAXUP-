import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone:true, imports:[RouterLink],
  template:`<section class="page">
    <div class="page-head">
      <div><h1>TRAXUP — Status da Implementação</h1><p>Visão consolidada do produto · última revisão 09/09/2026</p></div>
      <a class="primary" routerLink="/roadmap">Ver roadmap →</a>
    </div>

    <div class="kpis">
      @for (k of kpis; track k.label) {
        <div class="card kpi"><span class="kicon">{{k.icon}}</span><small>{{k.label}}</small><strong>{{k.value}}</strong><a [routerLink]="k.link">Ver detalhes →</a></div>
      }
    </div>

    <div class="two">
      <div class="card panel">
        <div class="panel-title"><h2>Progresso por Módulo</h2><a routerLink="/modulos">Ver módulos</a></div>
        @for (m of modules; track m.name) {
          <div class="bar-row"><span>{{m.name}}</span><div class="track"><i [style.width.%]="m.value"></i></div><b>{{m.value}}%</b></div>
        }
      </div>
      <div class="card panel">
        <h2>Marco atual</h2>
        <p><b>Foco:</b> núcleo fiscal e integração do perfil fiscal ao fluxo real de emissão NF-e/NFC-e.</p>
        <p><b>Último marco backend conhecido:</b> PR #206 integrada à develop, com 375 testes + migrations + Docker verdes.</p>
        <p><b>Central:</b> publicada em <b>central.traxup.com.br</b> via Docker/Nginx + Caddy.</p>
        <p><b>Observação:</b> os percentuais são estimativas consolidadas do projeto e serão refinados à medida que o backend for reconciliado automaticamente com o GitHub.</p>
      </div>
    </div>

    <div class="two bottom">
      <div class="card panel"><div class="panel-title"><h2>Em implementação / próximos marcos</h2><a routerLink="/funcionalidades">Ver funcionalidades</a></div>
        <table><thead><tr><th>Código</th><th>Funcionalidade</th><th>Módulo</th><th>Status</th></tr></thead><tbody>
        @for (x of next; track x.code) {<tr><td>{{x.code}}</td><td><b>{{x.name}}</b></td><td>{{x.module}}</td><td><span class="badge">{{x.status}}</span></td></tr>}
        </tbody></table>
      </div>
      <div class="card panel"><h2>Concluído / consolidado</h2>@for (a of activities; track a) {<div class="activity">✓ <b>{{a}}</b><small>Consolidado</small></div>}</div>
    </div>
  </section>`,
})
export class DashboardComponent {
  kpis=[
    {icon:'◉',label:'Progresso global',value:'≈ 50%',link:'/roadmap'},
    {icon:'✓',label:'Fundação / Arquitetura',value:'85%',link:'/modulos'},
    {icon:'🧾',label:'Fiscal',value:'60%',link:'/funcionalidades'},
    {icon:'📦',label:'Produtos / Estoque',value:'55%',link:'/funcionalidades'},
    {icon:'💰',label:'Financeiro',value:'40%',link:'/funcionalidades'},
    {icon:'🖥',label:'Central Web',value:'45%',link:'/historico'},
    {icon:'🛒',label:'PDV Desktop',value:'35%',link:'/funcionalidades'}
  ];
  modules=[
    {name:'Arquitetura / Fundação Backend',value:85},
    {name:'Multi-tenant / Segurança / RBAC',value:85},
    {name:'Banco / Migrations',value:80},
    {name:'Fiscal',value:60},
    {name:'Produtos / Estoque / Cadastros',value:55},
    {name:'Central Web Angular',value:45},
    {name:'Financeiro',value:40},
    {name:'PDV Desktop',value:35},
    {name:'Sincronização PDV ↔ Nuvem',value:30},
    {name:'Administração SaaS',value:30},
    {name:'Pagamentos / Conciliação',value:25},
    {name:'CRM / Fidelização',value:20},
    {name:'BI / Relatórios avançados',value:20},
    {name:'Apps / Mobile',value:10},
    {name:'Integrações externas',value:15}
  ];
  next=[
    {code:'FIS-CORE',name:'Integrar Perfil Fiscal ao fluxo de emissão',module:'Fiscal',status:'Em implementação'},
    {code:'FIS-NFE',name:'NF-e/NFC-e → SEFAZ → retorno → XML/DANFE',module:'Fiscal',status:'Próximo marco'},
    {code:'PDV-SYNC',name:'Sincronização offline PDV ↔ Nuvem',module:'PDV',status:'Planejado'},
    {code:'FIN-CONC',name:'Conciliação de cartões / recebíveis',module:'Financeiro',status:'Planejado'},
    {code:'PDV-SEG-001',name:'Segunda Tela Interativa do Cliente',module:'PDV',status:'Especificada'}
  ];
  activities=[
    'Central TRAXUP publicada em central.traxup.com.br',
    'Pipeline Docker da Central publicado no GHCR',
    'API de Perfil Fiscal por Filial implementada',
    'Multi-tenant, RBAC, auditoria e migrations em estágio avançado',
    'Arquitetura PDV offline com SQLite por terminal e série por PDV definida'
  ];
}
