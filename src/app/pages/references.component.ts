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
    <div class="card panel intro"><div class="panel-title"><h2>▧ Catálogo Visual</h2><span class="badge">{{references.length}} telas</span></div><p>As imagens abaixo são referências versionadas em <strong>docs/design</strong>. Clique em qualquer prévia para abrir a imagem completa. Elas orientam a implementação Angular e não representam, por si só, funcionalidade concluída.</p></div>
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
    {code:'UI-001',title:'Dashboard do TraxUp ERP',file:'01-traxup-erp/UI-001-dashboard-traxup-erp.webp',status:'Aprovada como direção visual'},
    {code:'UI-002',title:'Clientes',file:'02-cadastros/clientes.webp',status:'Proposta para revisão funcional'},
    {code:'UI-003',title:'Produtos',file:'02-cadastros/produtos.webp',status:'Proposta para revisão funcional'},
    {code:'UI-004',title:'Estoque',file:'03-estoque/estoque.webp',status:'Proposta para revisão funcional'},
    {code:'UI-005',title:'Vendas',file:'04-vendas/vendas.webp',status:'Proposta para revisão funcional'},
    {code:'UI-006',title:'Financeiro',file:'05-financeiro/financeiro.webp',status:'Proposta para revisão funcional'},
    {code:'UI-007',title:'Fiscal',file:'06-fiscal/modulo-fiscal.webp',status:'Proposta para revisão funcional'},
    {code:'UI-008',title:'CRM',file:'07-crm-bi/crm.webp',status:'Proposta para revisão funcional'},
    {code:'UI-009',title:'Relatórios',file:'08-relatorios/relatorios.webp',status:'Proposta para revisão funcional'},
    {code:'UI-010',title:'Configurações',file:'09-configuracoes/configuracoes.webp',status:'Proposta para revisão funcional'},
    {code:'UI-011',title:'PDV Desktop',file:'10-pdv/pdv-desktop.webp',status:'Aprovada como direção visual'},
    {code:'UI-012',title:'Aplicativo do cliente',file:'11-aplicativos/aplicativo-cliente.webp',status:'Aprovada como direção visual'},
    {code:'UI-013',title:'Aplicativo do vendedor',file:'11-aplicativos/aplicativo-vendedor.webp',status:'Aprovada como direção visual'},
    {code:'UI-014',title:'Captação do trial de 7 dias',file:'12-trial-admin/trial-7-dias.webp',status:'Proposta para revisão funcional'},
    {code:'UI-015',title:'Administração de clientes SaaS',file:'12-trial-admin/admin-clientes-saas.webp',status:'Proposta para revisão funcional'},
    {code:'UI-016',title:'Login do TraxUp ERP',file:'01-traxup-erp/UI-016-login-traxup-erp.webp',status:'✅ Visual aprovado'},
    {code:'UI-017',title:'Recuperação de senha do TraxUp ERP',file:'01-traxup-erp/UI-017-recuperacao-senha-traxup-erp.webp',status:'✅ Visual aprovado'},
    {code:'UI-018',title:'Seleção de empresa e filial do TraxUp ERP',file:'01-traxup-erp/UI-018-selecao-empresa-filial-traxup-erp.webp',status:'✅ Visual aprovado'},
    {code:'UI-019',title:'Boas-vindas e configuração inicial do TraxUp ERP',file:'01-traxup-erp/UI-019-onboarding-traxup-erp.webp',status:'✅ Visual aprovado'},
    {code:'UI-020',title:'Usuários e permissões do TraxUp ERP',file:'01-traxup-erp/UI-020-usuarios-permissoes-traxup-erp.webp',status:'✅ Visual aprovado'},
    {code:'UI-021',title:'Cadastro e edição de cliente do TraxUp ERP',file:'02-cadastros/UI-021-cadastro-cliente-traxup-erp.webp',status:'✅ Visual aprovado'},
    {code:'UI-022',title:'Visão 360º do cliente do TraxUp ERP',file:'02-cadastros/UI-022-visao-360-cliente-traxup-erp.webp',status:'✅ Visual aprovado'},
    {code:'UI-023',title:'Cadastro e edição de produto do TraxUp ERP',file:'02-cadastros/UI-023-cadastro-produto-traxup-erp.webp',status:'✅ Visual aprovado'},
    {code:'UI-024',title:'Produto — Preços do TraxUp ERP',file:'02-cadastros/UI-024-produto-precos-traxup-erp.webp',status:'✅ Visual aprovado'}
  ];
  asset(file:string){return `/assets/design/${file}`;}
}
