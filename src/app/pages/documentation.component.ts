import {Component} from '@angular/core';

@Component({
  standalone:true,
  template:`<section class="page">
    <div class="page-head"><div><h1>Documentação</h1><p>Conhecimento oficial, jornadas e decisões consolidadas do produto.</p></div></div>
    <div class="card panel" style="margin-bottom:18px">
      <div class="panel-title"><h2>Documento Mestre TRAXUP</h2><span class="badge">Atualizado</span></div>
      <p>Visão funcional consolidada em linguagem de negócio: jornada do cliente, jornada do produto, módulos, telas, dados, dashboard, frontend, PDV, fiscal, financeiro, CRM, BI, SaaS Admin, integrações, segurança e roadmap.</p>
      <a href="https://github.com/DjonesErison/ERP_TRAXUP-/blob/main/docs/TRAXUP-DOCUMENTO-MESTRE.md" target="_blank" rel="noopener noreferrer">Abrir Documento Mestre</a>
    </div>
    <div class="doc-grid">
      @for(d of docs;track d.title){
        <div class="card panel">
          <div class="doc-icon">□</div><h3>{{d.title}}</h3><p>{{d.desc}}</p><a [href]="documentUrl(d.path)" target="_blank" rel="noopener noreferrer">Abrir documentação</a>
          <div class="doc-foot"><span>{{d.status}}</span><span class="badge">{{d.scope}}</span></div>
        </div>
      }
    </div>
  </section>`
})
export class DocumentationComponent {
  docs=[
    {title:'Documento Mestre / Jornadas',desc:'Jornada do cliente e do produto, visão do ecossistema, fluxos de venda, estoque, fiscal, financeiro e roadmap funcional.',path:'docs/TRAXUP-DOCUMENTO-MESTRE.md',status:'Consolidado',scope:'Produto'},
    {title:'Catálogo Visual Oficial',desc:'Referências aprovadas e propostas de ERP Web, PDV, aplicativos e administração SaaS. Aprovação visual não significa funcionalidade implementada.',path:'docs/design/README.md',status:'Ativo',scope:'Design'},
    {title:'Arquitetura Geral',desc:'Angular 19, Spring Boot, PostgreSQL multi-tenant, SQLite no PDV, Kafka/Redis, Docker e S3 compatível.',path:'docs/arquitetura/README.md',status:'Consolidado',scope:'Arquitetura'},
    {title:'Fiscal',desc:'Perfil fiscal por filial, NF-e/NFC-e, SEFAZ, contingência, homologação, rejeições e repositório XML.',path:'docs/TRAXUP-DOCUMENTO-MESTRE.md#fiscal',status:'Em evolução',scope:'Fiscal'},
    {title:'PDV',desc:'Operação offline, séries por terminal, sincronização, mesas, delivery, impressão, segunda tela, self-checkout e últimas vendas.',path:'docs/TRAXUP-DOCUMENTO-MESTRE.md#pdv',status:'Especificado / parcial',scope:'PDV'},
    {title:'Financeiro',desc:'DRE, caixa, contas, conciliação de cartões, cobrança SaaS, inadimplência e integrações financeiras.',path:'docs/TRAXUP-DOCUMENTO-MESTRE.md#financeiro',status:'Em evolução',scope:'Financeiro'},
    {title:'SaaS Admin',desc:'Clientes assinantes, planos, faturamento, bloqueio, trial, certificados e suporte.',path:'docs/TRAXUP-DOCUMENTO-MESTRE.md#administração-saas-trial-e-suporte',status:'Especificado',scope:'Admin'},
    {title:'CRM / BI / Relatórios',desc:'Fidelização, segmentação, retorno de clientes, indicadores gerenciais e relatórios dinâmicos.',path:'docs/TRAXUP-DOCUMENTO-MESTRE.md',status:'Especificado',scope:'Gestão'},
    {title:'API e Autenticação',desc:'REST, Swagger, JWT + refresh token, integrações e contratos entre PDV, ERP e serviços externos.',path:'docs/api/README.md',status:'Em evolução',scope:'API'},
    {title:'Banco de Dados',desc:'Entidades, isolamento multi-tenant, migrations e modelo local do PDV.',path:'docs/banco/README.md',status:'Em evolução',scope:'Dados'},
    {title:'Testes e CI/CD',desc:'Critérios de aceite, migrations, build Angular, Docker e validações de merge.',path:'docs/testes/README.md',status:'Ativo',scope:'Qualidade'},
    {title:'Regras de Negócio',desc:'Requisitos oficiais organizados por módulo e separados de status de implementação.',path:'docs/negocio/README.md',status:'Consolidado',scope:'Produto'},
    {title:'Infraestrutura',desc:'VPS, /opt/traxup, Caddy, HTTPS, containers, GHCR e estratégia de deploy.',path:'deploy/README.md',status:'Consolidado',scope:'DevOps'},
    {title:'LGPD e Segurança',desc:'Proteção de dados, permissões, auditoria, segredos e boas práticas de acesso.',path:'docs/TRAXUP-DOCUMENTO-MESTRE.md#multiempresa-segurança-e-lgpd',status:'Especificado',scope:'Segurança'},
    {title:'ADRs',desc:'Registro das principais decisões arquiteturais e justificativas do projeto.',path:'docs/adr/',status:'A manter continuamente',scope:'Decisões'}
  ];

  documentUrl(path:string):string {
    const view=path.endsWith('/')?'tree':'blob';
    return `https://github.com/DjonesErison/ERP_TRAXUP-/${view}/main/${path}`;
  }
}

