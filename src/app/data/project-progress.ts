export type ProjectStatus = 'Concluída' | 'Em implementação' | 'Especificada' | 'Planejada';

export interface ProjectFeature {
  code: string;
  name: string;
  desc: string;
  module: string;
  priority: 'Crítica' | 'Alta' | 'Média';
  status: ProjectStatus;
}

export const STATUS_WEIGHT: Record<ProjectStatus, number> = {
  'Concluída': 100,
  'Em implementação': 75,
  'Especificada': 50,
  'Planejada': 10
};

export const PROJECT_FEATURES: ProjectFeature[] = [
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
  {code:'INT-IFOOD',name:'Integração iFood / Delivery',desc:'Pedidos no PDV, aceite, status e fluxo integrado de delivery.',module:'Integrações',priority:'Média',status:'Planejada'}
];

export function progressFor(features: ProjectFeature[]): number {
  if (!features.length) return 0;
  return Math.round(features.reduce((total, feature) => total + STATUS_WEIGHT[feature.status], 0) / features.length);
}

export function progressByModule(): {name: string; value: number; items: number}[] {
  const grouped = new Map<string, ProjectFeature[]>();
  for (const feature of PROJECT_FEATURES) {
    grouped.set(feature.module, [...(grouped.get(feature.module) ?? []), feature]);
  }
  return [...grouped.entries()]
    .map(([name, features]) => ({name, value: progressFor(features), items: features.length}))
    .sort((a, b) => b.value - a.value || a.name.localeCompare(b.name));
}
