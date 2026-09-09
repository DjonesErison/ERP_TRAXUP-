import {Component} from '@angular/core'; import {RouterLink} from '@angular/router';
@Component({standalone:true,imports:[RouterLink],template:`<section class="page"><div class="page-head"><div><h1>Módulos</h1><p>Status consolidado dos módulos do TRAXUP</p></div><a class="primary" routerLink="/roadmap">Roadmap →</a></div><div class="module-grid">@for(m of modules;track m.name){<div class="card module-card"><div class="module-icon">{{m.icon}}</div><div><h3>{{m.name}}</h3><p>{{m.desc}}</p><small>Status: {{m.status}} · {{m.progress}}%</small><div class="track"><i [style.width.%]="m.progress"></i></div></div><a routerLink="/funcionalidades">Abrir →</a></div>}</div></section>`})
export class ModulesComponent {modules=[
{name:'Arquitetura / Fundação',icon:'⚙',desc:'Spring Boot, API REST, Docker, padrões base e infraestrutura.',status:'Avançado',progress:85},
{name:'Multi-tenant / Segurança',icon:'🔐',desc:'Tenant, autenticação, RBAC, permissões e auditoria.',status:'Avançado',progress:85},
{name:'Banco / Migrations',icon:'🗄',desc:'PostgreSQL, migrations e evolução de schema.',status:'Avançado',progress:80},
{name:'Fiscal',icon:'🧾',desc:'Perfil fiscal, NF-e/NFC-e, SEFAZ, XML, DANFE, contingência e rejeições.',status:'Em implementação',progress:60},
{name:'Produtos / Estoque / Cadastros',icon:'📦',desc:'Produtos, grades, NCM, preços, inventário e movimentações.',status:'Em implementação',progress:55},
{name:'Central Web',icon:'🖥',desc:'Painel do produto, roadmap, funcionalidades, documentação e acompanhamento.',status:'Em atualização',progress:45},
{name:'Financeiro',icon:'💰',desc:'Contas, caixa, DRE, boletos, recebíveis e conciliação.',status:'Em implementação',progress:40},
{name:'PDV Desktop',icon:'🛒',desc:'Venda, caixa, SQLite local, impressão, últimas vendas e operação offline.',status:'Em implementação',progress:35},
{name:'Sincronização PDV ↔ Nuvem',icon:'🔄',desc:'Fila local, reenvio, idempotência, séries por terminal e retomada.',status:'Planejado / parcial',progress:30},
{name:'Administração SaaS',icon:'🏢',desc:'Planos, assinaturas, cobrança, bloqueio, trial e white-label.',status:'Planejado / parcial',progress:30},
{name:'Pagamentos / Conciliação',icon:'💳',desc:'TEF, Payment Hub, PIX, adquirentes, taxas, antecipações e divergências.',status:'Planejado',progress:25},
{name:'CRM / Fidelização',icon:'👥',desc:'Clientes, campanhas, retorno, relacionamento e pipeline.',status:'Planejado',progress:20},
{name:'BI / Relatórios',icon:'📊',desc:'Dashboards, relatórios dinâmicos, metas e indicadores.',status:'Planejado',progress:20},
{name:'Integrações externas',icon:'🔌',desc:'iFood, marketplaces, delivery, serviços e parceiros.',status:'Planejado',progress:15},
{name:'Apps / Mobile',icon:'📱',desc:'App vendedor, inventário mobile, Android e iOS.',status:'Planejado',progress:10}
];}