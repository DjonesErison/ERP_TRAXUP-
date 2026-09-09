import {Component} from '@angular/core';

@Component({
  standalone:true,
  template:`<section class="page">
    <div class="page-head"><div><h1>Ideias</h1><p>Backlog oficial e oportunidades do produto — não confundir com implementação concluída.</p></div></div>
    <div class="kanban">
      @for(c of columns;track c.title){
        <div class="kanban-col"><h3>{{c.title}} <span>{{c.items.length}}</span></h3>
          @for(i of c.items;track i.code){<div class="card idea"><small>{{i.code}}</small><b>{{i.name}}</b><p>{{i.module}}</p></div>}
        </div>
      }
    </div>
  </section>`
})
export class IdeasComponent {
  columns=[
    {title:'💡 Planejadas',items:[
      {code:'PDV-SELFCHECK',name:'Autoatendimento / Self-Checkout',module:'PDV'},
      {code:'PDV-TAP',name:'Tap to Pay / celular do vendedor',module:'PDV'},
      {code:'SUP-REMOTE',name:'Acesso remoto para suporte',module:'Suporte'},
      {code:'CERT-VENDA',name:'Venda de certificado digital',module:'SaaS Admin'},
      {code:'MOB-VENDEDOR',name:'Aplicativo do vendedor',module:'Mobile'},
      {code:'INT-MKT',name:'Integrações com marketplaces',module:'Integrações'},
      {code:'INT-IFOOD',name:'Integração iFood / delivery',module:'Delivery'}
    ]},
    {title:'🟡 Especificadas',items:[
      {code:'PDV-SEG-001',name:'Segunda Tela Interativa do Cliente',module:'PDV'},
      {code:'PDV-LAST',name:'Últimas Vendas / Consulta de Vendas',module:'PDV'},
      {code:'PRD-COMBO',name:'Produto Combo',module:'Produtos'},
      {code:'FIN-CONC',name:'Conciliação de Cartões',module:'Financeiro'},
      {code:'FIS-HOMO',name:'Assistente de Homologação Fiscal',module:'Fiscal'},
      {code:'SAA-TRIAL',name:'White Label + Trial 7 dias',module:'SaaS Admin'},
      {code:'ACC-PORTAL',name:'Portal da Contabilidade',module:'Fiscal/Contábil'},
      {code:'CRM-RET',name:'Campanhas de retorno de clientes',module:'CRM'},
      {code:'REL-DYN',name:'Relatórios dinâmicos',module:'BI/Relatórios'}
    ]},
    {title:'🔵 Em implementação',items:[
      {code:'FIS-NFE',name:'Emissão NF-e / NFC-e',module:'Fiscal'},
      {code:'FIS-SEFAZ',name:'Transmissão e integração SEFAZ',module:'Fiscal'},
      {code:'FIS-CONT',name:'Contingência fiscal',module:'Fiscal'},
      {code:'PDV-OFF',name:'Operação offline + sincronização',module:'PDV'},
      {code:'FIN-CORE',name:'Financeiro base / DRE / caixa',module:'Financeiro'}
    ]},
    {title:'🟢 Consolidadas',items:[
      {code:'CORE-MT',name:'Arquitetura multi-tenant',module:'Core'},
      {code:'FIS-PERFIL',name:'Perfil Fiscal por Filial',module:'Fiscal'},
      {code:'CENTRAL-WEB',name:'Central do Produto publicada',module:'Central'},
      {code:'INF-DOMAIN',name:'central.traxup.com.br + HTTPS',module:'Infraestrutura'},
      {code:'INF-CI',name:'CI + publicação Docker da Central',module:'DevOps'}
    ]}
  ];
}
