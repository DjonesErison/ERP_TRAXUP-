export interface Cliente {
  id: string;
  tipoPessoa: 'FISICA' | 'JURIDICA';
  nomeRazaoSocial: string;
  nomeFantasia?: string | null;
  cpfCnpj?: string | null;
  email?: string | null;
  telefone?: string | null;
  cliente: boolean;
  fornecedor: boolean;
  ativo: boolean;
  criadoEm: string;
  atualizadoEm: string;
}
export interface NovoCliente { tipoPessoa:string; nomeRazaoSocial:string; nomeFantasia?:string; cpfCnpj?:string; email?:string; telefone?:string; cliente:true; fornecedor:boolean; }
export interface ClienteContato { id:string; pessoaId:string; nome:string; cargo?:string|null; email?:string|null; telefone?:string|null; principal:boolean; criadoEm:string; atualizadoEm:string; }
export interface NovoContato { nome:string; cargo?:string; email?:string; telefone?:string; principal:boolean; }
export interface ClienteEndereco { id:string; pessoaId:string; tipo:string; logradouro:string; numero?:string|null; complemento?:string|null; bairro?:string|null; cidade:string; uf:string; cep?:string|null; principal:boolean; criadoEm:string; atualizadoEm:string; }
export interface NovoEndereco { tipo:string; logradouro:string; numero?:string; complemento?:string; bairro?:string; cidade:string; uf:string; cep?:string; principal:boolean; }
export interface ClienteVenda { id:string; clienteId?:string|null; numero:string; status:string; totalLiquido:number; criadoEm:string; }
export interface ClienteVendaPagina { conteudo:ClienteVenda[]; pagina:number; tamanho:number; totalRegistros:number; totalPaginas:number; }