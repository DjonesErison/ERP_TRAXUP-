# Fase 7 — Bloco 4 — Configuração operacional do PDV

## Entrega inicial

A configuração operacional passa a ser tenant-safe e vinculada a um terminal/filial já cadastrado.

Endpoints:
- `GET /api/v1/pdv/terminais/{terminalId}/configuracao`
- `PUT /api/v1/pdv/terminais/{terminalId}/configuracao`

Parâmetros entregues:
- exigir justificativa para cancelamento;
- exigir autorização para cancelamento;
- tamanho de impressão: `PEQUENA`, `MEDIA` ou `GRANDE`;
- destino caixa habilitado/desabilitado;
- destino cozinha habilitado/desabilitado.

Os padrões são seguros: justificativa de cancelamento habilitada, autorização adicional desabilitada, impressão média, caixa habilitado e cozinha desabilitada.

## Segurança

O tenant é obtido exclusivamente do `TenantContext`. O terminal é resolvido pelo backend dentro do tenant autenticado e a tabela possui FKs compostas tenant-safe para filial e terminal. Leitura e gestão usam `PDV_CONFIGURACAO_LER` e `PDV_CONFIGURACAO_GERENCIAR`. Alterações são auditadas sem armazenar segredos.

## Limites desta entrega

A configuração não implementa drivers de impressora no backend. Autodetecção de impressoras Windows pertence ao aplicativo local. Fechamento de caixa é uma operação transacional própria e será implementado separadamente, para não ser confundido com simples parâmetro de configuração.

A TRAXUP Central permanece sem alterações.
