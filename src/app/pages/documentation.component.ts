import {Component} from '@angular/core';

@Component({
  standalone:true,
  template:`<section class="page">
    <div class="page-head"><div><h1>Documentação</h1><p>Conhecimento oficial e decisões consolidadas do produto.</p></div></div>
    <div class="doc-grid">
      @for(d of docs;track d.title){
        <div class="card panel">
          <div class="doc-icon">□</div><h3>{{d.title}}</h3><p>{{d.desc}}</p><small>{{d.path}}</small>
          <div class="doc-foot"><span>{{d.status}}</span><span class="badge">{{d.scope}}</span></div>
        </div>
      }
    </div>
  </section>`
})
export class DocumentationComponent {
  docs=[
    {title:'Arquitetura Geral',desc:'Angular 19, Spring Boot, PostgreSQL multi-tenant, SQLite no PDV, Kafka/Redis, Docker e S3 compatível.',path:'docs/arquitetura/',status:'Consolidado',scope:'Arquitetura'},
    {title:'Fiscal',desc:'Perfil fiscal por filial, NF-e/NFC-e, SEFAZ, contingência, homologação, rejeições e repositório XML.',path:'docs/fiscal/',status:'Em evolução',scope:'Fiscal'},
    {title:'PDV',desc:'Operação offline, séries por terminal, sincronização, mesas, delivery, impressão, segunda tela e últimas vendas.',path:'docs/pdv/',status:'Especificado / parcial',scope:'PDV'},
    {title:'Financeiro',desc:'DRE, caixa, contas, conciliação de cartões, cobrança SaaS, inadimplência e integrações financeiras.',path:'docs/financeiro/',status:'Em evolução',scope:'Financeiro'},
    {title:'SaaS Admin',desc:'Clientes assinantes, planos, faturamento, bloqueio, trial, certificados e suporte.',path:'docs/saas-admin/',status:'Especificado',scope:'Admin'},
    {title:'API e Autenticação',desc:'REST, Swagger, JWT + refresh token, integrações e contratos entre PDV, ERP e serviços externos.',path:'docs/api/',status:'Em evolução',scope:'API'},
    {title:'Banco de Dados',desc:'Entidades, isolamento multi-tenant, migrations e modelo local do PDV.',path:'docs/banco/',status:'Em evolução',scope:'Dados'},
    {title:'Testes e CI/CD',desc:'Critérios de aceite, migrations, build Angular, Docker e validações de merge.',path:'docs/testes/',status:'Ativo',scope:'Qualidade'},
    {title:'Regras de Negócio',desc:'Requisitos oficiais organizados por módulo e separados de status de implementação.',path:'docs/negocio/',status:'Consolidado',scope:'Produto'},
    {title:'Infraestrutura',desc:'VPS, /opt/traxup, Caddy, HTTPS, containers, GHCR e estratégia de deploy.',path:'docs/infra/',status:'Consolidado',scope:'DevOps'},
    {title:'LGPD e Segurança',desc:'Proteção de dados, permissões, auditoria, segredos e boas práticas de acesso.',path:'docs/seguranca/',status:'Especificado',scope:'Segurança'},
    {title:'ADRs',desc:'Registro das principais decisões arquiteturais e justificativas do projeto.',path:'docs/adr/',status:'A manter continuamente',scope:'Decisões'}
  ];
}
