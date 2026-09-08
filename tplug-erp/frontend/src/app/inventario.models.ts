export type InventarioStatus = 'ABERTO' | 'CONCLUIDO' | 'CANCELADO';
export type InventarioTipoItem = 'PRODUTO' | 'GRADE';

export interface InventarioSessao {
  id: string;
  filialId: string;
  descricao?: string | null;
  status: InventarioStatus;
  contagemCega: boolean;
  criadoPorId?: string | null;
  criadoEm: string;
  concluidoPorId?: string | null;
  concluidoEm?: string | null;
  ajustadoPorId?: string | null;
  ajustadoEm?: string | null;
}

export interface InventarioItemLeitura {
  tipoItem: InventarioTipoItem;
  itemId: string;
  codigo: string;
  descricao: string;
  codigoBarra?: string | null;
}

export interface InventarioContagem {
  id: string;
  tipoItem: InventarioTipoItem;
  itemId: string;
  quantidadeSistema: number | null;
  quantidadeContada: number;
  divergencia: number | null;
  contadoPorId?: string | null;
  contadoEm: string;
  atualizadoEm: string;
}

export interface InventarioDivergencia extends Omit<InventarioContagem, 'quantidadeSistema' | 'divergencia'> {
  quantidadeSistema: number;
  divergencia: number;
  codigoItem: string;
  descricaoItem: string;
}
