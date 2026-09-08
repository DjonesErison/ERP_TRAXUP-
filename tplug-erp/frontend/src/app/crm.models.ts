export interface ClienteInativo {
  clienteId: string;
  nomeRazaoSocial: string;
  nomeFantasia?: string | null;
  email?: string | null;
  telefone?: string | null;
  ultimaCompraEm: string;
  quantidadeCompras: number;
}

export interface ClienteRfv {
  clienteId: string;
  nomeRazaoSocial: string;
  nomeFantasia?: string | null;
  email?: string | null;
  telefone?: string | null;
  ultimaCompraEm: string;
  diasDesdeUltimaCompra: number;
  quantidadeCompras: number;
  valorTotalCompras: number;
  ticketMedio: number;
}

export interface ClienteFollowUp {
  id: string;
  filialId: string;
  clienteId: string;
  status: 'PENDENTE' | 'CONCLUIDO' | 'CANCELADO';
  assunto: string;
  observacao?: string | null;
  agendadoPara: string;
}

export interface ClienteInteracao {
  id: string;
  filialId: string;
  clienteId: string;
  followUpId?: string | null;
  canal: string;
  resultado: string;
  assunto: string;
  ocorridoEm: string;
}
