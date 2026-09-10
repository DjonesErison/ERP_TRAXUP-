import {Component} from '@angular/core';

type Reference = {code:string; title:string; file:string; status:string};

@Component({
  standalone:true,
  selector:'app-references',
  styles:[`
    .intro{margin-bottom:18px}.gallery{display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:18px}.reference{overflow:hidden;padding:0}.preview{display:block;background:#eef2f7;aspect-ratio:16/10;overflow:hidden}.preview img{width:100%;height:100%;object-fit:cover;display:block;transition:transform .2s ease}.preview:hover img{transform:scale(1.02)}.meta{padding:16px}.top{display:flex;align-items:center;justify-content:space-between;gap:12px}.code{font-weight:700;font-size:12px}.status{font-size:12px;opacity:.75}.meta h3{margin:8px 0 4px}.meta p{margin:0;opacity:.75;font-size:13px}
  `],
  template:`<section class="page">
    <div class="page-head"><div><h1>Referências / Imagens</h1><p>Catálogo visual oficial do TRAXUP para implementação e homologação das interfaces.</p></div></div>
    <div class="card panel intro"><div class="panel-title"><h2>▧ Catálogo Visual</h2><span class="badge">16 telas</span></div><p>As imagens abaixo são referências versionadas em <strong>docs/design</strong>. Clique em qualquer prévia para abrir a imagem completa. Elas orientam a implementação Angular e não representam, por si só, funcionalidade concluída.</p></div>
    <div class="gallery">
      @for(ref of references; track ref.code){
        <article class="card reference">
          <a class="preview" [href]="asset(ref.file)" target="_blank" rel="noopener" [title]="'Abrir '+ref.title">
            <img [src]="asset(ref.file)" [alt]="ref.title" loading="lazy">
          </a>
          <div class="meta"><div class="top"><span class="code">{{ref.code}}</span><span class="status">{{ref.status}}</span></div><h3>{{ref.title}}</h3><p>{{ref.file}}</p></div>
        </article>
      }
    </div>
  </section>`
})
export class ReferencesComponent {
  references:Reference[]=[
    {code:'UI-001',title:'Dashboard da Central',file:'01-central/dashboard-central.webp',status:'Direção visual aprovada'},
    {code:'UI-002',title:'Clientes',file:'02-cadastros/clientes.webp',status:'Revisão funcional'},
    {code:'UI-003',title:'Produtos',file:'02-cadastros/produtos.webp',status:'Revisão funcional'},
    {code:'UI-004',title:'Estoque',file:'03-estoque/estoque.webp',status:'Revisão funcional'},
    {code:'UI-005',title:'Vendas',file:'04-vendas/vendas.webp',status:'Revisão funcional'},
    {code:'UI-006',title:'Financeiro',file:'05-financeiro/financeiro.webp',status:'Revisão funcional'},
    {code:'UI-007',title:'Fiscal',file:'06-fiscal/modulo-fiscal.webp',status:'Revisão funcional'},
    {code:'UI-008',title:'CRM',file:'07-crm-bi/crm.webp',status:'Revisão funcional'},
    {code:'UI-009',title:'Relatórios',file:'08-relatorios/relatorios.webp',status:'Revisão funcional'},
    {code:'UI-010',title:'Configurações',file:'09-configuracoes/configuracoes.webp',status:'Revisão funcional'},
    {code:'UI-011',title:'PDV Desktop',file:'10-pdv/pdv-desktop.webp',status:'Direção visual aprovada'},
    {code:'UI-012',title:'Aplicativo do cliente',file:'11-aplicativos/aplicativo-cliente.webp',status:'Direção visual aprovada'},
    {code:'UI-013',title:'Aplicativo do vendedor',file:'11-aplicativos/aplicativo-vendedor.webp',status:'Direção visual aprovada'},
    {code:'UI-014',title:'Trial de 7 dias',file:'12-trial-admin/trial-7-dias.webp',status:'Revisão funcional'},
    {code:'UI-015',title:'Administração SaaS',file:'12-trial-admin/admin-clientes-saas.webp',status:'Revisão funcional'},
    {code:'UI-016',title:'Login da Central',file:'01-central/login-central.webp',status:'Revisão visual'}
  ];
  asset(file:string){return `/assets/design/${file}`;}
}
