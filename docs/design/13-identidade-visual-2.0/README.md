# TRAXUP — Identidade Visual 2.0

> Nova base visual oficial do projeto. Iniciada em 07/10/2026 após revisão da identidade anterior.

## 1. Marca oficial definida

A nova referência mantém o símbolo de crescimento formado por barras azuis e seta ascendente laranja.

**Wordmark:** TRAXUP  
- TRAX: azul
- UP: laranja

**Assinatura institucional:** Tecnologia que impulsiona negócios.

A logomarca deve ser utilizada preferencialmente com fundo transparente nas novas telas. Não aplicar caixa branca, fundo branco artificial ou halo branco ao redor da marca.

## 2. Paleta-base

- Azul principal: #0066FF
- Azul escuro: #0033A0
- Laranja principal: #FF8A00
- Laranja claro/destaque: #FFC107
- Branco/neutros: usados como base das interfaces

Os valores acima são a base inicial do Design System e podem receber ajustes técnicos de contraste/acessibilidade antes da implementação final.

## 3. Tipografia

Direção aprovada para a nova identidade: tipografia sans-serif moderna, forte e altamente legível. Para interfaces, utilizar família compatível com Montserrat/alternativa web equivalente até a definição final do Design System.

## 4. Princípios para as novas telas

1. Interface limpa e com pouco texto.
2. Reduzir elementos decorativos e excesso de cards.
3. Azul como cor estrutural/principal.
4. Laranja como destaque e ação complementar, sem excesso.
5. Logo sempre limpa e preferencialmente transparente.
6. Hierarquia tipográfica clara.
7. Formulários simples, com poucos campos por etapa quando possível.
8. Mesma linguagem visual em ERP, Trial e demais produtos, respeitando o contexto de uso.
9. Design aprovado não equivale a implementação.

## 5. Nova tela de Trial

Primeira tela criada sob a nova identidade.

Direção:
- composição dividida;
- área institucional azul;
- formulário claro;
- mensagem curta;
- foco no teste gratuito de sete dias;
- CTA principal evidente;
- menos conteúdo que a UI-014 anterior.

### Estado
**APROVADA em 07/10/2026 — referência oficial para implementação.**

A nova Trial 2.0 substitui a UI-014 como referência visual ativa. A UI-014 anterior permanece apenas como legado/histórico.

## 6. Estrutura desta pasta

A pasta `docs/design/13-identidade-visual-2.0/` passa a concentrar somente a nova geração visual.

Estrutura planejada:

- `marca/` — logo principal, transparente, ícone e variações;
- `trial/` — propostas e versão aprovada do novo Trial;
- `erp/` — Login, Dashboard e módulos redesenhados;
- `pdv/` — nova identidade do PDV;
- `apps/` — identidade dos aplicativos;
- `design-system/` — cores, tipografia, componentes e regras.

As imagens anteriores permanecem como histórico até a nova identidade estar aprovada e consolidada.

## 7. Processo de substituição

Nova proposta → revisão → correção → aprovação explícita → registro nesta pasta → atualização do Documento Mestre → implementação → teste → homologação.

Não excluir referências antigas antes da aprovação da substituta.

## 8. Próximos passos

1. Aprovar/corrigir a nova tela de Trial.
2. Fechar Design System TRAXUP 2.0.
3. Criar Login.
4. Criar Dashboard ERP.
5. Redesenhar os módulos restantes.
6. Somente depois substituir/arquivar as referências visuais antigas.


## 9. Especificação aprovada — Trial 2.0

Ordem dos campos:
1. Nome fantasia
2. CNPJ
3. Razão social
4. E-mail
5. Telefone / WhatsApp
6. Segmento do negócio
7. Nome do responsável
8. Quantidade de lojas

A tela não possui o bloco “Já tem uma conta? / Entrar”.

CTA: **Criar conta de teste**.

Consentimento exibido abaixo do CTA: “Ao se cadastrar você concorda com as Políticas de Privacidade e com os Termos de uso.”

- Políticas de Privacidade: https://institucional.locaweb.com.br/politicas/
- Termos de uso: https://www.connectplug.com.br/termos_de_uso

Fluxo de implementação: primeira tela limpa → criação/confirmação de senha em etapa seguinte → criação do ambiente Trial → primeiro acesso/onboarding.


## 10. Política obrigatória de assets — Identidade Visual 2.0

`docs/design/13-identidade-visual-2.0/` é a **única fonte oficial de imagens aprovadas** para telas da Identidade Visual 2.0.

1. Nenhuma tela 2.0 pode reutilizar imagens, logos, mockups ou ilustrações do catálogo visual anterior.
2. Assets existentes em `tplug-erp/frontend/src/assets/` não são considerados aprovados para a Identidade 2.0 apenas por existirem no repositório.
3. Uma imagem só pode ser usada numa tela 2.0 depois de existir nesta pasta oficial e estar marcada como **APROVADA**.
4. Se o asset aprovado ainda não estiver gravado nesta pasta, a tela deve permanecer sem a imagem em vez de usar um substituto legado ou recriado.
5. O Login/ERP legado pode manter temporariamente seus assets atuais até ser redesenhado; isso não autoriza seu uso nas novas telas 2.0.

### Estado atual

Os arquivos binários da logo transparente e da referência visual final ainda precisam ser incorporados ao repositório. Até isso ocorrer, a Trial 2.0 não deve exibir logo/imagem substituta da identidade anterior.


## 11. Referência oficial da Trial 2.0 — APROVADA

A referência visual aprovada pelo responsável do projeto em 07/10/2026 é o arquivo **Trial.png** fornecido na conversa do projeto.

Destino obrigatório no repositório quando o upload binário estiver disponível:

`docs/design/13-identidade-visual-2.0/trial/Trial.png`

### Composição obrigatória

- painel institucional azul à esquerda, com cantos arredondados;
- logomarca TRAXUP transparente no topo esquerdo;
- título “Seu negócio mais simples, mais eficiente.”;
- texto curto sobre teste por 7 dias;
- quatro benefícios: PDV completo, Gestão integrada, Acesso em qualquer lugar, Seguro e confiável;
- composição de notebook + celular na base esquerda, usando somente arte aprovada da Identidade 2.0;
- formulário branco à direita;
- oito campos, nesta ordem: Nome fantasia, CNPJ, Razão social, E-mail, Telefone / WhatsApp, Segmento do seu negócio, Nome do responsável, Quantidade de lojas;
- CTA “Criar conta de teste”;
- Políticas de Privacidade e Termos de uso abaixo do CTA;
- faixa inferior com 7 dias de teste gratuito, Sem cartão de crédito e Acesso rápido e sem burocracia;
- não exibir checkbox extra de aceite;
- não exibir “Já tem uma conta? / Entrar”.

### Regra de fidelidade

A implementação deve reproduzir esta referência visual e não pode substituir a logo, notebook/celular, ícones ou demais elementos por assets da identidade anterior. Na ausência do binário aprovado no repositório, o elemento deve permanecer ausente em vez de receber substituto legado.
