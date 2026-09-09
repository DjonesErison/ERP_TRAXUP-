import { Component } from '@angular/core'; import { RouterLink } from '@angular/router';
@Component({standalone:true,imports:[RouterLink],template:`<section class="page">
<div class="page-head"><div><h1>Funcionalidades</h1><p>Backlog consolidado e estado atual da implementação</p></div><a class="primary" routerLink="/funcionalidades/nova">＋ Nova Funcionalidade</a></div>
<div class="card filters"><input placeholder="🔎 Buscar funcionalidade..."><select><option>Todos os módulos</option><option>Fiscal</option><option>PDV</option><option>Financeiro</option><option>SaaS</option><option>CRM</option><option>BI</option></select><select><option>Todos os status</option><option>Concluída</option><option>Em implementação</option><option>Especificada</option><option>Planejada</option></select><select><option>Todas prioridades</option><option>Crítica</option><option>Alta</option><option>Média</option></select></div>
<div class="card panel"><table><thead><tr><th>Código</th><th>Funcionalidade</th><th>Módulo</th><th>Prioridade</th><th>Status</th><th>Ações</th></tr></thead><tbody>
@for(f of features;track f.code){<tr><td>{{f.code}}</td><td><b>{{f.name}}</b><small>{{f.desc}}</small></td><td>{{f.module}}</td><td><span class="badge priority-high">{{f.priority}}</span></td><td><span class="badge">{{f.status}}</span></td><td><button class="ghost">⋮</button></td></tr>}
</tbody></table></div></section>`})
export class FunctionalitiesComponent {
features=[
{code:'FIS-PERFIL',name:'Perfil Fiscal por Filial',desc:'Configuração fiscal isolada por tenant/filial, validações, RBAC e auditoria.',module:'Fiscal',priority:'Crítica',status:'Concluída'},
{code:'FIS-EMISSAO',name:'Integração do Perfil Fiscal com emissão',desc:'Impedir emissão sem configuração válida e alimentar o fluxo NF-e/NFC-e.',module:'Fiscal',priority:'Crítica',status:'Em implementação'},
{code:'FIS-SEFAZ',name:'Fluxo NF-e/NFC-e com SEFAZ',desc:'Transmissão, retorno, autorização/rejeição, XML, DANFE e armazenamento.',module:'Fiscal',priority:'Crítica',status:'Em implementação'},
{code:'FIS-CONT',name:'Contingência fiscal automática',desc:'Operar durante indisponibilidade e reconciliar após retorno.',module:'Fiscal',priority:'Crítica',status:'Especificada'},
{code:'FIS-REJ',name:'Correção de rejeições na transmissão',desc:'Tela para correção imediata de problemas fiscais retornados pela SEFAZ.',module:'Fiscal',priority:'Alta',status:'Especificada'},
{code:'PDV-OFF',name:'PDV Offline com SQLite por terminal',desc:'Banco local por PDV, operação offline e série própria por terminal.',module:'PDV',priority:'Crítica',status:'Em implementação'},
{code:'PDV-SYNC',name:'Sincronização PDV ↔ ERP Nuvem',desc:'Enviar vendas, receber alterações, reenvio seguro e idempotência.',module:'PDV',priority:'Crítica',status:'Em implementação'},
{code:'PDV-SEG-001',name:'Segunda Tela Interativa do Cliente',desc:'Itens, total, CPF, QR PIX, avaliação, ofertas e funcionamento offline.',module:'PDV',priority:'Alta',status:'Especificada'},
{code:'PDV-VENDAS',name:'Últimas Vendas / Consulta de Vendas',desc:'Consulta, reimpressão, cancelamento, pagamento e detalhamento conforme permissão.',module:'PDV',priority:'Alta',status:'Especificada'},
{code:'PDV-SELF',name:'Self-Checkout / Tap to Pay',desc:'Autoatendimento, POS integrado, NFC e celular do vendedor como terminal.',module:'PDV',priority:'Alta',status:'Planejada'},
{code:'COMBO-001',name:'Produto Combo',desc:'Combos fixos, escolhas, adicionais, promoções e baixa por componentes.',module:'Produtos',priority:'Alta',status:'Especificada'},
{code:'FIN-CONC',name:'Conciliação de Cartões',desc:'Cruzamento de vendas e recebíveis, taxas, antecipações, estornos e divergências.',module:'Financeiro',priority:'Alta',status:'Especificada'},
{code:'SAAS-ADM',name:'Administração SaaS',desc:'Clientes assinantes, planos, cobrança, inadimplência, bloqueio e histórico.',module:'SaaS',priority:'Alta',status:'Em implementação'},
{code:'CONT-PORTAL',name:'Portal da Contabilidade',desc:'XML por 5 anos, SPED, inventário, livro caixa e documentos fiscais.',module:'Contabilidade',priority:'Alta',status:'Planejada'},
{code:'TRIAL-001',name:'White-label + Trial 7 dias',desc:'Prospecção, liberação temporária e PDV demo com produtos pré-cadastrados.',module:'SaaS',priority:'Média',status:'Planejada'},
{code:'CRM-001',name:'CRM e fidelização',desc:'Campanhas, retorno de clientes e avisos após período sem compra.',module:'CRM',priority:'Média',status:'Planejada'},
{code:'BI-001',name:'BI e Relatórios Dinâmicos',desc:'Relatórios customizados, dashboards, estoque, vendas, margem e financeiro.',module:'BI',priority:'Média',status:'Planejada'},
{code:'INT-IFOOD',name:'Integração iFood / Delivery',desc:'Pedidos no PDV, aceite, status e fluxo integrado de delivery.',module:'Integrações',priority:'Média',status:'Planejada'}];
}